package com.example.studentprojecttracker.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.utils.SessionManager;

public class RoleSelectionActivity extends AppCompatActivity {

    private MaterialCardView cardMember, cardAdvisor;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        sessionManager = new SessionManager(this);

        cardMember = findViewById(R.id.cardMember);
        cardAdvisor = findViewById(R.id.cardAdvisor);

        cardMember.setOnClickListener(v -> selectRole(getString(R.string.role_member)));
        cardAdvisor.setOnClickListener(v -> selectRole(getString(R.string.role_advisor)));
    }

    private void selectRole(String role) {
        sessionManager.saveUserRole(role);

        Intent intent = new Intent(RoleSelectionActivity.this, ProjectsDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}