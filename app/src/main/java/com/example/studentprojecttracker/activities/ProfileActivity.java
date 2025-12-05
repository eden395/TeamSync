package com.example.studentprojecttracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgProfileAvatar, btnBack;
    private TextView tvProfileName, tvProfileEmail;
    private RadioGroup rgViewMode;
    private RadioButton rbMemberView, rbAdvisorView;
    private View btnEditProfile, btnChangePassword, btnLogout;

    private SessionManager sessionManager;
    private FirebaseAuth mAuth;

    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        mAuth = FirebaseAuth.getInstance();

        // Register edit profile launcher
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Reload user data after edit
                        loadUserData();
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        initializeViews();
        loadUserData();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user data when returning to this activity
        loadUserData();
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

            // Load profile picture using Glide
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .error(R.drawable.ic_avatar_placeholder)
                        .into(imgProfileAvatar);
            } else {
                imgProfileAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
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
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });

        btnChangePassword.setOnClickListener(v -> {
            showChangePasswordDialog();
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);

        TextInputEditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnChangePassword).setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(currentPassword, newPassword, dialog);
        });

        dialog.show();
    }

    private void changePassword(String currentPassword, String newPassword, AlertDialog dialog) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        // Re-authenticate user before changing password
        com.google.firebase.auth.AuthCredential credential =
                com.google.firebase.auth.EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to change password: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                });
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