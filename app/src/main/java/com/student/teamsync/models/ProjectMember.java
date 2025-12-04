package com.student.teamsync.models;

import androidx.room.Entity;
import androidx.annotation.NonNull;

@Entity(tableName = "project_members",
        primaryKeys = {"projectLocalId", "userId"})
public class ProjectMember {
    public long projectLocalId;

    @NonNull
    public String userId;

    public ProjectMember(long projectLocalId, String userId) {
        this.projectLocalId = projectLocalId;
        this.userId = userId;
    }
}