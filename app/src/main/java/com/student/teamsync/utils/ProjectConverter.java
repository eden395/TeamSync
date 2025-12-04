package com.student.teamsync.utils;

import com.student.teamsync.models.Project;
import com.student.teamsync.models.ProjectEntity;

public class ProjectConverter {

    public static ProjectEntity toEntity(Project project, String ownerId) {
        ProjectEntity e = new ProjectEntity();
        e.firebaseId = project.getProjectId();
        e.projectName = project.getProjectName();
        e.courseCode = project.getCourseCode();
        e.dueDate = project.getDueDate();
        e.ownerId = ownerId;
        e.progressPercentage = project.getProgressPercentage();
        return e;
    }

    public static Project toModel(ProjectEntity e) {
        Project p = new Project(
                e.firebaseId != null ? e.firebaseId : "",
                e.projectName,
                e.courseCode,
                e.dueDate
        );
        p.setProgressPercentage(e.progressPercentage);
        return p;
    }
}