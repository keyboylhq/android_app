package com.example.smsforwarder;

public class SMSMessage {
    private String sender;
    private String message;
    private String timestamp;

    public SMSMessage(String sender, String message, String timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "From: " + sender + "\nTime: " + timestamp + "\n\n" + message;
    }
}