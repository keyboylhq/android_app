package com.example.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * BroadcastReceiver to handle boot completed events
 * This receiver will start the SMS Forwarder app when the device boots up
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called, action: " + intent.getAction());
        
        if (intent.getAction() != null) {
            // Check if the action is boot completed or reboot
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction()) ||
                Intent.ACTION_REBOOT.equals(intent.getAction())) {
                
                Log.d(TAG, "Device booted up, starting SMS Forwarder...");
                
                // Start the main activity
                Intent startIntent = new Intent(context, MainActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(startIntent);
                
                Log.d(TAG, "SMS Forwarder started successfully");
            } else {
                Log.d(TAG, "Unknown action: " + intent.getAction());
            }
        } else {
            Log.e(TAG, "Intent action is null");
        }
    }
}