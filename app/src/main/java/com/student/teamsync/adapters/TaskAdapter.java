package com.student.teamsync.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.student.teamsync.R;
import com.student.teamsync.models.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
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
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private CheckBox taskCheckbox;
        private TextView taskNameText;
        private TextView priorityBadge;
        private TextView assigneeText;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskCheckbox = itemView.findViewById(R.id.taskCheckbox);
            taskNameText = itemView.findViewById(R.id.taskNameText);
            priorityBadge = itemView.findViewById(R.id.priorityBadge);
            assigneeText = itemView.findViewById(R.id.assigneeText);
        }

        public void bind(Task task) {
            taskNameText.setText(task.getTaskName());
            taskCheckbox.setChecked(task.isCompleted());
            priorityBadge.setText(task.getPriority());
            assigneeText.setText(task.getAssignee() + " Due: " + task.getDueDate());

            // Set priority badge background
            Drawable background;
            switch (task.getPriority()) {
                case "High":
                    background = ContextCompat.getDrawable(itemView.getContext(), R.drawable.badge_high);
                    break;
                case "Medium":
                    background = ContextCompat.getDrawable(itemView.getContext(), R.drawable.badge_medium);
                    break;
                case "Low":
                    background = ContextCompat.getDrawable(itemView.getContext(), R.drawable.badge_low);
                    break;
                default:
                    background = ContextCompat.getDrawable(itemView.getContext(), R.drawable.badge_medium);
            }
            priorityBadge.setBackground(background);

            taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.setCompleted(isChecked);
            });
        }
    }
}
