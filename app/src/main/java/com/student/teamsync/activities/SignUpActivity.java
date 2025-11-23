package com.student.teamsync.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.student.teamsync.R;
import com.student.teamsync.utils.SessionManager;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private MaterialButton signUpButton;
    private FirebaseAuth mAuth;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        signUpButton = findViewById(R.id.signUpButton);
    }

    private void setupClickListeners() {
        signUpButton.setOnClickListener(v -> performSignUp());

        findViewById(R.id.termsText).setOnClickListener(v -> {
            Toast.makeText(this, "Terms of Service", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.privacyText).setOnClickListener(v -> {
            Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show();
        });
    }

    private void performSignUp() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (!validateInputs(name, email, password, confirmPassword)) {
            return;
        }

        signUpButton.setEnabled(false);
        signUpButton.setText("Creating account...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Update user profile with name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                sessionManager.createLoginSession(user.getEmail());
                                Toast.makeText(SignUpActivity.this, 
                                    getString(R.string.signup_success), 
                                    Toast.LENGTH_SHORT).show();
                                navigateToMain();
                            });
                        }
                    } else {
                        signUpButton.setEnabled(true);
                        signUpButton.setText(R.string.sign_up_button);
                        Toast.makeText(SignUpActivity.this, 
                            getString(R.string.error_signup_failed), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInputs(String name, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError(getString(R.string.error_empty_name));
            nameEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_empty_email));
            emailEditText.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.error_invalid_email));
            emailEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_empty_password));
            passwordEditText.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordEditText.setError(getString(R.string.error_password_length));
            passwordEditText.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError(getString(R.string.error_password_mismatch));
            confirmPasswordEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void navigateToMain() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
