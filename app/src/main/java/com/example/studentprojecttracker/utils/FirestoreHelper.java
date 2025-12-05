package com.example.studentprojecttracker.utils;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.studentprojecttracker.models.Note;
import com.example.studentprojecttracker.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.example.studentprojecttracker.models.Project;
import com.example.studentprojecttracker.models.Task;
import com.example.studentprojecttracker.models.FileModel;

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
                    if (listener != null) {
                        listener.onComplete(true, "Project created successfully");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating project", e);
                    if (listener != null) {
                        listener.onComplete(false, e.getMessage());
                    }
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
                    if (listener != null) {
                        listener.onComplete(true, "Project updated successfully");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating project", e);
                    if (listener != null) {
                        listener.onComplete(false, e.getMessage());
                    }
                });
    }

    // Add this method in the PROJECT OPERATIONS section of FirestoreHelper.java

    public interface OperationCallback {
        void onSuccess();
        void onError(String error);
    }

    public void updateProjectMembers(String projectId, List<String> memberIds, OperationCallback callback) {
        db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .update("memberIds", memberIds)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Project members updated successfully");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating project members", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    public void updateProjectLeaders(String projectId, List<String> leaderIds, OperationCallback callback) {
        db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .update("leaderIds", leaderIds)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Project leaders updated successfully");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating project leaders", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
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
                                if (listener != null) {
                                    listener.onComplete(true, "Project deleted successfully");
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting project", e);
                                if (listener != null) {
                                    listener.onComplete(false, e.getMessage());
                                }
                            });
                });
    }

    public ListenerRegistration getUserProjects(String userId, boolean isAdvisor, ProjectCallback callback) {
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

        return query.addSnapshotListener((querySnapshot, error) -> {
            if (error != null) {
                Log.e(TAG, "Error getting projects", error);
                if (callback != null) {
                    callback.onError(error.getMessage());
                }
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
            if (callback != null) {
                callback.onSuccess(projects);
            }
        });
    }

    public void getProject(String projectId, SingleProjectCallback callback) {
        db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Project project = documentToProject(documentSnapshot);
                        if (callback != null) {
                            callback.onSuccess(project);
                        }
                    } else {
                        if (callback != null) {
                            callback.onError("Project not found");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting project", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
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
                    if (listener != null) {
                        listener.onComplete(true, "Task created successfully");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating task", e);
                    if (listener != null) {
                        listener.onComplete(false, e.getMessage());
                    }
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
                    if (listener != null) {
                        listener.onComplete(true, "Task updated successfully");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating task", e);
                    if (listener != null) {
                        listener.onComplete(false, e.getMessage());
                    }
                });
    }

    public void deleteTask(String taskId, OnCompleteListener listener) {
        db.collection(COLLECTION_TASKS)
                .document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Task deleted successfully");
                    if (listener != null) {
                        listener.onComplete(true, "Task deleted successfully");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting task", e);
                    if (listener != null) {
                        listener.onComplete(false, e.getMessage());
                    }
                });
    }

    public ListenerRegistration getProjectTasks(String projectId, TaskCallback callback) {
        return db.collection(COLLECTION_TASKS)
                .whereEqualTo("projectId", projectId)
                // Removed orderBy - will add back after creating index
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting tasks", error);
                        if (callback != null) {
                            callback.onError(error.getMessage());
                        }
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
                    if (callback != null) {
                        callback.onSuccess(tasks);
                    }
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
                    if (listener != null) {
                        listener.onComplete(true, "File uploaded successfully");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving file metadata", e);
                    if (listener != null) {
                        listener.onComplete(false, e.getMessage());
                    }
                });
    }

    public ListenerRegistration getProjectFiles(String projectId, FileCallback callback) {
        return db.collection(COLLECTION_FILES)
                .whereEqualTo("projectId", projectId)
                // Removed orderBy - will add back after creating index
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error getting files", error);
                        if (callback != null) {
                            callback.onError(error.getMessage());
                        }
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
                    if (callback != null) {
                        callback.onSuccess(files);
                    }
                });
    }

    // ==================== USER OPERATIONS ====================

    public interface UserCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public void saveUserProfile(User user, OnCompleteListener listener) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getUserId());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());

        // Add photoUrl if it exists
        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            userData.put("photoUrl", user.getPhotoUrl());
        }

        userData.put("updatedAt", System.currentTimeMillis());

        db.collection(COLLECTION_USERS)
                .document(user.getUserId())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile saved successfully");
                    if (listener != null) {
                        listener.onComplete(true, "Profile saved successfully");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user profile", e);
                    if (listener != null) {
                        listener.onComplete(false, e.getMessage());
                    }
                });
    }


    public void getUserProfile(String userId, UserCallback callback) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            String id = documentSnapshot.getString("userId");
                            String name = documentSnapshot.getString("name");
                            String email = documentSnapshot.getString("email");
                            String role = documentSnapshot.getString("role");
                            String photoUrl = documentSnapshot.getString("photoUrl");

                            User user = new User(id, name, email, role, photoUrl);
                            if (callback != null) {
                                callback.onSuccess(user);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user document", e);
                            if (callback != null) {
                                callback.onError(e.getMessage());
                            }
                        }
                    } else {
                        if (callback != null) {
                            callback.onError("User not found");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user profile", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    /**
     * Get user profile by email (queries the email field instead of document ID)
     */
    public void getUserProfileByEmail(String email, UserCallback callback) {
        db.collection(COLLECTION_USERS)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        try {
                            String id = doc.getString("userId");
                            String name = doc.getString("name");
                            String userEmail = doc.getString("email");
                            String role = doc.getString("role");
                            String photoUrl = doc.getString("photoUrl");

                            User user = new User(id, name, userEmail, role, photoUrl);
                            if (callback != null) {
                                callback.onSuccess(user);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing user document", e);
                            if (callback != null) {
                                callback.onError(e.getMessage());
                            }
                        }
                    } else {
                        if (callback != null) {
                            callback.onError("User not found");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user profile by email", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    // ==================== NOTE OPERATIONS ====================
    public interface NoteCallback {
        void onSuccess(List<Note> notes);
        void onError(String error);
    }

    public void addProjectNote(String projectId, Note note, @Nullable OnCompleteListener listener) {
        db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .collection("notes")
                .document(note.getNoteId())
                .set(note)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onComplete(true, "Note saved successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving note", e);
                    if (listener != null) listener.onComplete(false, e.getMessage());
                });
    }

    /**
     * Delete a note
     */
    public void deleteProjectNote(String projectId, String noteId, @Nullable OnCompleteListener listener) {
        db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .collection("notes")
                .document(noteId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onComplete(true, "Note deleted successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting note", e);
                    if (listener != null) listener.onComplete(false, e.getMessage());
                });
    }

    /**
     * Listen for real-time notes
     */
    public ListenerRegistration getProjectNotes(String projectId, NoteCallback callback) {
        Query query = db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .collection("notes")
                .orderBy("timestamp");

        return query.addSnapshotListener((querySnapshot, error) -> {
            if (error != null) {
                if (callback != null) callback.onError(error.getMessage());
                return;
            }

            List<Note> notes = new ArrayList<>();
            if (querySnapshot != null) {
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Note note = doc.toObject(Note.class);
                    if (note != null) notes.add(note);
                }
            }

            if (callback != null) callback.onSuccess(notes);
        });
    }


    // ==================== HELPER METHOD FOR TYPE-SAFE LIST CONVERSION ====================

    @SuppressWarnings("unchecked")
    private List<String> getStringList(DocumentSnapshot doc, String field) {
        Object obj = doc.get(field);
        if (obj instanceof List<?>) {
            List<?> list = (List<?>) obj;
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof String) {
                    result.add((String) item);
                }
            }
            return result;
        }
        return new ArrayList<>();
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

            // Use helper method for type-safe list conversion
            List<String> leaderIds = getStringList(doc, "leaderIds");
            if (!leaderIds.isEmpty()) {
                project.setLeaderIds(leaderIds);
            }

            List<String> memberIds = getStringList(doc, "memberIds");
            if (!memberIds.isEmpty()) {
                project.setMemberIds(memberIds);
            }

            List<String> mentorIds = getStringList(doc, "mentorIds");
            if (!mentorIds.isEmpty()) {
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