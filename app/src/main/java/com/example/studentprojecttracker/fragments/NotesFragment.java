package com.example.studentprojecttracker.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.activities.MainActivity;
import com.example.studentprojecttracker.adapters.NoteAdapter;
import com.example.studentprojecttracker.models.Note;
import com.example.studentprojecttracker.models.Project;
import com.example.studentprojecttracker.utils.FirestoreHelper;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotesFragment extends Fragment {

    private RecyclerView notesRecyclerView;
    private EditText noteInput;
    private ImageView addNoteButton;

    private NoteAdapter adapter;
    private List<Note> notes = new ArrayList<>();

    private FirestoreHelper firestoreHelper;
    private String projectId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        notesRecyclerView = view.findViewById(R.id.notesRecyclerView);
        noteInput = view.findViewById(R.id.noteInput);
        addNoteButton = view.findViewById(R.id.addNoteButton);

        adapter = new NoteAdapter(getContext(), notes);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notesRecyclerView.setAdapter(adapter);

        firestoreHelper = new FirestoreHelper();

        Project currentProject = ((MainActivity) requireActivity()).getCurrentProject();
        if (currentProject == null) {
            Log.e("NotesFragment", "Project is null!");
            return view;
        }
        projectId = currentProject.getProjectId();

        // Listen for real-time updates
        listenForNotes();

        // Add note button
        addNoteButton.setOnClickListener(v -> addNote());

        // Edit/Delete actions
        adapter.setNoteActionListener(new NoteAdapter.NoteActionListener() {
            @Override
            public void onEdit(Note note) {
                // Show edit dialog
                EditNoteDialog dialog = EditNoteDialog.newInstance(note.getText());
                dialog.setListener(newText -> {
                    note.setText(newText);
                    firestoreHelper.addProjectNote(projectId, note, (success, message) -> {
                        if (!success) Log.e("NotesFragment", "Failed to update note: " + message);
                    });
                });
                dialog.show(getParentFragmentManager(), "EditNote");
            }

            @Override
            public void onDelete(Note note) {
                firestoreHelper.deleteProjectNote(projectId, note.getNoteId(), (success, message) -> {
                    if (!success) Log.e("NotesFragment", "Failed to delete note: " + message);
                });
            }
        });

        return view;
    }

    private void addNote() {
        String text = noteInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        String noteId = UUID.randomUUID().toString();
        String uid = FirebaseAuth.getInstance().getUid();
        String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        Note note = new Note(noteId, uid, name, text, System.currentTimeMillis());

        firestoreHelper.addProjectNote(projectId, note, (success, message) -> {
            if (success) noteInput.setText("");
            else Toast.makeText(getContext(), "Failed to add note: " + message, Toast.LENGTH_SHORT).show();
        });
    }

    private void listenForNotes() {
        firestoreHelper.getProjectNotes(projectId, new FirestoreHelper.NoteCallback() {
            @Override
            public void onSuccess(List<Note> noteList) {
                notes.clear();
                notes.addAll(noteList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Log.e("NotesFragment", "Error loading notes: " + error);
            }
        });
    }
}
