package com.example.studentprojecttracker.models;

import java.io.Serializable;

public class ProjectFile implements Serializable {
    private String fileId;
    private String fileName;
    private String fileUrl;
    private String uploadedBy;
    private String uploadDate;
    private String projectId;

    public ProjectFile() {}

    public ProjectFile(String fileId, String fileName, String fileUrl, String uploadedBy, String uploadDate, String projectId) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.uploadedBy = uploadedBy;
        this.uploadDate = uploadDate;
        this.projectId = projectId;
    }

    // Getters and Setters
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getUploadDate() { return uploadDate; }
    public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
}