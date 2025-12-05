package com.example.studentprojecttracker.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

import com.example.studentprojecttracker.activities.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.adapters.FileAdapter;
import com.example.studentprojecttracker.models.FileModel;
import com.example.studentprojecttracker.models.Project;
import com.example.studentprojecttracker.utils.FirestoreHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FilesFragment extends Fragment {

    private static final String TAG = "FilesFragment";

    private RecyclerView filesRecyclerView;
    private MaterialButton uploadFileButton;
    private FileAdapter fileAdapter;
    private List<FileModel> fileList;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private Project currentProject;
    private FirestoreHelper firestoreHelper;
    private ProgressDialog progressDialog;
    private ListenerRegistration filesListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firestoreHelper = new FirestoreHelper();

        // Get project data from MainActivity
        if (getActivity() instanceof MainActivity) {
            currentProject = ((MainActivity) getActivity()).getCurrentProject();
            if (currentProject != null) {
                Log.d(TAG, "Project received: " + currentProject.getProjectName());
            } else {
                Log.e(TAG, "No project available in MainActivity!");
            }
        }

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);

        initializeViews(view);
        setupRecyclerView();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadFilesFromFirestore();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove Firestore listener when fragment is destroyed
        if (filesListener != null) {
            filesListener.remove();
            filesListener = null;
        }
        // Dismiss progress dialog if showing
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void initializeViews(View view) {
        filesRecyclerView = view.findViewById(R.id.filesRecyclerView);
        uploadFileButton = view.findViewById(R.id.uploadButton);

        uploadFileButton.setOnClickListener(v -> openFilePicker());

        // Initialize progress dialog
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
    }

    private void setupRecyclerView() {
        fileList = new ArrayList<>();
        fileAdapter = new FileAdapter(fileList);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        filesRecyclerView.setAdapter(fileAdapter);
    }

    private void loadFilesFromFirestore() {
        if (currentProject == null) {
            Log.e(TAG, "No current project");
            return;
        }

        if (!isAdded() || getContext() == null) {
            Log.e(TAG, "Fragment not attached");
            return;
        }

        // Remove old listener if exists
        if (filesListener != null) {
            filesListener.remove();
        }

        filesListener = firestoreHelper.getProjectFiles(currentProject.getProjectId(),
                new FirestoreHelper.FileCallback() {
                    @Override
                    public void onSuccess(List<FileModel> files) {
                        if (!isAdded() || getContext() == null) return;

                        fileList.clear();
                        fileList.addAll(files);
                        fileAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded() || getContext() == null) return;

                        Log.e(TAG, "Firestore error: " + error);

                        // Ignore harmless idle connection warnings
                        if (error.contains("CANCELLED") ||
                                error.contains("idle") ||
                                error.contains("Disconnecting idle stream")) {
                            return;  // Don't show these as errors
                        }

                        // Show real errors
                        Toast.makeText(getContext(), "Error loading files: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "image/*",
                "text/plain"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select File"));
    }

    private void uploadFileToFirebase(Uri fileUri) {
        if (currentProject == null) {
            Toast.makeText(getContext(), "No project selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isAdded() || getContext() == null) {
            return;
        }

        progressDialog.setMessage("Uploading file...");
        progressDialog.show();

        // Get file name
        String fileName = getFileName(fileUri);
        if (fileName == null) {
            fileName = "file_" + System.currentTimeMillis();
        }

        // Create unique file name
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

        // Upload to Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference fileRef = storageRef.child("project_files/" + uniqueFileName);

        String finalFileName = fileName;
        fileRef.putFile(fileUri)
                .addOnProgressListener(snapshot -> {
                    if (!isAdded() || getContext() == null || !progressDialog.isShowing()) return;

                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploading: " + (int) progress + "%");
                })
                .addOnSuccessListener(taskSnapshot -> {
                    if (!isAdded() || getContext() == null) return;

                    // Get download URL
                    fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        if (!isAdded() || getContext() == null) return;

                        // Save file metadata to Firestore
                        FileModel file = new FileModel(finalFileName, downloadUri.toString(),
                                System.currentTimeMillis());

                        firestoreHelper.saveFileMetadata(currentProject.getProjectId(), file,
                                new FirestoreHelper.OnCompleteListener() {
                                    @Override
                                    public void onComplete(boolean success, String message) {
                                        if (!isAdded() || getContext() == null) return;

                                        if (progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }

                                        if (success) {
                                            Toast.makeText(getContext(), "File uploaded successfully",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), "Error: " + message,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }).addOnFailureListener(e -> {
                        if (!isAdded() || getContext() == null) return;

                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Log.e(TAG, "Failed to get download URL", e);
                        Toast.makeText(getContext(), "Failed to get file URL", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return;

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Log.e(TAG, "Upload failed", e);
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private String getFileName(Uri uri) {
        if (getContext() == null) return null;

        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContext().getContentResolver()
                    .query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file name", e);
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }
}