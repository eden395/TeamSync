package com.student.teamsync.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.student.teamsync.R;

public class ChatsFragment extends Fragment {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageView attachIcon, sendIcon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        
        return view;
    }

    private void initializeViews(View view) {
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        attachIcon = view.findViewById(R.id.attachIcon);
        sendIcon = view.findViewById(R.id.sendIcon);
    }

    private void setupRecyclerView() {
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Chat adapter will be implemented when database is integrated
    }

    private void setupClickListeners() {
        attachIcon.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Attach file feature coming soon", Toast.LENGTH_SHORT).show()
        );
        
        sendIcon.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                Toast.makeText(getContext(), "Send message: " + message, Toast.LENGTH_SHORT).show();
                messageInput.setText("");
            }
        });
    }
}
