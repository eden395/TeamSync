package com.student.teamsync.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.student.teamsync.R;
import com.student.teamsync.adapters.ProjectAdapter;
import com.student.teamsync.models.Project;
import com.student.teamsync.utils.FirestoreHelper;
import com.student.teamsync.utils.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class ProjectsDashboardActivity extends AppCompatActivity implements ProjectAdapter.OnProjectClickListener {

    private RecyclerView rvProjects;
    private FloatingActionButton fabAddProject;
    private MaterialButton btnProjects, btnReminders;
    private ImageView btnLogout, btnProfile;
    private ProjectAdapter projectAdapter;
    private List<Project> projectList;
    private SessionManager sessionManager;
    private FirestoreHelper firestoreHelper;
    private boolean isAdvisorMode;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects_dashboard);

        sessionManager = new SessionManager(this);
        firestoreHelper = new FirestoreHelper();
        String userRole = sessionManager.getUserRole();
        isAdvisorMode = getString(R.string.role_advisor).equals(userRole);

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadProjectsFromFirestore();

        // Hide FAB for advisors
        if (isAdvisorMode) {
            fabAddProject.setVisibility(View.GONE);
        }
    }

    private void initializeViews() {
        rvProjects = findViewById(R.id.rvProjects);
        fabAddProject = findViewById(R.id.fabAddProject);
        btnProjects = findViewById(R.id.btnProjects);
        btnReminders = findViewById(R.id.btnReminders);
        btnLogout = findViewById(R.id.btnLogout);
        btnProfile = findViewById(R.id.btnProfile);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        // Profile button click
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        projectList = new ArrayList<>();
        projectAdapter = new ProjectAdapter(projectList, this, isAdvisorMode);
        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        rvProjects.setAdapter(projectAdapter);
    }

    private void loadProjectsFromFirestore() {
        progressDialog.show();
        String currentUserId = sessionManager.getUserEmail();

        firestoreHelper.getUserProjects(currentUserId, isAdvisorMode, new FirestoreHelper.ProjectCallback() {
            @Override
            public void onSuccess(List<Project> projects) {
                progressDialog.dismiss();
                projectList.clear();
                projectList.addAll(projects);
                projectAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                Toast.makeText(ProjectsDashboardActivity.this,
                        "Error loading projects: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        fabAddProject.setOnClickListener(v -> showAddProjectDialog());

        btnLogout.setOnClickListener(v -> logout());

        btnReminders.setOnClickListener(v -> {
            Toast.makeText(this, R.string.reminders_coming_soon, Toast.LENGTH_SHORT).show();
        });
    }

    private void showAddProjectDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_project, null);

        TextInputEditText etProjectName = dialogView.findViewById(R.id.etProjectName);
        TextInputEditText etCourseCode = dialogView.findViewById(R.id.etCourseCode);
        TextInputEditText etProjectDueDate = dialogView.findViewById(R.id.etProjectDueDate);

        // Date picker for due date
        etProjectDueDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        String date = (month + 1) + "/" + dayOfMonth + "/" + year;
                        etProjectDueDate.setText(date);
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnCancelProject).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnCreateProject).setOnClickListener(v -> {
            String name = etProjectName.getText().toString().trim();
            String code = etCourseCode.getText().toString().trim();
            String dueDate = etProjectDueDate.getText().toString().trim();

            if (name.isEmpty() || code.isEmpty() || dueDate.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.setMessage("Creating project...");
            progressDialog.show();

            String projectId = UUID.randomUUID().toString();
            String currentUserId = sessionManager.getUserEmail();

            Project newProject = new Project(projectId, name, code, dueDate, currentUserId);

            firestoreHelper.createProject(newProject, new FirestoreHelper.OnCompleteListener() {
                @Override
                public void onComplete(boolean success, String message) {
                    progressDialog.dismiss();
                    if (success) {
                        Toast.makeText(ProjectsDashboardActivity.this,
                                "Project created successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        // No need to manually add - Firestore listener will update the list
                    } else {
                        Toast.makeText(ProjectsDashboardActivity.this,
                                "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        dialog.show();
    }

    private void showEditProjectDialog(Project project) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_project, null);

        TextInputEditText etProjectName = dialogView.findViewById(R.id.etProjectName);
        TextInputEditText etCourseCode = dialogView.findViewById(R.id.etCourseCode);
        TextInputEditText etProjectDueDate = dialogView.findViewById(R.id.etProjectDueDate);
        MaterialButton btnCreate = dialogView.findViewById(R.id.btnCreateProject);

        // Pre-fill with existing data
        etProjectName.setText(project.getProjectName());
        etCourseCode.setText(project.getCourseCode());
        etProjectDueDate.setText(project.getDueDate());

        // Change button text
        btnCreate.setText("Update Project");

        // Date picker for due date
        etProjectDueDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        String date = (month + 1) + "/" + dayOfMonth + "/" + year;
                        etProjectDueDate.setText(date);
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnCancelProject).setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String name = etProjectName.getText().toString().trim();
            String code = etCourseCode.getText().toString().trim();
            String dueDate = etProjectDueDate.getText().toString().trim();

            if (name.isEmpty() || code.isEmpty() || dueDate.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.setMessage("Updating project...");
            progressDialog.show();

            // Update project
            project.setProjectName(name);
            project.setCourseCode(code);
            project.setDueDate(dueDate);

            firestoreHelper.updateProject(project, new FirestoreHelper.OnCompleteListener() {
                @Override
                public void onComplete(boolean success, String message) {
                    progressDialog.dismiss();
                    if (success) {
                        Toast.makeText(ProjectsDashboardActivity.this,
                                "Project updated successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(ProjectsDashboardActivity.this,
                                "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        dialog.show();
    }

    @Override
    public void onProjectClick(Project project) {
        if (isAdvisorMode) {
            // Navigate to advisor view
            Intent intent = new Intent(this, AdvisorProjectViewActivity.class);
            intent.putExtra("project", project);
            startActivity(intent);
        } else {
            // Navigate to project details (MainActivity with tabs)
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("project", project);
            startActivity(intent);
        }
    }

    @Override
    public void onProjectMenuClick(Project project, View view) {
        PopupMenu popup = new PopupMenu(this, view);

        if (isAdvisorMode) {
            popup.getMenu().add("View Details");
        } else {
            popup.getMenu().add("Edit Project");
            popup.getMenu().add("Add Member");
            popup.getMenu().add("Add Mentor");
            popup.getMenu().add("Delete Project");
        }

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            switch (title) {
                case "Edit Project":
                    showEditProjectDialog(project);
                    break;
                case "Add Member":
                    showAddMemberDialog(project);
                    break;
                case "Add Mentor":
                    showAddMentorDialog(project);
                    break;
                case "Delete Project":
                    deleteProject(project);
                    break;
                case "View Details":
                    onProjectClick(project);
                    break;
            }
            return true;
        });

        popup.show();
    }

    private void showAddMemberDialog(Project project) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_member, null);

        TextInputEditText etMemberEmail = dialogView.findViewById(R.id.etMemberEmail);
        TextInputEditText etMemberName = dialogView.findViewById(R.id.etMemberName);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            String email = etMemberEmail.getText().toString().trim();
            String name = etMemberName.getText().toString().trim();

            if (email.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.setMessage("Adding member...");
            progressDialog.show();

            // Add member to project
            project.getMemberIds().add(email);

            firestoreHelper.updateProject(project, new FirestoreHelper.OnCompleteListener() {
                @Override
                public void onComplete(boolean success, String message) {
                    progressDialog.dismiss();
                    if (success) {
                        Toast.makeText(ProjectsDashboardActivity.this,
                                name + " added to project", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(ProjectsDashboardActivity.this,
                                "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        dialog.show();
    }

    private void showAddMentorDialog(Project project) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_member, null);

        TextInputEditText etMemberEmail = dialogView.findViewById(R.id.etMemberEmail);
        TextInputEditText etMemberName = dialogView.findViewById(R.id.etMemberName);
        MaterialButton btnAdd = dialogView.findViewById(R.id.btnAdd);

        etMemberEmail.setHint("Mentor Email");
        etMemberName.setHint("Mentor Name");
        btnAdd.setText("Add Mentor");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            String email = etMemberEmail.getText().toString().trim();
            String name = etMemberName.getText().toString().trim();

            if (email.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.setMessage("Adding mentor...");
            progressDialog.show();

            // Add mentor to project
            project.getMentorIds().add(email);

            firestoreHelper.updateProject(project, new FirestoreHelper.OnCompleteListener() {
                @Override
                public void onComplete(boolean success, String message) {
                    progressDialog.dismiss();
                    if (success) {
                        Toast.makeText(ProjectsDashboardActivity.this,
                                name + " added as mentor", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(ProjectsDashboardActivity.this,
                                "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        dialog.show();
    }

    private void deleteProject(Project project) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Project")
                .setMessage("Are you sure you want to delete " + project.getProjectName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    progressDialog.setMessage("Deleting project...");
                    progressDialog.show();

                    firestoreHelper.deleteProject(project.getProjectId(),
                            new FirestoreHelper.OnCompleteListener() {
                                @Override
                                public void onComplete(boolean success, String message) {
                                    progressDialog.dismiss();
                                    if (success) {
                                        Toast.makeText(ProjectsDashboardActivity.this,
                                                "Project deleted", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ProjectsDashboardActivity.this,
                                                "Error: " + message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        sessionManager.logout();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}