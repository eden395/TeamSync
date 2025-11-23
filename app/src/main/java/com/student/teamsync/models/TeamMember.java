package com.student.teamsync.models;

public class TeamMember {
    private String name;
    private String status;
    private int tasksAssigned;
    private int completionPercentage;

    public TeamMember() {
    }

    public TeamMember(String name, String status, int tasksAssigned, int completionPercentage) {
        this.name = name;
        this.status = status;
        this.tasksAssigned = tasksAssigned;
        this.completionPercentage = completionPercentage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
