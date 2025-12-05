package com.example.studentprojecttracker.models;

import java.io.Serializable;

public class Task implements Serializable {
    private String taskId;
    private String taskName;
    private String priority;
    private String assigneeId;  // User ID of assigned person
    private String assigneeName;  // Display name
    private String dueDate;
    private boolean isCompleted;
    private String projectId;
    private String createdBy;  // Leader who created the task

    public Task() {
    }

    public Task(String taskName, String priority, String assigneeName, String dueDate, boolean isCompleted) {
        this.taskName = taskName;
        this.priority = priority;
        this.assigneeName = assigneeName;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
    }

    public Task(String taskId, String taskName, String priority, String assigneeId, String assigneeName,
                String dueDate, boolean isCompleted, String projectId, String createdBy) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.priority = priority;
        this.assigneeId = assigneeId;
        this.assigneeName = assigneeName;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.projectId = projectId;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getAssigneeId() { return assigneeId; }
    public void setAssigneeId(String assigneeId) { this.assigneeId = assigneeId; }

    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }

    // Legacy getter for compatibility
    public String getAssignee() { return assigneeName; }
    public void setAssignee(String assignee) { this.assigneeName = assignee; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}