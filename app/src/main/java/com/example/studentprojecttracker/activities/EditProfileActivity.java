package com.example.studentprojecttracker.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.models.User;
import com.example.studentprojecttracker.utils.FirestoreHelper;
import com.example.studentprojecttracker.utils.ProfilePictureManager;
import com.example.studentprojecttracker.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView btnBack, imgEditAvatar, btnChangeAvatar;
    private TextInputEditText etDisplayName, etEmail;
    private Button btnSaveProfile;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirestoreHelper firestoreHelper;
    private StorageReference storageReference;

    private Uri selectedImageUri;
    private String currentPhotoUrl;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        firestoreHelper = new FirestoreHelper();
        storageReference = FirebaseStorage.getInstance().getReference();

        initializeViews();
        setupImagePicker();
        loadUserData();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        imgEditAvatar = findViewById(R.id.imgEditAvatar);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        etDisplayName = findViewById(R.id.etDisplayName);
        etEmail = findViewById(R.id.etEmail);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        // Display the selected image immediately
                        Glide.with(this)
                                .load(uri)
                                .circleCrop()
                                .into(imgEditAvatar);
                    }
                }
        );
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                etDisplayName.setText(displayName);
            }

            etEmail.setText(user.getEmail());
            etEmail.setEnabled(false); // Email changes require re-authentication

            // Load profile photo
            currentPhotoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
            if (currentPhotoUrl != null && !currentPhotoUrl.isEmpty()) {
                Glide.with(this)
                        .load(currentPhotoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .into(imgEditAvatar);
            }

            // Load additional user data from Firestore
            loadUserFromFirestore(user.getUid());
        }
    }

    private void loadUserFromFirestore(String userId) {
        firestoreHelper.getUserProfile(userId, new FirestoreHelper.UserCallback() {
            @Override
            public void onSuccess(User user) {
                // User data loaded successfully
                // You can populate additional fields here if needed
            }

            @Override
            public void onError(String error) {
                // User profile not in Firestore yet, will be created on save
            }
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnChangeAvatar.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String displayName = etDisplayName.getText().toString().trim();

        if (displayName.isEmpty()) {
            etDisplayName.setError("Name is required");
            etDisplayName.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSaveProfile.setEnabled(false);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            showError("User not authenticated");
            return;
        }

        // If a new image was selected, upload it first
        if (selectedImageUri != null) {
            uploadProfileImage(user.getUid(), displayName);
        } else {
            // No new image, just update the profile
            updateUserProfile(displayName, currentPhotoUrl);
        }
    }

    private void uploadProfileImage(String userId, String displayName) {
        // Create a reference to the profile image location
        StorageReference profileImageRef = storageReference
                .child("profile_images")
                .child(userId + ".jpg");

        // Upload the image
        profileImageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    profileImageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String photoUrl = uri.toString();
                                updateUserProfile(displayName, photoUrl);
                            })
                            .addOnFailureListener(e -> {
                                showError("Failed to get image URL: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    showError("Failed to upload image: " + e.getMessage());
                });
    }

    private void updateUserProfile(String displayName, String photoUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            showError("User not authenticated");
            return;
        }

        // Update Firebase Auth profile
        UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName);

        if (photoUrl != null && !photoUrl.isEmpty()) {
            profileUpdates.setPhotoUri(Uri.parse(photoUrl));
        }

        user.updateProfile(profileUpdates.build())
                .addOnSuccessListener(aVoid -> {
                    // Save to Firestore
                    saveUserToFirestore(user.getUid(), displayName, user.getEmail(), photoUrl);
                })
                .addOnFailureListener(e -> {
                    showError("Failed to update profile: " + e.getMessage());
                });
    }

    private void saveUserToFirestore(String userId, String displayName, String email, String photoUrl) {
        User user = new User();
        user.setUserId(userId);
        user.setName(displayName);
        user.setEmail(email);
        user.setPhotoUrl(photoUrl);

        SessionManager sessionManager = new SessionManager(this);
        String role = sessionManager.getUserRole();
        user.setRole(role != null ? role : "member");

        firestoreHelper.saveUserProfile(user, new FirestoreHelper.OnCompleteListener() {
            @Override
            public void onComplete(boolean success, String message) {
                progressBar.setVisibility(View.GONE);
                btnSaveProfile.setEnabled(true);

                if (success) {
                    // Clear profile picture cache so it reloads everywhere
                    ProfilePictureManager.getInstance().clearCache(userId);

                    Toast.makeText(EditProfileActivity.this,
                            "Profile updated successfully",
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(message);
                }
            }
        });
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        btnSaveProfile.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}