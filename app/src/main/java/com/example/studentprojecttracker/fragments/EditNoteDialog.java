package com.example.studentprojecttracker.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.studentprojecttracker.R;

public class EditNoteDialog extends DialogFragment {

    private EditText editNoteInput;
    private Button saveButton;

    private EditNoteListener listener;
    private String initialText;

    public interface EditNoteListener {
        void onNoteEdited(String newText);
    }

    public void setListener(EditNoteListener listener) {
        this.listener = listener;
    }

    public static EditNoteDialog newInstance(String text) {
        EditNoteDialog dialog = new EditNoteDialog();
        Bundle args = new Bundle();
        args.putString("text", text);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_note, container, false);

        editNoteInput = view.findViewById(R.id.editNoteInput);
        saveButton = view.findViewById(R.id.saveButton);

        if (getArguments() != null) {
            initialText = getArguments().getString("text", "");
            editNoteInput.setText(initialText);
        }

        saveButton.setOnClickListener(v -> {
            String newText = editNoteInput.getText().toString().trim();
            if (!TextUtils.isEmpty(newText) && listener != null) {
                listener.onNoteEdited(newText);
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
