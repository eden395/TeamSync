package com.student.teamsync.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.student.teamsync.R;
import com.student.teamsync.models.Task;

import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    private TextInputEditText etTaskName, etAssignedTo, etDueDate, etAdditionalNotes;
    private MaterialButton btnSave;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize views with correct IDs from XML
        etTaskName = findViewById(R.id.etTaskName);
        etAssignedTo = findViewById(R.id.etAssignedTo);
        etDueDate = findViewById(R.id.etDueDate);
        etAdditionalNotes = findViewById(R.id.etAdditionalNotes);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        // Set up date picker for due date field
        etDueDate.setOnClickListener(v -> showDatePicker());

        // You can also click the calendar icon on the TextInputLayout
        findViewById(R.id.tilDueDate).setOnClickListener(v -> showDatePicker());

        // Save button click
        btnSave.setOnClickListener(v -> saveTask());

        // Back button click
        btnBack.setOnClickListener(v -> onBackPressed());
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
        String assigned = etAssignedTo.getText().toString().trim();
        String due = etDueDate.getText().toString().trim();
        String notes = etAdditionalNotes.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            etTaskName.setError("Task name required");
            etTaskName.requestFocus();
            return;
        }

        // Note: Your XML doesn't have a priority spinner, so I'm setting default priority as "Medium"
        // If you need priority selection, you'll need to add a Spinner to your XML
        String priority = "Medium";

        Task task = new Task(
                name,
                priority,
                assigned,
                due,
                false // initially not completed
        );

        Intent resultIntent = new Intent();
        resultIntent.putExtra("task", task);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }
}