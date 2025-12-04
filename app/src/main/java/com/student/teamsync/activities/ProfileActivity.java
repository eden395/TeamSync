package com.student.teamsync.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.student.teamsync.R;
import com.student.teamsync.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgProfileAvatar, btnBack;
    private TextView tvProfileName, tvProfileEmail;
    private RadioGroup rgViewMode;
    private RadioButton rbMemberView, rbAdvisorView;
    private View btnEditProfile, btnChangePassword, btnLogout;

    private SessionManager sessionManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        loadUserData();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        rgViewMode = findViewById(R.id.rgViewMode);
        rbMemberView = findViewById(R.id.rbMemberView);
        rbAdvisorView = findViewById(R.id.rbAdvisorView);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
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
        btnBack.setOnClickListener(v -> finish());

        // View mode change listener
        rgViewMode.setOnCheckedChangeListener((group, checkedId) -> {
            String newMode;
            if (checkedId == R.id.rbAdvisorView) {
                newMode = "advisor";
            } else {
                newMode = "member";
            }

            sessionManager.saveUserRole(newMode);
            Toast.makeText(this, "View mode updated. Restart app to see changes.", Toast.LENGTH_SHORT).show();

            // Restart dashboard activity
            Intent intent = new Intent(ProfileActivity.this, ProjectsDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Edit profile feature coming soon", Toast.LENGTH_SHORT).show();
        });

        btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Change password feature coming soon", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        mAuth.signOut();
        sessionManager.logout();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}