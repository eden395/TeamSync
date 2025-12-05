package com.example.studentprojecttracker.models;

import java.io.Serializable;

public class User implements Serializable {
    private String userId;
    private String name;
    private String email;
    private String role; // "member" or "advisor"
    private String photoUrl; // Profile picture URL

    public User() {}

    public User(String userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public User(String userId, String name, String email, String role, String photoUrl) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.photoUrl = photoUrl;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}