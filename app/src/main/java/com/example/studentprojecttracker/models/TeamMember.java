package com.example.studentprojecttracker.models;

import java.io.Serializable;

public class TeamMember implements Serializable {
    private String userId;
    private String name;
    private String email;
    private String photoUrl;
    private String status;
    private int tasksAssigned;
    private int completionPercentage;

    public TeamMember() {
    }

    public TeamMember(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.status = "On Track";
        this.tasksAssigned = 0;
        this.completionPercentage = 0;
    }

    public TeamMember(String userId, String name, String email, String status,
                      int tasksAssigned, int completionPercentage) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.status = status;
        this.tasksAssigned = tasksAssigned;
        this.completionPercentage = completionPercentage;
    }

    public TeamMember(String name, String onTrack, int i, int i1) {
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTasksAssigned() {
        return tasksAssigned;
    }

    public void setTasksAssigned(int tasksAssigned) {
        this.tasksAssigned = tasksAssigned;
    }

    public int getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(int completionPercentage) {
        this.completionPercentage = completionPercentage;
    }
}