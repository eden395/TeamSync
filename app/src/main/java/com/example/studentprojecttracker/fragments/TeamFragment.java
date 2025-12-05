package com.example.studentprojecttracker.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.auth.FirebaseAuth;

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
    private boolean isUserLeader = false;

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

        // Get project from parent activity FIRST
        if (getActivity() != null && getActivity().getIntent() != null) {
            currentProject = (Project) getActivity().getIntent().getSerializableExtra("project");
        }

        // Check if current user is the leader AFTER getting the project
        checkIfUserIsLeader();

        // Setup RecyclerView
        rvTeamMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        teamMemberAdapter = new TeamMemberAdapter(
                new ArrayList<>(),
                isUserLeader,
                new TeamMemberAdapter.OnMemberInteractionListener() {
                    @Override
                    public void onMemberEdit(TeamMember member, int position) {
                        showEditMemberDialog(member, position);
                    }

                    @Override
                    public void onMemberDelete(TeamMember member, int position) {
                        showDeleteMemberConfirmation(member, position);
                    }
                }
        );
        rvTeamMembers.setAdapter(teamMemberAdapter);

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
     * Check if the current user is the project leader
     */
    /**
     * Check if the current user is the project leader
     */
    private void checkIfUserIsLeader() {
        if (currentProject != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            // Check using both userId and email since the project might store either
            isUserLeader = currentProject.isLeader(currentUserId) ||
                    currentProject.isLeader(currentUserEmail);
        }
    }

    /**
     * Show edit member dialog
     */
    /**
     * Show edit member dialog
     */
    private void showEditMemberDialog(TeamMember member, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit " + member.getName());

        // Create options array
        String[] options;
        boolean isMemberLeader = currentProject.isLeader(member.getEmail()) ||
                currentProject.isLeader(member.getUserId());

        if (isMemberLeader) {
            options = new String[]{
//                    "View Member Details",
                    "Remove as Leader",
                    "Reassign Tasks"
            };
        } else {
            options = new String[]{
//                    "View Member Details",
                    "Promote to Leader",
                    "Reassign Tasks"
            };
        }

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
//                case 0: // View Member Details
//                    showMemberDetailsDialog(member);
//                    break;
                case 1: // Promote to Leader / Remove as Leader
                    if (isMemberLeader) {
                        removeAsLeader(member);
                    } else {
                        promoteToLeader(member);
                    }
                    break;
                case 2: // Reassign Tasks
                    showReassignTasksDialog(member);
                    break;
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Show member details in a dialog
     */
    private void showMemberDetailsDialog(TeamMember member) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(member.getName());

        String details = "Email: " + (member.getEmail() != null ? member.getEmail() : "N/A") + "\n\n" +
                "Status: " + member.getStatus() + "\n" +
                "Tasks Assigned: " + member.getTasksAssigned() + "\n" +
                "Completion: " + member.getCompletionPercentage() + "%\n\n";

        boolean isLeader = currentProject.isLeader(member.getEmail()) ||
                currentProject.isLeader(member.getUserId());
        if (isLeader) {
            details += "Role: Project Leader";
        } else {
            details += "Role: Team Member";
        }

        builder.setMessage(details);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    /**
     * Promote member to leader
     */
    private void promoteToLeader(TeamMember member) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Promote to Leader")
                .setMessage("Promote " + member.getName() + " to project leader?")
                .setPositiveButton("Promote", (dialog, which) -> {
                    showLoading();

                    String memberIdentifier = member.getEmail() != null ? member.getEmail() : member.getUserId();
                    List<String> updatedLeaderIds = new ArrayList<>(currentProject.getLeaderIds());

                    if (!updatedLeaderIds.contains(memberIdentifier)) {
                        updatedLeaderIds.add(memberIdentifier);
                    }

                    // Update in Firestore
                    firestoreHelper.updateProjectLeaders(
                            currentProject.getProjectId(),
                            updatedLeaderIds,
                            new FirestoreHelper.OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            hideLoading();
                                            Toast.makeText(getContext(),
                                                    member.getName() + " promoted to leader",
                                                    Toast.LENGTH_SHORT).show();

                                            // Update local project reference
                                            currentProject.setLeaderIds(updatedLeaderIds);

                                            // Reload team members
                                            loadTeamMembers();
                                        });
                                    }
                                }

                                @Override
                                public void onError(String error) {
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            hideLoading();
                                            Toast.makeText(getContext(),
                                                    "Error promoting member: " + error,
                                                    Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                }
                            }
                    );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Remove member as leader
     */
    private void removeAsLeader(TeamMember member) {
        // Don't allow removing the creator as leader
        String memberIdentifier = member.getEmail() != null ? member.getEmail() : member.getUserId();
        if (currentProject.getCreatorId() != null &&
                (currentProject.getCreatorId().equals(memberIdentifier) ||
                        currentProject.getCreatorId().equals(member.getUserId()))) {
            Toast.makeText(getContext(), "Cannot remove project creator as leader", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Remove as Leader")
                .setMessage("Remove " + member.getName() + " from project leaders?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    showLoading();

                    List<String> updatedLeaderIds = new ArrayList<>(currentProject.getLeaderIds());
                    updatedLeaderIds.remove(memberIdentifier);
                    updatedLeaderIds.remove(member.getUserId());

                    // Update in Firestore
                    firestoreHelper.updateProjectLeaders(
                            currentProject.getProjectId(),
                            updatedLeaderIds,
                            new FirestoreHelper.OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            hideLoading();
                                            Toast.makeText(getContext(),
                                                    member.getName() + " removed as leader",
                                                    Toast.LENGTH_SHORT).show();

                                            // Update local project reference
                                            currentProject.setLeaderIds(updatedLeaderIds);

                                            // Reload team members
                                            loadTeamMembers();
                                        });
                                    }
                                }

                                @Override
                                public void onError(String error) {
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            hideLoading();
                                            Toast.makeText(getContext(),
                                                    "Error removing leader: " + error,
                                                    Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                }
                            }
                    );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Show dialog to reassign member's tasks
     */
    private void showReassignTasksDialog(TeamMember member) {
        Toast.makeText(getContext(), "Reassign tasks feature coming soon", Toast.LENGTH_SHORT).show();

        // TODO: Implement task reassignment
        // 1. Get all tasks assigned to this member
        // 2. Show a dialog with list of tasks
        // 3. Allow selecting new assignee for each task
        // 4. Update tasks in Firestore
    }

    /**
     * Show delete member confirmation dialog
     */
    private void showDeleteMemberConfirmation(TeamMember member, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Team Member")
                .setMessage("Are you sure you want to remove " + member.getName() + " from the team?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    deleteMemberFromTeam(member, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Delete member from team
     */
    private void deleteMemberFromTeam(TeamMember member, int position) {
        if (currentProject == null) return;

        showLoading();

        // Get the member identifier (email or userId)
        String memberIdentifier = member.getEmail() != null ? member.getEmail() : member.getUserId();

        // Remove member from project's memberIds list
        List<String> updatedMemberIds = new ArrayList<>(currentProject.getMemberIds());
        updatedMemberIds.remove(memberIdentifier);

        // Update project in Firestore
        firestoreHelper.updateProjectMembers(
                currentProject.getProjectId(),
                updatedMemberIds,
                new FirestoreHelper.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                hideLoading();
                                Toast.makeText(getContext(),
                                        member.getName() + " removed from team",
                                        Toast.LENGTH_SHORT).show();

                                // Update local project reference
                                currentProject.setMemberIds(updatedMemberIds);

                                // Reload team members
                                loadTeamMembers();
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                hideLoading();
                                Toast.makeText(getContext(),
                                        "Error removing member: " + error,
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                }
        );
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
        checkIfUserIsLeader();
        loadTeamMembers();
    }
}