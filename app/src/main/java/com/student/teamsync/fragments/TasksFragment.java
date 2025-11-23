package com.student.teamsync.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.student.teamsync.R;
import com.student.teamsync.adapters.TaskAdapter;
import com.student.teamsync.models.Task;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {

    private RecyclerView tasksRecyclerView;
    private MaterialButton addTaskButton;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

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
        
        addTaskButton.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Add Task feature coming soon", Toast.LENGTH_SHORT).show()
        );
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
}
