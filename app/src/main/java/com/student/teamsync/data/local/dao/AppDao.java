package com.student.teamsync.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.student.teamsync.models.*;

import java.util.List;

@Dao
public interface AppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Insert
    long insertProject(ProjectEntity project);

    @Insert
    void insertProjectMembers(List<ProjectMember> members);

    @Query("SELECT p.* FROM projects p " +
            "INNER JOIN project_members pm ON p.localId = pm.projectLocalId " +
            "WHERE pm.userId = :currentUserId")
    LiveData<List<ProjectEntity>> getMyProjects(String currentUserId);
}