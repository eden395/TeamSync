package com.example.studentprojecttracker.models;

public class FileModel {

    private String name;
    private String url;
    private long uploadedAt;

    // Required empty constructor for Firestore
    public FileModel() {}

    public FileModel(String name, String url, long uploadedAt) {
        this.name = name;
        this.url = url;
        this.uploadedAt = uploadedAt;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public long getUploadedAt() {
        return uploadedAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUploadedAt(long uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}