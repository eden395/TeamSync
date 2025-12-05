package com.example.studentprojecttracker.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.activities.MainActivity;
import com.example.studentprojecttracker.adapters.ChatAdapter;
import com.example.studentprojecttracker.models.ChatMessage;
import com.example.studentprojecttracker.models.Project;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatsFragment extends Fragment {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageView attachIcon, sendIcon;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatList = new ArrayList<>();

    private FirebaseFirestore db;
    private String projectId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        db = FirebaseFirestore.getInstance();

        // Get project from MainActivity
        Project currentProject = ((MainActivity) requireActivity()).getCurrentProject();
        projectId = currentProject.getProjectId();

        initializeViews(view);
        setupRecyclerView();
        listenForMessages();
        setupSend();

        return view;
    }

    private void initializeViews(View view) {
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        attachIcon = view.findViewById(R.id.attachIcon);
        sendIcon = view.findViewById(R.id.sendIcon);
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(chatList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void setupSend() {
        sendIcon.setOnClickListener(v -> {
            String msg = messageInput.getText().toString().trim();
            if (TextUtils.isEmpty(msg)) return;

            sendMessage(msg);
            messageInput.setText("");
        });
    }

    private void sendMessage(String text) {
        String uid = FirebaseAuth.getInstance().getUid();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName(); // Or your user profile

        String messageId = db.collection("projects")
                .document(projectId)
                .collection("chats")
                .document().getId();

        ChatMessage message = new ChatMessage(
                messageId,
                uid,
                userName,
                text,
                System.currentTimeMillis(),
                null
        );

        db.collection("projects")
                .document(projectId)
                .collection("chats")
                .document(messageId)
                .set(message);
    }

    private void listenForMessages() {
        db.collection("projects")
                .document(projectId)
                .collection("chats")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    chatList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        ChatMessage msg = doc.toObject(ChatMessage.class);
                        chatList.add(msg);
                    }

                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.scrollToPosition(chatList.size() - 1);
                });
    }
}
