package com.student.teamsync.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.student.teamsync.R;
import com.student.teamsync.adapters.FileAdapter;
import com.student.teamsync.models.FileModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FilesFragment extends Fragment {

    private RecyclerView filesRecyclerView;
    private MaterialButton uploadButton;
    private FileAdapter fileAdapter;
    private List<FileModel> fileList = new ArrayList<>();
    private ActivityResultLauncher<Intent> filePickerLauncher;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Register file picker launcher
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            uploadFileToFirebase(fileUri);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);

        initializeViews(view);
        setupRecyclerView();
        loadFilesFromFirestore();

        return view;
    }

    private void initializeViews(View view) {
        filesRecyclerView = view.findViewById(R.id.filesRecyclerView);
        uploadButton = view.findViewById(R.id.uploadButton);

        uploadButton.setOnClickListener(v -> openFilePicker());
    }

    private void setupRecyclerView() {
        fileAdapter = new FileAdapter(getContext(), fileList);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        filesRecyclerView.setAdapter(fileAdapter);
    }

    // Open file picker
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String[] mimeTypes = {
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "image/*"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select File"));
    }

    // Upload file to Firebase Storage
    private void uploadFileToFirebase(Uri fileUri) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Uploading...");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String fileName = getFileName(fileUri);
        String fileId = UUID.randomUUID().toString();

        // Create reference to Firebase Storage
        StorageReference fileRef = storageRef.child("project_files/" + fileId + "_" + fileName);

        // Upload file
        fileRef.putFile(fileUri)
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                })
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();

                        // Save file info to Firestore
                        saveFileToFirestore(fileName, downloadUrl);

                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Save file metadata to Firestore
    private void saveFileToFirestore(String name, String url) {
        FileModel file = new FileModel(name, url, System.currentTimeMillis());

        db.collection("files")
                .add(file)
                .addOnSuccessListener(documentReference -> {
                    loadFilesFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save file info", Toast.LENGTH_SHORT).show();
                });
    }

    // Load files from Firestore
    private void loadFilesFromFirestore() {
        db.collection("files")
                .orderBy("uploadedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fileList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        FileModel file = doc.toObject(FileModel.class);
                        fileList.add(file);
                    }
                    fileAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load files", Toast.LENGTH_SHORT).show();
                });
    }

    // Get file name from URI
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getActivity().getContentResolver()
                    .query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
}