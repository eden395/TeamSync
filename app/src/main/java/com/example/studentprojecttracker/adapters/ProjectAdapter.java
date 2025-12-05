package com.example.studentprojecttracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.models.Project;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<Project> projectList;
    private OnProjectClickListener listener;
    private boolean isAdvisorMode;

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
        void onProjectMenuClick(Project project, View view);
    }

    public ProjectAdapter(List<Project> projectList, OnProjectClickListener listener, boolean isAdvisorMode) {
        this.projectList = projectList;
        this.listener = listener;
        this.isAdvisorMode = isAdvisorMode;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);
        holder.bind(project);
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProjectName, tvCourseCode, tvDueDate, tvProgress;
        private ImageView btnMenu;
        private LinearLayout layoutProgress;
        private ProgressBar progressBar;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            btnMenu = itemView.findViewById(R.id.btnMenu);
            layoutProgress = itemView.findViewById(R.id.layoutProgress);
            progressBar = itemView.findViewById(R.id.progressBar);
        }

        public void bind(Project project) {
            tvProjectName.setText(project.getProjectName());
            tvCourseCode.setText(project.getCourseCode());
            tvDueDate.setText("Due: " + project.getDueDate());

            // Show progress only for advisors
            if (isAdvisorMode) {
                layoutProgress.setVisibility(View.VISIBLE);
                progressBar.setProgress(project.getProgressPercentage());
                tvProgress.setText(project.getProgressPercentage() + "%");
            } else {
                layoutProgress.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProjectClick(project);
                }
            });

            btnMenu.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProjectMenuClick(project, v);
                }
            });
        }
    }
}