package com.student.teamsync.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.student.teamsync.R;
import com.student.teamsync.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private MaterialButton loginButton;
    private FirebaseAuth mAuth;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performLogin());

        findViewById(R.id.signUpText).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });

        findViewById(R.id.forgotPasswordText).setOnClickListener(v -> {
            Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void performLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!validateInputs(email, password)) {
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            sessionManager.createLoginSession(user.getEmail());
                            Toast.makeText(LoginActivity.this, 
                                getString(R.string.login_success), 
                                Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        }
                    } else {
                        loginButton.setEnabled(true);
                        loginButton.setText(R.string.login);
                        Toast.makeText(LoginActivity.this, 
                            getString(R.string.error_login_failed), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInputs(String email, String password) {
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

        return true;
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, RoleSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
