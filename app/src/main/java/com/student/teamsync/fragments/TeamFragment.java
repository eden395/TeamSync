package com.student.teamsync.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.student.teamsync.R;
import com.student.teamsync.adapters.TeamMemberAdapter;
import com.student.teamsync.models.TeamMember;

import java.util.ArrayList;
import java.util.List;

public class TeamFragment extends Fragment {

    private RecyclerView teamRecyclerView;
    private TeamMemberAdapter teamMemberAdapter;
    private List<TeamMember> teamMemberList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        loadSampleTeamMembers();
        
        return view;
    }

    private void initializeViews(View view) {
        teamRecyclerView = view.findViewById(R.id.teamRecyclerView);
    }

    private void setupRecyclerView() {
        teamMemberList = new ArrayList<>();
        teamMemberAdapter = new TeamMemberAdapter(teamMemberList);
        teamRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        teamRecyclerView.setAdapter(teamMemberAdapter);
    }

    private void loadSampleTeamMembers() {
        // Sample team members based on the PDF design
        teamMemberList.add(new TeamMember("Christine Fel", "Ahead", 5, 100));
        teamMemberList.add(new TeamMember("Eden Grace", "On Track", 5, 85));
        teamMemberList.add(new TeamMember("Irish", "On Track", 5, 75));
        
        teamMemberAdapter.notifyDataSetChanged();
    }
}
