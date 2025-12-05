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
import com.google.android.material.card.MaterialCardView;

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
        private MaterialCardView cardView;
        private CheckBox checkBox;
        private TextView tvTaskName, tvPriority, tvAssignee, tvDueDate;
        private ImageView btnMenu;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
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

            // Display assignee
            displayAssigneeName(task);

            tvDueDate.setText("Due: " + task.getDueDate());

            // Set card background color based on priority
            switch (task.getPriority()) {
                case "High":
                    cardView.setCardBackgroundColor(
                            itemView.getContext().getResources().getColor(R.color.priority_high));
                    break;
                case "Medium":
                    cardView.setCardBackgroundColor(
                            itemView.getContext().getResources().getColor(R.color.priority_medium));
                    break;
                case "Low":
                    cardView.setCardBackgroundColor(
                            itemView.getContext().getResources().getColor(R.color.priority_low));
                    break;
                default:
                    cardView.setCardBackgroundColor(
                            itemView.getContext().getResources().getColor(R.color.card_background2));
            }

            // Priority badge
            int bgResource = R.drawable.badge_medium;
            if ("High".equals(task.getPriority())) bgResource = R.drawable.badge_high;
            else if ("Low".equals(task.getPriority())) bgResource = R.drawable.badge_low;
            tvPriority.setBackgroundResource(bgResource);

            // Checkbox
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(task.isCompleted());

            if (task.isCompleted()) {
                tvTaskName.setPaintFlags(tvTaskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                tvTaskName.setPaintFlags(tvTaskName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            boolean canCheck = currentUserId.equals(task.getAssigneeId()) || isUserLeader;
            checkBox.setEnabled(canCheck);
            if (canCheck) {
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (listener != null) listener.onTaskChecked(task, position, isChecked);
                });
            }

            // Menu button visibility and actions
            btnMenu.setVisibility(isUserLeader ? View.VISIBLE : View.GONE);
            if (isUserLeader) {
                btnMenu.setOnClickListener(v -> showTaskMenu(v, task, position));
            }
        }

        private void displayAssigneeName(Task task) {
            String assigneeName = task.getAssigneeName();
            String assigneeId = task.getAssigneeId();

            if (assigneeName != null && !assigneeName.isEmpty() && !assigneeName.contains("@")) {
                tvAssignee.setText("Assigned to: " + assigneeName);
                return;
            }

            if (assigneeId == null || assigneeId.isEmpty()) {
                tvAssignee.setText("Assigned to: Unassigned");
                return;
            }

            String displayName = extractUsernameFromEmail(assigneeId);
            tvAssignee.setText("Assigned to: " + displayName);
        }

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
                    if (listener != null) listener.onTaskEdit(task, position);
                } else if ("Delete Task".equals(title)) {
                    if (listener != null) listener.onTaskDelete(task, position);
                }
                return true;
            });

            popup.show();
        }
    }
}
