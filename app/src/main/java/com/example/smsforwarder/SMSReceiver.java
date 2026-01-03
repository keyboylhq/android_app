package com.example.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
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
    
    // Local broadcast action for SMS received
    public static final String ACTION_SMS_RECEIVED = "com.example.smsforwarder.ACTION_SMS_RECEIVED";
    public static final String EXTRA_SENDER = "sender";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_TIMESTAMP = "timestamp";    
    
    // Configuration keys
    private static final String CONFIG_FILE = "local.properties";
    private static final String KEY_SMTP_SERVER = "smtp.server";
    private static final String KEY_SMTP_PORT = "smtp.port";
    private static final String KEY_EMAIL_FROM = "email.from";
    private static final String KEY_EMAIL_PASSWORD = "email.password";
    private static final String KEY_EMAIL_TO = "email.to";
    
    // Default values if not in configuration file
    private static final String DEFAULT_SMTP_SERVER = "smtp.qq.com";
    private static final int DEFAULT_SMTP_PORT = 465;
    private static final String DEFAULT_EMAIL_FROM = "your_email@example.com";
    private static final String DEFAULT_EMAIL_PASSWORD = "your_email_password_or_app_password";
    private static final String DEFAULT_EMAIL_TO = "recipient_email@example.com";

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

    /**
     * Load configuration from local.properties file
     * @param context Application context
     * @return Properties object with configuration values
     */
    private static Properties loadConfig(Context context) {
        Properties config = new Properties();
        String projectRoot = context.getFilesDir().getParentFile().getParentFile().getParentFile().getParentFile().getPath();
        String configPath = projectRoot + "/" + CONFIG_FILE;
        
        Log.d(TAG, "Loading configuration from: " + configPath);
        
        try (FileInputStream fis = new FileInputStream(configPath)) {
            config.load(fis);
            Log.d(TAG, "Successfully loaded configuration from " + CONFIG_FILE);
        } catch (IOException e) {
            Log.w(TAG, "Failed to load configuration file " + CONFIG_FILE + ": " + e.getMessage());
            Log.w(TAG, "Using default configuration values");
        }
        
        return config;
    }
    
    public static void sendEmail(final Context context, final String subject, final String body) {
        final String TAG = "SMSReceiver"; // Static context for logging
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Load configuration
                    Properties config = loadConfig(context);
                    
                    // Get configuration values or use defaults
                    String smtpServer = config.getProperty(KEY_SMTP_SERVER, DEFAULT_SMTP_SERVER);
                    int smtpPort = Integer.parseInt(config.getProperty(KEY_SMTP_PORT, String.valueOf(DEFAULT_SMTP_PORT)));
                    String emailFrom = config.getProperty(KEY_EMAIL_FROM, DEFAULT_EMAIL_FROM);
                    String emailPassword = config.getProperty(KEY_EMAIL_PASSWORD, DEFAULT_EMAIL_PASSWORD);
                    String emailTo = config.getProperty(KEY_EMAIL_TO, DEFAULT_EMAIL_TO);
                    
                    Log.d(TAG, "Using SMTP server: " + smtpServer + ":" + smtpPort);
                    Log.d(TAG, "Sending from: " + emailFrom + " to: " + emailTo);
                    
                    Properties props = new Properties();
                    props.put("mail.smtp.host", smtpServer);
                    props.put("mail.smtp.port", smtpPort);
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    props.put("mail.smtp.socketFactory.port", smtpPort);
                    props.put("mail.smtp.starttls.enable", "true");
                    
                    Session session = Session.getInstance(props, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(emailFrom, emailPassword);
                        }
                    });
                    
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(emailFrom));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTo));
                    message.setSubject(subject);
                    message.setText(body);
                    
                    Transport.send(message);
                    Log.d(TAG, "Email sent successfully");
                    showToast(context, "Email sent successfully!");
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid SMTP port format: " + e.getMessage(), e);
                    showToast(context, "Email failed: Invalid SMTP port format");
                } catch (MessagingException e) {
                    Log.e(TAG, "Failed to send email: " + e.getMessage(), e);
                    showToast(context, "Email failed: " + e.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error: " + e.getMessage(), e);
                    showToast(context, "Email failed with unexpected error");
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