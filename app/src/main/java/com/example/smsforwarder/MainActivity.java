package com.example.smsforwarder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.Manifest;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int SMS_PERMISSION_REQUEST_CODE = 100;
    
    private TextView textView;
    private TextView permissionStatus;
    private TextView testResult;
    private Button testEmailButton;
    private Button testSmsButton;
    private Button readSmsButton;
    private ListView smsListView;
    
    private List<SMSMessage> smsMessageList;
    private ArrayAdapter<SMSMessage> smsAdapter;
    
    private BroadcastReceiver smsBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        textView = findViewById(R.id.text_view);
        permissionStatus = findViewById(R.id.permission_status);
        testResult = findViewById(R.id.test_result);
        testEmailButton = findViewById(R.id.test_email_button);
        testSmsButton = findViewById(R.id.test_sms_button);
        readSmsButton = findViewById(R.id.read_sms_button);
        smsListView = findViewById(R.id.sms_list);

        // Initialize SMS list and adapter
        smsMessageList = new ArrayList<>();
        smsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsMessageList);
        smsListView.setAdapter(smsAdapter);

        checkAndRequestPermissions();
        setupButtonListeners();
        setupBroadcastReceiver();
    }

    private void checkAndRequestPermissions() {
        // Check RECEIVE_SMS permission
        boolean hasReceiveSms = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
        // Check READ_SMS permission
        boolean hasReadSms = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
        // Check INTERNET permission
        boolean hasInternet = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;

        String status = "Permissions: " +
                "RECEIVE_SMS: " + (hasReceiveSms ? "GRANTED" : "DENIED") + ", " +
                "READ_SMS: " + (hasReadSms ? "GRANTED" : "DENIED") + ", " +
                "INTERNET: " + (hasInternet ? "GRANTED" : "DENIED");

        permissionStatus.setText(status);

        if (!hasReceiveSms || !hasReadSms) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, 
                    SMS_PERMISSION_REQUEST_CODE);
        }
    }

    private void setupButtonListeners() {
        // Test Email Configuration Button
        testEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testResult.setText("Sending test email...");
                String testSubject = "Test Email from SMS Forwarder";
                String testBody = "This is a test email to verify that the SMS Forwarder email configuration is working correctly.\n\nSent on: " + new java.util.Date();
                SMSReceiver.sendEmail(MainActivity.this, testSubject, testBody);
                testResult.setText("Test email sent! Check your inbox.");
            }
        });

        // Test SMS Forwarding Button
        testSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testResult.setText("Testing SMS forwarding...");
                // Simulate SMS forwarding by sending a test email
                String testSubject = "Test SMS Forward";
                String testBody = "This is a test of the SMS forwarding feature.\n\nSender: 1234567890\nMessage: This is a test message for SMS forwarding.";
                SMSReceiver.sendEmail(MainActivity.this, testSubject, testBody);
                testResult.setText("SMS forwarding test completed! Check your inbox.");
            }
        });
        
        // Read SMS Database Button
        readSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testResult.setText("Reading SMS database...");
                readSmsFromDatabase();
            }
        });
    }
    
    /**
     * Read SMS messages directly from the database
     */
    private void readSmsFromDatabase() {
        Log.d(TAG, "Reading SMS from database...");
        
        // Check if we have READ_SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "READ_SMS permission not granted");
            testResult.setText("Error: READ_SMS permission not granted");
            return;
        }
        
        try {
            // Use Telephony.Sms.Inbox.CONTENT_URI to read SMS from inbox
            Uri smsUri = Telephony.Sms.Inbox.CONTENT_URI;
            String[] projection = new String[] {
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
            };
            String sortOrder = Telephony.Sms.DATE + " DESC";
            
            Cursor cursor = getContentResolver().query(smsUri, projection, null, null, sortOrder);
            
            if (cursor != null) {
                int addressIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS);
                int bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY);
                int dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE);
                
                Log.d(TAG, "Cursor columns: addressIndex=" + addressIndex + ", bodyIndex=" + bodyIndex + ", dateIndex=" + dateIndex);
                
                // Clear current list
                smsMessageList.clear();
                
                int count = 0;
                // Read up to 10 latest SMS messages
                while (cursor.moveToNext() && count < 10) {
                    String sender = cursor.getString(addressIndex);
                    String message = cursor.getString(bodyIndex);
                    long dateMillis = cursor.getLong(dateIndex);
                    
                    // Format date
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                    String timestamp = sdf.format(new java.util.Date(dateMillis));
                    
                    Log.d(TAG, "Read SMS: sender=" + sender + ", message=" + message + ", timestamp=" + timestamp);
                    
                    // Add to list
                    SMSMessage smsMessage = new SMSMessage(sender, message, timestamp);
                    smsMessageList.add(smsMessage);
                    count++;
                }
                
                cursor.close();
                
                // Update UI
                smsAdapter.notifyDataSetChanged();
                
                if (count > 0) {
                    testResult.setText("Successfully read " + count + " SMS messages from database");
                    android.widget.Toast.makeText(this, "Read " + count + " SMS messages", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    testResult.setText("No SMS messages found in database");
                    android.widget.Toast.makeText(this, "No SMS messages found", android.widget.Toast.LENGTH_SHORT).show();
                }
                
                Log.d(TAG, "Read " + count + " SMS messages from database");
            } else {
                Log.e(TAG, "Failed to query SMS database");
                testResult.setText("Error: Failed to query SMS database");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading SMS database: " + e.getMessage(), e);
            testResult.setText("Error: " + e.getMessage());
        }
    }

    private void setupBroadcastReceiver() {
        // Create broadcast receiver for SMS messages
        smsBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Broadcast received, action: " + intent.getAction());
                if (intent.getAction() != null) {
                    if (SMSReceiver.ACTION_SMS_RECEIVED.equals(intent.getAction())) {
                        String sender = intent.getStringExtra(SMSReceiver.EXTRA_SENDER);
                        String message = intent.getStringExtra(SMSReceiver.EXTRA_MESSAGE);
                        String timestamp = intent.getStringExtra(SMSReceiver.EXTRA_TIMESTAMP);
                        
                        Log.d(TAG, "Received SMS broadcast: sender=" + sender + ", message=" + message + ", timestamp=" + timestamp);
                        
                        // Add SMS to list and update UI
                        if (sender != null && message != null) {
                            SMSMessage smsMessage = new SMSMessage(sender, message, timestamp);
                            smsMessageList.add(0, smsMessage); // Add to top of list
                            smsAdapter.notifyDataSetChanged();
                            
                            // Show toast notification
                            android.widget.Toast.makeText(context, "New SMS from " + sender, android.widget.Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Invalid SMS data: sender=" + sender + ", message=" + message);
                        }
                    } else {
                        Log.d(TAG, "Unknown broadcast action: " + intent.getAction());
                    }
                } else {
                    Log.e(TAG, "Broadcast intent action is null");
                }
            }
        };
        
        // Register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SMSReceiver.ACTION_SMS_RECEIVED);
        filter.setPriority(1000); // High priority to ensure we receive the broadcast
        
        try {
            registerReceiver(smsBroadcastReceiver, filter);
            Log.d(TAG, "SMS broadcast receiver registered successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to register SMS broadcast receiver: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure receiver is registered when activity resumes
        if (smsBroadcastReceiver == null) {
            setupBroadcastReceiver();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister receiver when activity pauses
        if (smsBroadcastReceiver != null) {
            unregisterReceiver(smsBroadcastReceiver);
            smsBroadcastReceiver = null;
            Log.d(TAG, "SMS broadcast receiver unregistered");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            checkAndRequestPermissions();
        }
    }
}