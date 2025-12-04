package com.student.teamsync.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.student.teamsync.R;
import com.student.teamsync.adapters.TeamMemberAdapter;
import com.student.teamsync.models.Project;
import com.student.teamsync.models.TeamMember;
import com.student.teamsync.utils.FirestoreHelper;

import java.util.ArrayList;
import java.util.List;

public class TeamFragment extends Fragment {

    private RecyclerView teamRecyclerView;
    private MaterialButton addMemberButton;
    private TeamMemberAdapter teamMemberAdapter;
    private List<TeamMember> teamMemberList;

    private Project currentProject;
    private String currentUserId;
    private boolean isUserLeader = false;
    private FirestoreHelper firestoreHelper;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team, container, false);

        firestoreHelper = new FirestoreHelper();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        // Get project data passed from MainActivity
        if (getActivity() != null && getActivity().getIntent() != null) {
            currentProject = (Project) getActivity().getIntent().getSerializableExtra("project");
            if (currentProject != null) {
                isUserLeader = currentProject.isLeader(currentUserId);
            }
        }

        initializeViews(view);
        setupRecyclerView();
        loadTeamMembers();

        return view;
    }

    private void initializeViews(View view) {
        teamRecyclerView = view.findViewById(R.id.teamRecyclerView);
        addMemberButton = view.findViewById(R.id.addMemberButton);

        // Show add member button only for leaders
        if (isUserLeader) {
            addMemberButton.setVisibility(View.VISIBLE);
            addMemberButton.setOnClickListener(v -> showAddMemberDialog());
        } else {
            addMemberButton.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        teamMemberList = new ArrayList<>();
        teamMemberAdapter = new TeamMemberAdapter(teamMemberList);
        teamRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        teamRecyclerView.setAdapter(teamMemberAdapter);
    }

    private void loadTeamMembers() {
        if (currentProject == null) return;

        // Load team members from project's memberIds
        teamMemberList.clear();
        for (String memberId : currentProject.getMemberIds()) {
            // Extract name from email (simple version - you can enhance this)
            String name = memberId.split("@")[0];
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            teamMemberList.add(new TeamMember(name, "On Track", 0, 0));
        }
        teamMemberAdapter.notifyDataSetChanged();
    }

    private void showAddMemberDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_member, null);

        TextInputEditText etMemberEmail = dialogView.findViewById(R.id.etMemberEmail);
        TextInputEditText etMemberName = dialogView.findViewById(R.id.etMemberName);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            String email = etMemberEmail.getText().toString().trim();
            String name = etMemberName.getText().toString().trim();

            if (email.isEmpty() || name.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.setMessage("Adding member...");
            progressDialog.show();

            // Add member to project
            if (currentProject != null) {
                currentProject.getMemberIds().add(email);

                firestoreHelper.updateProject(currentProject, new FirestoreHelper.OnCompleteListener() {
                    @Override
                    public void onComplete(boolean success, String message) {
                        progressDialog.dismiss();
                        if (success) {
                            // Add to display list
                            TeamMember newMember = new TeamMember(name, "On Track", 0, 0);
                            teamMemberList.add(newMember);
                            teamMemberAdapter.notifyItemInserted(teamMemberList.size() - 1);

                            Toast.makeText(getContext(), name + " added to team",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), "Error: " + message,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        dialog.show();
    }
}