package com.example.studentprojecttracker.models;

public class ChatMessage {
    private String messageId;
    private String senderId;
    private String senderName;
    private String text;
    private long timestamp;
    private String fileUrl;

    public ChatMessage() {} // Firestore needs empty constructor

    public ChatMessage(String messageId, String senderId, String senderName, String text, long timestamp, String fileUrl) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
        this.fileUrl = fileUrl;
    }

    public String getMessageId() { return messageId; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
    public String getFileUrl() { return fileUrl; }
}
