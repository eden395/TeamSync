package com.student.teamsync.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.student.teamsync.R;
import com.student.teamsync.activities.AddTaskActivity;
import com.student.teamsync.adapters.TaskAdapter;
import com.student.teamsync.models.Project;
import com.student.teamsync.models.Task;
import com.student.teamsync.utils.FirestoreHelper;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {

    private Spinner spinnerTaskView;
    private RecyclerView tasksRecyclerView;
    private MaterialButton addTaskButton;
    private TaskAdapter taskAdapter;
    private List<Task> allTasks;
    private List<Task> filteredTasks;
    private ActivityResultLauncher<Intent> addTaskLauncher;

    private Project currentProject;
    private String currentUserId;
    private boolean isUserLeader = false;
    private String selectedView = "My Tasks";
    private FirestoreHelper firestoreHelper;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firestoreHelper = new FirestoreHelper();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Get project data passed from MainActivity
        if (getActivity() != null && getActivity().getIntent() != null) {
            currentProject = (Project) getActivity().getIntent().getSerializableExtra("project");
            if (currentProject != null) {
                isUserLeader = currentProject.isLeader(currentUserId);
            }
        }

        // Register activity result launcher for AddTaskActivity
        addTaskLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Task task = (Task) result.getData().getSerializableExtra("task");
                        boolean isEdit = result.getData().getBooleanExtra("isEdit", false);

                        if (task != null) {
                            progressDialog.setMessage(isEdit ? "Updating task..." : "Creating task...");
                            progressDialog.show();

                            if (isEdit) {
                                firestoreHelper.updateTask(task, new FirestoreHelper.OnCompleteListener() {
                                    @Override
                                    public void onComplete(boolean success, String message) {
                                        progressDialog.dismiss();
                                        if (success) {
                                            Toast.makeText(getContext(), "Task updated successfully",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), "Error: " + message,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                firestoreHelper.createTask(task, new FirestoreHelper.OnCompleteListener() {
                                    @Override
                                    public void onComplete(boolean success, String message) {
                                        progressDialog.dismiss();
                                        if (success) {
                                            Toast.makeText(getContext(), "Task added successfully",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), "Error: " + message,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        initializeViews(view);
        setupViewSpinner();
        setupRecyclerView();
        loadTasksFromFirestore();

        return view;
    }

    private void initializeViews(View view) {
        spinnerTaskView = view.findViewById(R.id.spinnerTaskView);
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        addTaskButton = view.findViewById(R.id.addTaskButton);

        // Only leaders can add tasks
        if (isUserLeader) {
            addTaskButton.setVisibility(View.VISIBLE);
            addTaskButton.setOnClickListener(v -> openAddTaskActivity(null, -1));
        } else {
            addTaskButton.setVisibility(View.GONE);
        }
    }

    private void setupViewSpinner() {
        // Create view options
        String[] viewOptions = {"My Tasks", "All Tasks"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                viewOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTaskView.setAdapter(adapter);

        // Set listener for view changes
        spinnerTaskView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedView = viewOptions[position];
                filterTasks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupRecyclerView() {
        allTasks = new ArrayList<>();
        filteredTasks = new ArrayList<>();

        taskAdapter = new TaskAdapter(filteredTasks, currentUserId, isUserLeader,
                new TaskAdapter.OnTaskInteractionListener() {
                    @Override
                    public void onTaskChecked(Task task, int position, boolean isChecked) {
                        // Update task completion status
                        task.setCompleted(isChecked);

                        firestoreHelper.updateTask(task, new FirestoreHelper.OnCompleteListener() {
                            @Override
                            public void onComplete(boolean success, String message) {
                                if (success) {
                                    Toast.makeText(getContext(),
                                            isChecked ? "Task completed" : "Task uncompleted",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Error: " + message,
                                            Toast.LENGTH_SHORT).show();
                                    task.setCompleted(!isChecked); // Revert on error
                                    taskAdapter.notifyItemChanged(position);
                                }
                            }
                        });
                    }

                    @Override
                    public void onTaskEdit(Task task, int position) {
                        openAddTaskActivity(task, position);
                    }

                    @Override
                    public void onTaskDelete(Task task, int position) {
                        progressDialog.setMessage("Deleting task...");
                        progressDialog.show();

                        firestoreHelper.deleteTask(task.getTaskId(), new FirestoreHelper.OnCompleteListener() {
                            @Override
                            public void onComplete(boolean success, String message) {
                                progressDialog.dismiss();
                                if (success) {
                                    Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Error: " + message,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tasksRecyclerView.setAdapter(taskAdapter);
    }

    private void loadTasksFromFirestore() {
        if (currentProject == null) return;

        progressDialog.show();

        firestoreHelper.getProjectTasks(currentProject.getProjectId(), new FirestoreHelper.TaskCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                progressDialog.dismiss();
                allTasks.clear();
                allTasks.addAll(tasks);
                filterTasks();
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Error loading tasks: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterTasks() {
        filteredTasks.clear();

        if ("My Tasks".equals(selectedView)) {
            // Show only tasks assigned to current user
            for (Task task : allTasks) {
                if (currentUserId.equals(task.getAssigneeId())) {
                    filteredTasks.add(task);
                }
            }
        } else {
            // Show all tasks
            filteredTasks.addAll(allTasks);
        }

        taskAdapter.notifyDataSetChanged();
    }

    private void openAddTaskActivity(Task task, int position) {
        Intent intent = new Intent(getActivity(), AddTaskActivity.class);
        intent.putExtra("project", currentProject);
        if (task != null) {
            intent.putExtra("task", task);
        }
        addTaskLauncher.launch(intent);
    }
}