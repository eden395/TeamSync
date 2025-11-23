package com.student.teamsync.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.student.teamsync.R;

public class FilesFragment extends Fragment {

    private RecyclerView filesRecyclerView;
    private MaterialButton uploadButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        
        return view;
    }

    private void initializeViews(View view) {
        filesRecyclerView = view.findViewById(R.id.filesRecyclerView);
        uploadButton = view.findViewById(R.id.uploadButton);
        
        uploadButton.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Upload file feature coming soon", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupRecyclerView() {
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Files adapter will be implemented when database is integrated
    }
}
