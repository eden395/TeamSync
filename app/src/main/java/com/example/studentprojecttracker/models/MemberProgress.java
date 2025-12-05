package com.example.studentprojecttracker.models;

import java.io.Serializable;

public class MemberProgress implements Serializable {
    private String memberId;
    private String memberName;
    private String memberEmail;
    private int tasksCompleted;
    private int tasksTotal;
    private int progressPercentage;
    private String status; // "Ahead", "On Track", "Behind"

    public MemberProgress() {}

    public MemberProgress(String memberId, String memberName, String memberEmail,
                          int tasksCompleted, int tasksTotal, String status) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberEmail = memberEmail;
        this.tasksCompleted = tasksCompleted;
        this.tasksTotal = tasksTotal;
        this.status = status;
        calculateProgress();
    }

    private void calculateProgress() {
        if (tasksTotal > 0) {
            this.progressPercentage = (tasksCompleted * 100) / tasksTotal;
        } else {
            this.progressPercentage = 0;
        }
    }

    // Getters and Setters
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getMemberEmail() { return memberEmail; }
    public void setMemberEmail(String memberEmail) { this.memberEmail = memberEmail; }

    public int getTasksCompleted() { return tasksCompleted; }
    public void setTasksCompleted(int tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
        calculateProgress();
    }

    public int getTasksTotal() { return tasksTotal; }
    public void setTasksTotal(int tasksTotal) {
        this.tasksTotal = tasksTotal;
        calculateProgress();
    }

    public int getProgressPercentage() { return progressPercentage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}