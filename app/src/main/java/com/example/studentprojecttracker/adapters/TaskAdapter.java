package com.example.studentprojecttracker.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.models.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private String currentUserId;
    private boolean isUserLeader;
    private OnTaskInteractionListener listener;

    public interface OnTaskInteractionListener {
        void onTaskChecked(Task task, int position, boolean isChecked);
        void onTaskEdit(Task task, int position);
        void onTaskDelete(Task task, int position);
    }

    public TaskAdapter(List<Task> taskList, String currentUserId, boolean isUserLeader,
                       OnTaskInteractionListener listener) {
        this.taskList = taskList;
        this.currentUserId = currentUserId;
        this.isUserLeader = isUserLeader;
        this.listener = listener;
    }

    // Simplified constructor for backward compatibility
    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
        this.currentUserId = "";
        this.isUserLeader = false;
        this.listener = null;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task, position);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkBox;
        private TextView tvTaskName, tvPriority, tvAssignee, tvDueDate;
        private ImageView btnMenu;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.taskCheckbox);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvAssignee = itemView.findViewById(R.id.tvAssignee);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            btnMenu = itemView.findViewById(R.id.btnTaskMenu);
        }

        public void bind(Task task, int position) {
            tvTaskName.setText(task.getTaskName());
            tvPriority.setText(task.getPriority());

            // Display assignee name (already loaded by TasksFragment)
            displayAssigneeName(task);

            tvDueDate.setText("Due: " + task.getDueDate());

            // Set priority badge background
            int bgResource = R.drawable.badge_medium;
            if ("High".equals(task.getPriority())) {
                bgResource = R.drawable.badge_high;
            } else if ("Low".equals(task.getPriority())) {
                bgResource = R.drawable.badge_low;
            }
            tvPriority.setBackgroundResource(bgResource);

            // Set checkbox state
            checkBox.setOnCheckedChangeListener(null); // Remove listener temporarily
            checkBox.setChecked(task.isCompleted());

            // Strike through if completed
            if (task.isCompleted()) {
                tvTaskName.setPaintFlags(tvTaskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                tvTaskName.setPaintFlags(tvTaskName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            // Checkbox interaction
            // Only allow checking if it's the user's own task OR if user is a leader
            boolean canCheck = currentUserId.equals(task.getAssigneeId()) || isUserLeader;
            checkBox.setEnabled(canCheck);

            if (canCheck) {
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (listener != null) {
                        listener.onTaskChecked(task, position, isChecked);
                    }
                });
            }

            // Menu button - only visible for leaders
            if (isUserLeader) {
                btnMenu.setVisibility(View.VISIBLE);
                btnMenu.setOnClickListener(v -> showTaskMenu(v, task, position));
            } else {
                btnMenu.setVisibility(View.GONE);
            }
        }

        /**
         * Display assignee name
         * The display name is already loaded by TasksFragment, so we just display it
         * Following the same pattern as TeamFragment
         */
        private void displayAssigneeName(Task task) {
            String assigneeName = task.getAssigneeName();
            String assigneeId = task.getAssigneeId();

            Log.d("TaskAdapter", "Task: " + task.getTaskName());
            Log.d("TaskAdapter", "AssigneeName: " + assigneeName);
            Log.d("TaskAdapter", "AssigneeId: " + assigneeId);

            // If assigneeName is available and looks like a proper name (not email), use it
            if (assigneeName != null && !assigneeName.isEmpty() && !assigneeName.contains("@")) {
                tvAssignee.setText("Assigned to: " + assigneeName);
                Log.d("TaskAdapter", "✓ Displaying name: " + assigneeName);
                return;
            }

            // If no valid assignee ID
            if (assigneeId == null || assigneeId.isEmpty()) {
                tvAssignee.setText("Assigned to: Unassigned");
                Log.d("TaskAdapter", "→ No assignee");
                return;
            }

            // Fallback: extract username from email
            // This should rarely happen since TasksFragment pre-loads all names
            String displayName = extractUsernameFromEmail(assigneeId);
            tvAssignee.setText("Assigned to: " + displayName);
            Log.d("TaskAdapter", "⚠ Using email fallback: " + displayName);
        }

        /**
         * Extract and capitalize username from email
         * Same logic as TeamFragment
         */
        private String extractUsernameFromEmail(String email) {
            if (email != null && email.contains("@")) {
                String username = email.substring(0, email.indexOf("@"));
                if (!username.isEmpty()) {
                    return username.substring(0, 1).toUpperCase() + username.substring(1);
                }
                return username;
            }
            return email != null ? email : "Unknown";
        }

        private void showTaskMenu(View view, Task task, int position) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.getMenu().add("Edit Task");
            popup.getMenu().add("Delete Task");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if ("Edit Task".equals(title)) {
                    if (listener != null) {
                        listener.onTaskEdit(task, position);
                    }
                } else if ("Delete Task".equals(title)) {
                    if (listener != null) {
                        listener.onTaskDelete(task, position);
                    }
                }
                return true;
            });

            popup.show();
        }
    }
}