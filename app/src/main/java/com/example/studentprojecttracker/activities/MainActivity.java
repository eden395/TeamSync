package com.example.studentprojecttracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.fragments.ChatsFragment;
import com.example.studentprojecttracker.fragments.FilesFragment;
import com.example.studentprojecttracker.fragments.TasksFragment;
import com.example.studentprojecttracker.fragments.TeamFragment;
import com.example.studentprojecttracker.models.Project;
import com.example.studentprojecttracker.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigation;
    private ImageView logoutIcon;
    private FirebaseAuth mAuth;
    private SessionManager sessionManager;
    private Project currentProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        mAuth = FirebaseAuth.getInstance();

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        // Get project data passed from ProjectsDashboardActivity
        currentProject = (Project) getIntent().getSerializableExtra("project");

        if (currentProject == null) {
            Log.e(TAG, "No project data received!");
            Toast.makeText(this, "Error: No project selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Project loaded: " + currentProject.getProjectName());

        setContentView(R.layout.activity_main);

        initializeViews();
        setupBottomNavigation();
        setupLogout();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new TasksFragment());
        }
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        logoutIcon = findViewById(R.id.logoutIcon);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_tasks) {
                selectedFragment = new TasksFragment();
            } else if (itemId == R.id.nav_chats) {
                selectedFragment = new ChatsFragment();
            } else if (itemId == R.id.nav_files) {
                selectedFragment = new FilesFragment();
            } else if (itemId == R.id.nav_team) {
                selectedFragment = new TeamFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void setupLogout() {
        logoutIcon.setOnClickListener(v -> {
            mAuth.signOut();
            sessionManager.logout();
            navigateToLogin();
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Public method for fragments to get the current project
    public Project getCurrentProject() {
        return currentProject;
    }
}