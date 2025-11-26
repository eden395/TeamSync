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
import android.widget.Spinner;
import android.widget.Toast;

import com.student.teamsync.R;
import com.student.teamsync.models.Task;

import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    private EditText inputTaskName, inputAssignedTo, inputDueDate;
    private Spinner prioritySpinner;
    private Button btnSaveTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        inputTaskName = findViewById(R.id.inputTaskName);
        inputAssignedTo = findViewById(R.id.inputAssignedTo);
        inputDueDate = findViewById(R.id.inputDueDate);
        prioritySpinner = findViewById(R.id.prioritySpinner);
        btnSaveTask = findViewById(R.id.btnSaveTask);

        // Priority dropdown values
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"High", "Medium", "Low"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter);

        inputDueDate.setOnClickListener(v -> showDatePicker());

        btnSaveTask.setOnClickListener(v -> saveTask());
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
                    inputDueDate.setText(date);
                },
                year, month, day
        );
        datePicker.show();
    }

    private void saveTask() {
        String name = inputTaskName.getText().toString().trim();
        String assigned = inputAssignedTo.getText().toString().trim();
        String due = inputDueDate.getText().toString().trim();
        String priority = prioritySpinner.getSelectedItem().toString();

        if (name.isEmpty()) {
            inputTaskName.setError("Task name required");
            inputTaskName.requestFocus();
            return;
        }

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

    // optional: provide friendly behaviour if user cancels
    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }
}
