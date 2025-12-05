package com.example.studentprojecttracker.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.models.Task;
import com.example.studentprojecttracker.models.Project;
import com.example.studentprojecttracker.models.User;
import com.example.studentprojecttracker.utils.FirestoreHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddTaskActivity extends AppCompatActivity {

    private TextInputEditText etTaskName, etDueDate, etAdditionalNotes;
    private Spinner spinnerPriority, spinnerAssignTo;
    private MaterialButton btnSave;
    private ImageView btnBack;

    private Project currentProject;
    private Task editingTask;
    private boolean isEditMode = false;
    private List<String> memberEmails;
    private Map<String, String> emailToNameMap; // Map email to display name
    private FirestoreHelper firestoreHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        firestoreHelper = new FirestoreHelper();
        emailToNameMap = new HashMap<>();

        // Get project and task data
        currentProject = (Project) getIntent().getSerializableExtra("project");
        editingTask = (Task) getIntent().getSerializableExtra("task");
        isEditMode = editingTask != null;

        initializeViews();
        setupSpinners();

        if (isEditMode) {
            loadTaskData();
            setTitle("Edit Task");
        } else {
            setTitle("Add Task");
        }
    }

    private void initializeViews() {
        etTaskName = findViewById(R.id.etTaskName);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerAssignTo = findViewById(R.id.spinnerAssignTo);
        etDueDate = findViewById(R.id.etDueDate);
        etAdditionalNotes = findViewById(R.id.etAdditionalNotes);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        // Set up date picker for due date field
        etDueDate.setOnClickListener(v -> showDatePicker());

        // Save button click
        btnSave.setOnClickListener(v -> saveTask());

        // Back button click
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupSpinners() {
        // Priority spinner
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.priority_levels,
                android.R.layout.simple_spinner_item
        );
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);

        // Assign to spinner - populate with project members
        memberEmails = new ArrayList<>();
        if (currentProject != null && currentProject.getMemberIds() != null) {
            memberEmails.addAll(currentProject.getMemberIds());
        }

        // Load display names for all members
        loadMemberDisplayNames();
    }

    /**
     * Load display names from Firestore for all project members
     */
    private void loadMemberDisplayNames() {
        List<String> displayNames = new ArrayList<>();
        displayNames.add("Unassigned");

        if (memberEmails.isEmpty()) {
            updateAssigneeSpinner(displayNames);
            return;
        }

        final int[] loadedCount = {0};
        final int totalMembers = memberEmails.size();

        for (String email : memberEmails) {
            firestoreHelper.getUserProfileByEmail(email, new FirestoreHelper.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    String displayName = user.getName();
                    if (displayName == null || displayName.isEmpty() || displayName.contains("@")) {
                        // Fallback to email username
                        displayName = extractUsernameFromEmail(email);
                    }

                    emailToNameMap.put(email, displayName);
                    displayNames.add(displayName);
                    loadedCount[0]++;

                    if (loadedCount[0] == totalMembers) {
                        updateAssigneeSpinner(displayNames);
                    }
                }

                @Override
                public void onError(String error) {
                    // Use email username as fallback
                    String displayName = extractUsernameFromEmail(email);
                    emailToNameMap.put(email, displayName);
                    displayNames.add(displayName);
                    loadedCount[0]++;

                    if (loadedCount[0] == totalMembers) {
                        updateAssigneeSpinner(displayNames);
                    }
                }
            });
        }
    }

    /**
     * Update assignee spinner with display names
     */
    private void updateAssigneeSpinner(List<String> displayNames) {
        runOnUiThread(() -> {
            ArrayAdapter<String> memberAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    displayNames
            );
            memberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerAssignTo.setAdapter(memberAdapter);

            // If editing, set the current assignee
            if (isEditMode && editingTask != null) {
                loadTaskData();
            }
        });
    }

    /**
     * Extract username from email and capitalize
     */
    private String extractUsernameFromEmail(String email) {
        if (email != null && email.contains("@")) {
            String username = email.substring(0, email.indexOf("@"));
            if (!username.isEmpty()) {
                return username.substring(0, 1).toUpperCase() + username.substring(1);
            }
            return username;
        }
        return email;
    }

    private void loadTaskData() {
        if (editingTask != null) {
            etTaskName.setText(editingTask.getTaskName());
            etDueDate.setText(editingTask.getDueDate());

            // Set priority
            String priority = editingTask.getPriority();
            if (priority != null) {
                String[] priorities = getResources().getStringArray(R.array.priority_levels);
                for (int i = 0; i < priorities.length; i++) {
                    if (priorities[i].equals(priority)) {
                        spinnerPriority.setSelection(i);
                        break;
                    }
                }
            }

            // Set assignee by finding the display name
            String assigneeId = editingTask.getAssigneeId();
            if (assigneeId != null && emailToNameMap.containsKey(assigneeId)) {
                String displayName = emailToNameMap.get(assigneeId);

                // Find position in spinner
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerAssignTo.getAdapter();
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (adapter.getItem(i).equals(displayName)) {
                        spinnerAssignTo.setSelection(i);
                        break;
                    }
                }
            }

            btnSave.setText("Update Task");
        }
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (DatePicker view, int y, int m, int d) -> {
                    String date = (m + 1) + "/" + d + "/" + y;
                    etDueDate.setText(date);
                },
                year, month, day
        );
        datePicker.show();
    }

    private void saveTask() {
        String name = etTaskName.getText().toString().trim();
        String due = etDueDate.getText().toString().trim();
        String notes = etAdditionalNotes.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            etTaskName.setError("Task name required");
            etTaskName.requestFocus();
            return;
        }

        if (due.isEmpty()) {
            etDueDate.setError("Due date required");
            etDueDate.requestFocus();
            return;
        }

        // Get selected priority
        String priority = spinnerPriority.getSelectedItem().toString();

        // Get selected assignee
        int assigneePosition = spinnerAssignTo.getSelectedItemPosition();
        String assigneeId = null;
        String assigneeName = "Unassigned";

        if (assigneePosition > 0) { // 0 is "Unassigned"
            // Get email from memberEmails list
            assigneeId = memberEmails.get(assigneePosition - 1);

            // Get display name from map
            assigneeName = emailToNameMap.get(assigneeId);
            if (assigneeName == null || assigneeName.isEmpty()) {
                assigneeName = extractUsernameFromEmail(assigneeId);
            }

            Log.d("AddTaskActivity", "Saving task - AssigneeId: " + assigneeId + ", AssigneeName: " + assigneeName);
        }

        // Get current user ID
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        Task task;
        if (isEditMode) {
            // Update existing task
            task = editingTask;
            task.setTaskName(name);
            task.setPriority(priority);
            task.setAssigneeId(assigneeId);
            task.setAssigneeName(assigneeName);
            task.setDueDate(due);
        } else {
            // Create new task
            String taskId = UUID.randomUUID().toString();
            String projectId = currentProject != null ? currentProject.getProjectId() : null;

            task = new Task(
                    taskId,
                    name,
                    priority,
                    assigneeId,
                    assigneeName,
                    due,
                    false,
                    projectId,
                    currentUserId
            );
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("task", task);
        resultIntent.putExtra("isEdit", isEditMode);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }
}