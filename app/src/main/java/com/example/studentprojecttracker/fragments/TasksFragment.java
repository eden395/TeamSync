package com.example.studentprojecttracker.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.example.studentprojecttracker.activities.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.activities.AddTaskActivity;
import com.example.studentprojecttracker.adapters.TaskAdapter;
import com.example.studentprojecttracker.models.Project;
import com.example.studentprojecttracker.models.Task;
import com.example.studentprojecttracker.utils.FirestoreHelper;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {

    private static final String TAG = "TasksFragment";

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
    private ListenerRegistration tasksListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firestoreHelper = new FirestoreHelper();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Get project data from MainActivity
        if (getActivity() instanceof MainActivity) {
            currentProject = ((MainActivity) getActivity()).getCurrentProject();
            if (currentProject != null) {
                Log.d(TAG, "Project received: " + currentProject.getProjectName());
                isUserLeader = currentProject.isLeader(currentUserId);
            } else {
                Log.e(TAG, "No project available in MainActivity!");
            }
        }

        // Register activity result launcher for AddTaskActivity
        addTaskLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Task task = (Task) result.getData().getSerializableExtra("task");
                        boolean isEdit = result.getData().getBooleanExtra("isEdit", false);

                        if (task != null && isAdded() && getContext() != null) {
                            progressDialog.setMessage(isEdit ? "Updating task..." : "Creating task...");
                            progressDialog.show();

                            if (isEdit) {
                                firestoreHelper.updateTask(task, new FirestoreHelper.OnCompleteListener() {
                                    @Override
                                    public void onComplete(boolean success, String message) {
                                        if (!isAdded() || getContext() == null) return;

                                        if (progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }

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
                                        if (!isAdded() || getContext() == null) return;

                                        if (progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }

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

        initializeViews(view);
        setupViewSpinner();
        setupRecyclerView();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadTasksFromFirestore();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove Firestore listener when fragment is destroyed
        if (tasksListener != null) {
            tasksListener.remove();
            tasksListener = null;
        }
        // Dismiss progress dialog if showing
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void initializeViews(View view) {
        spinnerTaskView = view.findViewById(R.id.spinnerTaskView);
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        addTaskButton = view.findViewById(R.id.addTaskButton);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

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
                        if (!isAdded() || getContext() == null) return;

                        // Update task completion status
                        task.setCompleted(isChecked);

                        firestoreHelper.updateTask(task, new FirestoreHelper.OnCompleteListener() {
                            @Override
                            public void onComplete(boolean success, String message) {
                                if (!isAdded() || getContext() == null) return;

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
                        if (!isAdded() || getContext() == null) return;

                        progressDialog.setMessage("Deleting task...");
                        progressDialog.show();

                        firestoreHelper.deleteTask(task.getTaskId(), new FirestoreHelper.OnCompleteListener() {
                            @Override
                            public void onComplete(boolean success, String message) {
                                if (!isAdded() || getContext() == null) return;

                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }

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
        if (currentProject == null) {
            Log.e(TAG, "No current project");
            return;
        }

        if (!isAdded() || getContext() == null) {
            Log.e(TAG, "Fragment not attached");
            return;
        }

        // Remove old listener if exists
        if (tasksListener != null) {
            tasksListener.remove();
        }

        tasksListener = firestoreHelper.getProjectTasks(currentProject.getProjectId(),
                new FirestoreHelper.TaskCallback() {
                    @Override
                    public void onSuccess(List<Task> tasks) {
                        if (!isAdded() || getContext() == null) return;

                        allTasks.clear();
                        allTasks.addAll(tasks);
                        filterTasks();
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded() || getContext() == null) return;

                        Log.e(TAG, "Firestore error: " + error);

                        // Ignore harmless idle connection warnings
                        if (error.contains("CANCELLED") ||
                                error.contains("idle") ||
                                error.contains("Disconnecting idle stream")) {
                            return;  // Don't show these as errors
                        }

                        // Show real errors
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