package com.student.teamsync.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.student.teamsync.R;
import com.student.teamsync.activities.AddTaskActivity;
import com.student.teamsync.adapters.TaskAdapter;
import com.student.teamsync.models.Task;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {

    private RecyclerView tasksRecyclerView;
    private MaterialButton addTaskButton;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private ActivityResultLauncher<Intent> addTaskLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register activity result launcher for AddTaskActivity
        addTaskLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Task newTask = (Task) result.getData().getSerializableExtra("task");
                        if (newTask != null) {
                            taskList.add(0, newTask);
                            taskAdapter.notifyItemInserted(0);
                            tasksRecyclerView.scrollToPosition(0);
                            Toast.makeText(getContext(), "Task added successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        initializeViews(view);
        setupRecyclerView();
        loadSampleTasks();

        return view;
    }

    private void initializeViews(View view) {
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        addTaskButton = view.findViewById(R.id.addTaskButton);

        addTaskButton.setOnClickListener(v -> openAddTaskActivity());
    }

    private void setupRecyclerView() {
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tasksRecyclerView.setAdapter(taskAdapter);
    }

    private void loadSampleTasks() {
        // Sample tasks based on the PDF design
        taskList.add(new Task("Design user interface mockups", "High", "Christine", "10/5/2025", false));
        taskList.add(new Task("Write Proposal Project", "Medium", "Eden", "10/5/2025", true));
        taskList.add(new Task("Create presentation slides", "Medium", "Irish", "10/5/2025", true));
        taskList.add(new Task("Conduct user interviews", "Low", "Aliyah", "10/5/2025", false));

        taskAdapter.notifyDataSetChanged();
    }

    private void openAddTaskActivity() {
        Intent intent = new Intent(getActivity(), AddTaskActivity.class);
        addTaskLauncher.launch(intent);
    }
}