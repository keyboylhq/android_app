package com.example.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SMSReceiver extends BroadcastReceiver {

    private static final String TAG = "SMSReceiver";
    
    // Email configuration - WARNING: These values should be stored securely
    // For production use, consider using SharedPreferences, encrypted storage, or a backend service
    private static final String SMTP_SERVER = "smtp.qq.com";
    private static final int SMTP_PORT = 465;
    
    // TODO: Replace with your email configuration
    // These values should not be hardcoded in production
    private static final String EMAIL_FROM = "your_email@example.com";
    private static final String EMAIL_PASSWORD = "your_email_password_or_app_password";
    private static final String EMAIL_TO = "recipient_email@example.com";
    
    // Local broadcast action for SMS received
    public static final String ACTION_SMS_RECEIVED = "com.example.smsforwarder.ACTION_SMS_RECEIVED";
    public static final String EXTRA_SENDER = "sender";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_TIMESTAMP = "timestamp";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called, action: " + intent.getAction());
        if (intent.getAction() != null) {
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Log.d(TAG, "Received SMS_RECEIVED broadcast");
                
                // Try both pdus and messages extra keys (different Android versions may use different keys)
                Object[] pdus = (Object[]) intent.getExtras().get("pdus");
                
                if (pdus == null) {
                    pdus = (Object[]) intent.getExtras().get("messages");
                    Log.d(TAG, "Trying 'messages' extra instead of 'pdus': " + (pdus != null ? pdus.length : "null"));
                }
                
                if (pdus != null && pdus.length > 0) {
                    Log.d(TAG, "PDUs found: " + pdus.length);
                    StringBuilder messageBody = new StringBuilder();
                    String sender = null;
                    
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        if (sender == null) {
                            sender = smsMessage.getOriginatingAddress();
                            Log.d(TAG, "Sender: " + sender);
                        }
                        String part = smsMessage.getMessageBody();
                        Log.d(TAG, "Message part: " + part);
                        messageBody.append(part);
                    }
                    
                    if (sender != null && messageBody.length() > 0) {
                        String subject = "New SMS from " + sender;
                        String body = "From: " + sender + "\n\n" + messageBody.toString();
                        Log.d(TAG, "Sending email: " + subject);
                        
                        // Format timestamp
                        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                        
                        // Send email
                        sendEmail(context, subject, body);
                        
                        // Send local broadcast to update UI
                        Intent localIntent = new Intent(ACTION_SMS_RECEIVED);
                        localIntent.putExtra(EXTRA_SENDER, sender);
                        localIntent.putExtra(EXTRA_MESSAGE, messageBody.toString());
                        localIntent.putExtra(EXTRA_TIMESTAMP, timestamp);
                        context.sendBroadcast(localIntent);
                        Log.d(TAG, "Sent local broadcast for SMS: " + sender);
                    } else {
                        Log.e(TAG, "Invalid SMS data: sender=" + sender + ", body length=" + messageBody.length());
                    }
                } else {
                    Log.e(TAG, "No PDUs found in intent");
                    // Log all extras for debugging
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        for (String key : extras.keySet()) {
                            Log.d(TAG, "Extra: " + key + " = " + extras.get(key));
                        }
                    } else {
                        Log.e(TAG, "Intent extras is null");
                    }
                }
            } else {
                Log.d(TAG, "Unknown action: " + intent.getAction());
            }
        } else {
            Log.e(TAG, "Intent action is null");
        }
    }

    public static void sendEmail(final Context context, final String subject, final String body) {
        final String TAG = "SMSReceiver"; // Static context for logging
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Properties props = new Properties();
                    props.put("mail.smtp.host", SMTP_SERVER);
                    props.put("mail.smtp.port", SMTP_PORT);
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    props.put("mail.smtp.socketFactory.port", SMTP_PORT);
                    props.put("mail.smtp.starttls.enable", "true");
                    
                    Session session = Session.getInstance(props, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                        }
                    });
                    
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(EMAIL_FROM));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(EMAIL_TO));
                    message.setSubject(subject);
                    message.setText(body);
                    
                    Transport.send(message);
                    Log.d(TAG, "Email sent successfully");
                    showToast(context, "Email test sent successfully!");
                } catch (MessagingException e) {
                    Log.e(TAG, "Failed to send email: " + e.getMessage(), e);
                    showToast(context, "Email test failed: " + e.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error: " + e.getMessage(), e);
                    showToast(context, "Email test failed with unexpected error");
                }
            }
        }).start();
    }
    
    private static void showToast(final Context context, final String message) {
        android.os.Handler handler = new android.os.Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }
}