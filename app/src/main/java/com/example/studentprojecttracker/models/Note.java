package com.example.studentprojecttracker.models;

public class Note {
    private String noteId;
    private String authorId;
    private String authorName;
    private String text;
    private long timestamp;

    public Note() {} // Empty constructor required for Firestore

    public Note(String noteId, String authorId, String authorName, String text, long timestamp) {
        this.noteId = noteId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getters & Setters
    public String getNoteId() { return noteId; }
    public void setNoteId(String noteId) { this.noteId = noteId; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
