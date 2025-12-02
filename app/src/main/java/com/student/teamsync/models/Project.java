package com.student.teamsync.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Project implements Serializable {
    private String projectId;
    private String projectName;
    private String courseCode;
    private String dueDate;
    private List<String> memberIds;
    private List<String> mentorIds;
    private int progressPercentage;

    public Project() {
        this.memberIds = new ArrayList<>();
        this.mentorIds = new ArrayList<>();
    }

    public Project(String projectId, String projectName, String courseCode, String dueDate) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.courseCode = courseCode;
        this.dueDate = dueDate;
        this.memberIds = new ArrayList<>();
        this.mentorIds = new ArrayList<>();
        this.progressPercentage = 0;
    }

    // Getters and Setters
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }

    public List<String> getMentorIds() { return mentorIds; }
    public void setMentorIds(List<String> mentorIds) { this.mentorIds = mentorIds; }

    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
}