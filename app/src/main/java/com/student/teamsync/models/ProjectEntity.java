package com.student.teamsync.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "projects")
public class ProjectEntity {
    @PrimaryKey(autoGenerate = true)
    public long localId;

    public String firebaseId;
    public String projectName;
    public String courseCode;
    public String dueDate;
    public String ownerId;
    public int progressPercentage = 0;

    public ProjectEntity() {}
}