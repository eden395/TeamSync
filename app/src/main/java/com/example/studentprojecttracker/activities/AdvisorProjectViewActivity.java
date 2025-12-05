package com.example.studentprojecttracker.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.adapters.MemberProgressAdapter;
import com.example.studentprojecttracker.models.MemberProgress;
import com.example.studentprojecttracker.models.Project;

import java.util.ArrayList;
import java.util.List;

public class AdvisorProjectViewActivity extends AppCompatActivity {

    private TextView tvProjectTitle, tvOverallProgress, tvTotalTasks, tvCompletedTasks, tvTotalMembers;
    private ProgressBar progressOverall;
    private RecyclerView rvMemberProgress;
    private ImageView btnBack;
    private MemberProgressAdapter memberProgressAdapter;
    private List<MemberProgress> memberProgressList;
    private Project project;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advisor_projects_view);

        project = (Project) getIntent().getSerializableExtra("project");

        initializeViews();
        setupRecyclerView();
        loadProjectData();
        loadMemberProgress();
    }

    private void initializeViews() {
        tvProjectTitle = findViewById(R.id.tvProjectTitle);
        tvOverallProgress = findViewById(R.id.tvOverallProgress);
        tvTotalTasks = findViewById(R.id.tvTotalTasks);
        tvCompletedTasks = findViewById(R.id.tvCompletedTasks);
        tvTotalMembers = findViewById(R.id.tvTotalMembers);
        progressOverall = findViewById(R.id.progressOverall);
        rvMemberProgress = findViewById(R.id.rvMemberProgress);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        memberProgressList = new ArrayList<>();
        memberProgressAdapter = new MemberProgressAdapter(memberProgressList);
        rvMemberProgress.setLayoutManager(new LinearLayoutManager(this));
        rvMemberProgress.setAdapter(memberProgressAdapter);
    }

    private void loadProjectData() {
        if (project != null) {
            tvProjectTitle.setText(project.getProjectName());

            // Sample overall statistics
            int totalTasks = 24;
            int completedTasks = 16;
            int progress = (completedTasks * 100) / totalTasks;

            tvTotalTasks.setText(String.valueOf(totalTasks));
            tvCompletedTasks.setText(String.valueOf(completedTasks));
            tvTotalMembers.setText("4");
            tvOverallProgress.setText(progress + "%");
            progressOverall.setProgress(progress);
        }
    }

    private void loadMemberProgress() {
        // Sample member progress data
        memberProgressList.add(new MemberProgress(
                "1", "Christine Fel", "christine@email.com", 5, 5, "Ahead"
        ));
        memberProgressList.add(new MemberProgress(
                "2", "Eden Grace", "eden@email.com", 5, 6, "On Track"
        ));
        memberProgressList.add(new MemberProgress(
                "3", "Irish Sabido", "irish@email.com", 4, 6, "On Track"
        ));
        memberProgressList.add(new MemberProgress(
                "4", "Aliyah Margaret", "aliyah@email.com", 2, 7, "Behind"
        ));

        memberProgressAdapter.notifyDataSetChanged();
    }
}