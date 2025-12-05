package com.example.studentprojecttracker.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.adapters.TeamMemberAdapter;
import com.example.studentprojecttracker.models.Project;
import com.example.studentprojecttracker.models.Task;
import com.example.studentprojecttracker.models.TeamMember;
import com.example.studentprojecttracker.models.User;
import com.example.studentprojecttracker.utils.FirestoreHelper;
import com.example.studentprojecttracker.utils.ProfilePictureManager;

import java.util.ArrayList;
import java.util.List;

public class TeamFragment extends Fragment {

    private RecyclerView rvTeamMembers;
    private TeamMemberAdapter teamMemberAdapter;
    private FirestoreHelper firestoreHelper;
    private ProfilePictureManager profilePictureManager;
    private Project currentProject;
    private View progressBar;
    private View emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize
        firestoreHelper = new FirestoreHelper();
        profilePictureManager = ProfilePictureManager.getInstance();

        // Find views
        rvTeamMembers = view.findViewById(R.id.rvTeamMembers);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);

        // Setup RecyclerView
        rvTeamMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        teamMemberAdapter = new TeamMemberAdapter(new ArrayList<>());
        rvTeamMembers.setAdapter(teamMemberAdapter);

        // Get project from parent activity
        if (getActivity() != null && getActivity().getIntent() != null) {
            currentProject = (Project) getActivity().getIntent().getSerializableExtra("project");
        }

        // Load team members
        if (currentProject != null) {
            loadTeamMembers();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh team members list to get updated profile pictures
        if (currentProject != null) {
            loadTeamMembers();
        }
    }

    /**
     * Load team members with their profile pictures and stats
     */
    private void loadTeamMembers() {
        if (currentProject == null || currentProject.getMemberIds() == null
                || currentProject.getMemberIds().isEmpty()) {
            showEmptyView();
            return;
        }

        showLoading();

        List<String> memberIds = currentProject.getMemberIds();
        List<TeamMember> teamMembers = new ArrayList<>();

        // Preload all profile pictures for better performance
        profilePictureManager.preloadProfilePictures(requireContext(), memberIds);

        // Counter to track loaded members
        final int[] loadedCount = {0};
        final int totalMembers = memberIds.size();

        for (String memberId : memberIds) {
            loadMemberWithProfile(memberId, teamMembers, loadedCount, totalMembers);
        }
    }

    /**
     * Load individual member with profile and stats
     */
    /**
     * Load individual member with profile and stats
     */
    private void loadMemberWithProfile(String memberId, List<TeamMember> teamMembers,
                                       int[] loadedCount, int totalMembers) {
        Log.d("TeamFragment", "=== Loading profile for: " + memberId + " ===");

        // Use email-based query since memberIds contain emails
        firestoreHelper.getUserProfileByEmail(memberId, new FirestoreHelper.UserCallback() {
            @Override
            public void onSuccess(User user) {
                Log.d("TeamFragment", "✓ Firestore SUCCESS for: " + memberId);
                Log.d("TeamFragment", "  - User ID: " + user.getUserId());
                Log.d("TeamFragment", "  - Name: " + user.getName());
                Log.d("TeamFragment", "  - Email: " + user.getEmail());
                Log.d("TeamFragment", "  - PhotoUrl: " + user.getPhotoUrl());

                // Use the name from Firestore (should be the display name)
                String displayName = user.getName();

                // Check if name is valid
                if (displayName == null || displayName.isEmpty()) {
                    Log.w("TeamFragment", "  ⚠ Name is null/empty, using email fallback");
                    displayName = extractUsernameFromEmail(memberId);
                } else if (displayName.contains("@")) {
                    Log.w("TeamFragment", "  ⚠ Name looks like email, using email fallback");
                    displayName = extractUsernameFromEmail(memberId);
                } else {
                    Log.d("TeamFragment", "  ✓ Using name from Firestore: " + displayName);
                }

                TeamMember member = new TeamMember(
                        user.getUserId(),
                        displayName,
                        user.getEmail()
                );
                member.setPhotoUrl(user.getPhotoUrl());

                Log.d("TeamFragment", "  → Created TeamMember with name: " + displayName);

                // Calculate member statistics
                calculateMemberStats(member, currentProject.getProjectId());

                teamMembers.add(member);
                loadedCount[0]++;

                // Update UI when all members are loaded
                if (loadedCount[0] == totalMembers) {
                    Log.d("TeamFragment", "=== All " + totalMembers + " members loaded ===");
                    updateTeamMembersList(teamMembers);
                }
            }

            @Override
            public void onError(String error) {
                Log.e("TeamFragment", "✗ Firestore FAILED for: " + memberId);
                Log.e("TeamFragment", "  Error: " + error);

                // Create member with email username as fallback
                String displayName = extractUsernameFromEmail(memberId);
                Log.d("TeamFragment", "  → Using fallback name: " + displayName);

                TeamMember member = new TeamMember(memberId, displayName, memberId);
                member.setUserId(memberId);
                teamMembers.add(member);
                loadedCount[0]++;

                if (loadedCount[0] == totalMembers) {
                    updateTeamMembersList(teamMembers);
                }
            }
        });
    }

    /**
     * Extract username from email and capitalize
     */
    private String extractUsernameFromEmail(String email) {
        if (email != null && email.contains("@")) {
            String username = email.substring(0, email.indexOf("@"));
            if (!username.isEmpty()) {
                return username.substring(0, 1).toUpperCase() + username.substring(1);
            }
            return username;
        }
        return email;
    }

    /**
     * Calculate member statistics (tasks assigned, completion %)
     */
    private void calculateMemberStats(TeamMember member, String projectId) {
        firestoreHelper.getProjectTasks(projectId, new FirestoreHelper.TaskCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                int assignedTasks = 0;
                int completedTasks = 0;

                for (Task task : tasks) {
                    if (task.getAssigneeId() != null &&
                            (task.getAssigneeId().equals(member.getUserId()) ||
                                    task.getAssigneeId().equals(member.getEmail()))) {
                        assignedTasks++;
                        if (task.isCompleted()) {
                            completedTasks++;
                        }
                    }
                }

                member.setTasksAssigned(assignedTasks);

                if (assignedTasks > 0) {
                    int completion = (completedTasks * 100) / assignedTasks;
                    member.setCompletionPercentage(completion);

                    // Set status based on completion
                    if (completion >= 80) {
                        member.setStatus("Ahead");
                    } else if (completion >= 50) {
                        member.setStatus("On Track");
                    } else {
                        member.setStatus("Behind");
                    }
                } else {
                    member.setCompletionPercentage(0);
                    member.setStatus("On Track");
                }

                // Refresh the adapter to show updated stats
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (teamMemberAdapter != null) {
                            teamMemberAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                // Keep default values
                member.setTasksAssigned(0);
                member.setCompletionPercentage(0);
                member.setStatus("On Track");
            }
        });
    }

    /**
     * Update team members list in UI
     */
    private void updateTeamMembersList(List<TeamMember> members) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            hideLoading();

            if (members.isEmpty()) {
                showEmptyView();
            } else {
                hideEmptyView();
                teamMemberAdapter.updateData(members);
            }
        });
    }

    /**
     * Show loading indicator
     */
    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (rvTeamMembers != null) {
            rvTeamMembers.setVisibility(View.GONE);
        }
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }

    /**
     * Hide loading indicator
     */
    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (rvTeamMembers != null) {
            rvTeamMembers.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show empty view when no team members
     */
    private void showEmptyView() {
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
        }
        if (rvTeamMembers != null) {
            rvTeamMembers.setVisibility(View.GONE);
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Hide empty view
     */
    private void hideEmptyView() {
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }

    /**
     * Public method to refresh team members (can be called from parent activity)
     */
    public void refreshTeamMembers() {
        if (currentProject != null) {
            loadTeamMembers();
        }
    }

    /**
     * Update project reference (useful when project details change)
     */
    public void setProject(Project project) {
        this.currentProject = project;
        loadTeamMembers();
    }
}