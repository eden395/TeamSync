package com.student.teamsync.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.student.teamsync.R;
import com.student.teamsync.models.Task;
import com.student.teamsync.models.Project;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class AddTaskActivity extends AppCompatActivity {

    private TextInputEditText etTaskName, etDueDate, etAdditionalNotes;
    private Spinner spinnerPriority, spinnerAssignTo;
    private MaterialButton btnSave;
    private ImageView btnBack;

    private Project currentProject;
    private Task editingTask; // null if creating new task
    private boolean isEditMode = false;
    private List<String> memberEmails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

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

        // Add "Unassigned" option
        List<String> displayNames = new ArrayList<>();
        displayNames.add("Unassigned");
        displayNames.addAll(memberEmails);

        ArrayAdapter<String> memberAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                displayNames
        );
        memberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAssignTo.setAdapter(memberAdapter);
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

            // Set assignee
            String assigneeId = editingTask.getAssigneeId();
            if (assigneeId != null) {
                int position = memberEmails.indexOf(assigneeId) + 1; // +1 for "Unassigned"
                if (position > 0) {
                    spinnerAssignTo.setSelection(position);
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
            assigneeId = memberEmails.get(assigneePosition - 1);
            assigneeName = assigneeId; // You can replace this with actual names later
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