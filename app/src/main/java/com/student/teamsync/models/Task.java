package com.student.teamsync.models;

public class Task {
    private String taskName;
    private String priority;
    private String assignee;
    private String dueDate;
    private boolean isCompleted;

    public Task() {
    }

    public Task(String taskName, String priority, String assignee, String dueDate, boolean isCompleted) {
        this.taskName = taskName;
        this.priority = priority;
        this.assignee = assignee;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
