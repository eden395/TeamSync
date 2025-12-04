package com.student.teamsync.utils;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.student.teamsync.models.Project;
import com.student.teamsync.models.Task;
import com.student.teamsync.models.FileModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreHelper {
    private static final String TAG = "FirestoreHelper";

    // Collection names
    public static final String COLLECTION_PROJECTS = "projects";
    public static final String COLLECTION_TASKS = "tasks";
    public static final String COLLECTION_FILES = "files";
    public static final String COLLECTION_USERS = "users";

    private final FirebaseFirestore db;

    public FirestoreHelper() {
        this.db = FirebaseFirestore.getInstance();
    }

    // ==================== PROJECT OPERATIONS ====================

    public interface ProjectCallback {
        void onSuccess(List<Project> projects);
        void onError(String error);
    }

    public interface SingleProjectCallback {
        void onSuccess(Project project);
        void onError(String error);
    }

    public void createProject(Project project, OnCompleteListener listener) {
        Map<String, Object> projectData = new HashMap<>();
        projectData.put("projectId", project.getProjectId());
        projectData.put("projectName", project.getProjectName());
        projectData.put("courseCode", project.getCourseCode());
        projectData.put("dueDate", project.getDueDate());
        projectData.put("creatorId", project.getCreatorId());
        projectData.put("leaderIds", project.getLeaderIds());
        projectData.put("memberIds", project.getMemberIds());
        projectData.put("mentorIds", project.getMentorIds());
        projectData.put("progressPercentage", project.getProgressPercentage());
        projectData.put("createdAt", System.currentTimeMillis());

        db.collection(COLLECTION_PROJECTS)
                .document(project.getProjectId())
                .set(projectData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Project created successfully");
                    listener.onComplete(true, "Project created successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating project", e);
                    listener.onComplete(false, e.getMessage());
                });
    }

    public void updateProject(Project project, OnCompleteListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("projectName", project.getProjectName());
        updates.put("courseCode", project.getCourseCode());
        updates.put("dueDate", project.getDueDate());
        updates.put("leaderIds", project.getLeaderIds());
        updates.put("memberIds", project.getMemberIds());
        updates.put("mentorIds", project.getMentorIds());
        updates.put("progressPercentage", project.getProgressPercentage());
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection(COLLECTION_PROJECTS)
                .document(project.getProjectId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Project updated successfully");
                    listener.onComplete(true, "Project updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating project", e);
                    listener.onComplete(false, e.getMessage());
                });
    }

    public void deleteProject(String projectId, OnCompleteListener listener) {
        // Delete all tasks associated with the project first
        db.collection(COLLECTION_TASKS)
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete();
                    }

                    // Then delete the project
                    db.collection(COLLECTION_PROJECTS)
                            .document(projectId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Project deleted successfully");
                                listener.onComplete(true, "Project deleted successfully");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting project", e);
                                listener.onComplete(false, e.getMessage());
                            });
                });
    }

    public void getUserProjects(String userId, boolean isAdvisor, ProjectCallback callback) {
        Query query;

        if (isAdvisor) {
            // Advisors see projects they're mentoring
            query = db.collection(COLLECTION_PROJECTS)
                    .whereArrayContains("mentorIds", userId);
        } else {
            // Members see projects they're part of
            query = db.collection(COLLECTION_PROJECTS)
                    .whereArrayContains("memberIds", userId);
        }

        query.addSnapshotListener((querySnapshot, error) -> {
            if (error != null) {
                Log.e(TAG, "Error getting projects", error);
                callback.onError(error.getMessage());
                return;
            }

            List<Project> projects = new ArrayList<>();
            if (querySnapshot != null) {
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Project project = documentToProject(doc);
                    if (project != null) {
                        projects.add(project);
                    }
                }
            }
            callback.onSuccess(projects);
        });
    }

    public void getProject(String projectId, SingleProjectCallback callback) {
        db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Project project = documentToProject(documentSnapshot);
                        callback.onSuccess(project);
                    } else {
                        callback.onError("Project not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting project", e);
                    callback.onError(e.getMessage());
                });
    }

    // ==================== TASK OPERATIONS ====================

    public interface TaskCallback {
        void onSuccess(List<Task> tasks);
        void onError(String error);
    }

    public void createTask(Task task, OnCompleteListener listener) {
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("taskId", task.getTaskId());
        taskData.put("taskName", task.getTaskName());
        taskData.put("priority", task.getPriority());
        taskData.put("assigneeId", task.getAssigneeId());
        taskData.put("assigneeName", task.getAssigneeName());
        taskData.put("dueDate", task.getDueDate());
        taskData.put("isCompleted", task.isCompleted());
        taskData.put("projectId", task.getProjectId());
        taskData.put("createdBy", task.getCreatedBy());
        taskData.put("createdAt", System.currentTimeMillis());

        db.collection(COLLECTION_TASKS)
                .document(task.getTaskId())
                .set(taskData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Task created successfully");
                    listener.onComplete(true, "Task created successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating task", e);
                    listener.onComplete(false, e.getMessage());
                });
    }

    public void updateTask(Task task, OnCompleteListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("taskName", task.getTaskName());
        updates.put("priority", task.getPriority());
        updates.put("assigneeId", task.getAssigneeId());
        updates.put("assigneeName", task.getAssigneeName());
        updates.put("dueDate", task.getDueDate());
        updates.put("isCompleted", task.isCompleted());
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection(COLLECTION_TASKS)
                .document(task.getTaskId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Task updated successfully");
                    listener.onComplete(true, "Task updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating task", e);
                    listener.onComplete(false, e.getMessage());
                });
    }

    public void deleteTask(String taskId, OnCompleteListener listener) {
        db.collection(COLLECTION_TASKS)
                .document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Task deleted successfully");
                    listener.onComplete(true, "Task deleted successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting task", e);
                    listener.onComplete(false, e.getMessage());
                });
    }

    public void getProjectTasks(String projectId, TaskCallback callback) {
        db.collection(COLLECTION_TASKS)
                .whereEqualTo("projectId", projectId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting tasks", error);
                        callback.onError(error.getMessage());
                        return;
                    }

                    List<Task> tasks = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Task task = documentToTask(doc);
                            if (task != null) {
                                tasks.add(task);
                            }
                        }
                    }
                    callback.onSuccess(tasks);
                });
    }

    // ==================== FILE OPERATIONS ====================

    public interface FileCallback {
        void onSuccess(List<FileModel> files);
        void onError(String error);
    }

    public void saveFileMetadata(String projectId, FileModel file, OnCompleteListener listener) {
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("name", file.getName());
        fileData.put("url", file.getUrl());
        fileData.put("uploadedAt", file.getUploadedAt());
        fileData.put("projectId", projectId);

        db.collection(COLLECTION_FILES)
                .add(fileData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "File metadata saved successfully");
                    listener.onComplete(true, "File uploaded successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving file metadata", e);
                    listener.onComplete(false, e.getMessage());
                });
    }

    public void getProjectFiles(String projectId, FileCallback callback) {
        db.collection(COLLECTION_FILES)
                .whereEqualTo("projectId", projectId)
                .orderBy("uploadedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting files", error);
                        callback.onError(error.getMessage());
                        return;
                    }

                    List<FileModel> files = new ArrayList<>();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            FileModel file = documentToFile(doc);
                            if (file != null) {
                                files.add(file);
                            }
                        }
                    }
                    callback.onSuccess(files);
                });
    }

    // ==================== HELPER METHODS ====================

    private Project documentToProject(DocumentSnapshot doc) {
        try {
            String projectId = doc.getString("projectId");
            String projectName = doc.getString("projectName");
            String courseCode = doc.getString("courseCode");
            String dueDate = doc.getString("dueDate");
            String creatorId = doc.getString("creatorId");

            Project project = new Project(projectId, projectName, courseCode, dueDate, creatorId);

            List<String> leaderIds = (List<String>) doc.get("leaderIds");
            if (leaderIds != null) {
                project.setLeaderIds(leaderIds);
            }

            List<String> memberIds = (List<String>) doc.get("memberIds");
            if (memberIds != null) {
                project.setMemberIds(memberIds);
            }

            List<String> mentorIds = (List<String>) doc.get("mentorIds");
            if (mentorIds != null) {
                project.setMentorIds(mentorIds);
            }

            Long progress = doc.getLong("progressPercentage");
            if (progress != null) {
                project.setProgressPercentage(progress.intValue());
            }

            return project;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing project document", e);
            return null;
        }
    }

    private Task documentToTask(DocumentSnapshot doc) {
        try {
            String taskId = doc.getString("taskId");
            String taskName = doc.getString("taskName");
            String priority = doc.getString("priority");
            String assigneeId = doc.getString("assigneeId");
            String assigneeName = doc.getString("assigneeName");
            String dueDate = doc.getString("dueDate");
            Boolean isCompleted = doc.getBoolean("isCompleted");
            String projectId = doc.getString("projectId");
            String createdBy = doc.getString("createdBy");

            return new Task(
                    taskId,
                    taskName,
                    priority,
                    assigneeId,
                    assigneeName,
                    dueDate,
                    isCompleted != null && isCompleted,
                    projectId,
                    createdBy
            );
        } catch (Exception e) {
            Log.e(TAG, "Error parsing task document", e);
            return null;
        }
    }

    private FileModel documentToFile(DocumentSnapshot doc) {
        try {
            String name = doc.getString("name");
            String url = doc.getString("url");
            Long uploadedAt = doc.getLong("uploadedAt");

            return new FileModel(name, url, uploadedAt != null ? uploadedAt : 0);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing file document", e);
            return null;
        }
    }

    // ==================== CALLBACK INTERFACE ====================

    public interface OnCompleteListener {
        void onComplete(boolean success, String message);
    }
}