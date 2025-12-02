package com.student.teamsync.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.student.teamsync.R;
import com.student.teamsync.fragments.ChatsFragment;
import com.student.teamsync.fragments.FilesFragment;
import com.student.teamsync.fragments.TasksFragment;
import com.student.teamsync.fragments.TeamFragment;
import com.student.teamsync.models.Project;
import com.student.teamsync.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private ImageView logoutIcon;
    private FirebaseAuth mAuth;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Add this at the beginning of onCreate, before checking login
        Project currentProject = (Project) getIntent().getSerializableExtra("project");
        if (currentProject != null) {
            // Store current project for fragments to access
            // You can use SharedPreferences or a singleton to share this data
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

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
}
