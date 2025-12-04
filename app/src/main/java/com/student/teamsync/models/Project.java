package com.student.teamsync.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Project implements Serializable {
    private String projectId;
    private String projectName;
    private String courseCode;
    private String dueDate;
    private String creatorId;  // The person who created the project
    private List<String> leaderIds;  // Project leaders (can be multiple)
    private List<String> memberIds;
    private List<String> mentorIds;
    private int progressPercentage;

    public Project() {
        this.leaderIds = new ArrayList<>();
        this.memberIds = new ArrayList<>();
        this.mentorIds = new ArrayList<>();
    }

    public Project(String projectId, String projectName, String courseCode, String dueDate, String creatorId) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.courseCode = courseCode;
        this.dueDate = dueDate;
        this.creatorId = creatorId;
        this.leaderIds = new ArrayList<>();
        this.leaderIds.add(creatorId); // Creator is automatically a leader
        this.memberIds = new ArrayList<>();
        this.memberIds.add(creatorId); // Creator is also a member
        this.mentorIds = new ArrayList<>();
        this.progressPercentage = 0;
    }

    public Project(String number, String s, String s2, String s1) {
    }

    // Check if user is a leader
    public boolean isLeader(String userId) {
        return leaderIds != null && leaderIds.contains(userId);
    }

    // Check if user is a member
    public boolean isMember(String userId) {
        return memberIds != null && memberIds.contains(userId);
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

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public List<String> getLeaderIds() { return leaderIds; }
    public void setLeaderIds(List<String> leaderIds) { this.leaderIds = leaderIds; }

    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }

    public List<String> getMentorIds() { return mentorIds; }
    public void setMentorIds(List<String> mentorIds) { this.mentorIds = mentorIds; }

    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
}