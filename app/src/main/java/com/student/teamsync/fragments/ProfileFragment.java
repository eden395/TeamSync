package com.student.teamsync.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.student.teamsync.R;
import com.student.teamsync.activities.LoginActivity;
import com.student.teamsync.activities.ProjectsDashboardActivity;
import com.student.teamsync.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private ImageView imgProfileAvatar;
    private TextView tvProfileName, tvProfileEmail;
    private RadioGroup rgViewMode;
    private RadioButton rbMemberView, rbAdvisorView;
    private View btnEditProfile, btnChangePassword, btnLogout;

    private SessionManager sessionManager;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sessionManager = new SessionManager(getContext());
        mAuth = FirebaseAuth.getInstance();

        initializeViews(view);
        loadUserData();
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        imgProfileAvatar = view.findViewById(R.id.imgProfileAvatar);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        rgViewMode = view.findViewById(R.id.rgViewMode);
        rbMemberView = view.findViewById(R.id.rbMemberView);
        rbAdvisorView = view.findViewById(R.id.rbAdvisorView);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvProfileEmail.setText(user.getEmail());
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvProfileName.setText(displayName);
            } else {
                tvProfileName.setText("User");
            }
        }

        // Load saved view mode
        String viewMode = sessionManager.getUserRole();
        if ("advisor".equals(viewMode)) {
            rbAdvisorView.setChecked(true);
        } else {
            rbMemberView.setChecked(true);
        }
    }

    private void setupClickListeners() {
        // View mode change listener
        rgViewMode.setOnCheckedChangeListener((group, checkedId) -> {
            String newMode;
            if (checkedId == R.id.rbAdvisorView) {
                newMode = "advisor";
            } else {
                newMode = "member";
            }

            sessionManager.saveUserRole(newMode);
            Toast.makeText(getContext(), "View mode updated. Restart app to see changes.", Toast.LENGTH_SHORT).show();

            // Restart dashboard activity
            Intent intent = new Intent(getActivity(), ProjectsDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Edit profile feature coming soon", Toast.LENGTH_SHORT).show();
        });

        btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Change password feature coming soon", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        mAuth.signOut();
        sessionManager.logout();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}