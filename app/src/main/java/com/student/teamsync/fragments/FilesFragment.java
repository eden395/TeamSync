package com.student.teamsync.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.student.teamsync.R;
import com.student.teamsync.adapters.FileAdapter;
import com.student.teamsync.models.ProjectFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class FilesFragment extends Fragment implements FileAdapter.OnFileClickListener {

    private RecyclerView filesRecyclerView;
    private MaterialButton uploadButton;
    private FileAdapter fileAdapter;
    private List<ProjectFile> fileList;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register file picker
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            handleFileUpload(fileUri);
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
        loadSampleFiles();

        return view;
    }

    private void initializeViews(View view) {
        filesRecyclerView = view.findViewById(R.id.filesRecyclerView);
        uploadButton = view.findViewById(R.id.uploadButton);

        uploadButton.setOnClickListener(v -> openFilePicker());
    }

    private void setupRecyclerView() {
        fileList = new ArrayList<>();
        fileAdapter = new FileAdapter(fileList, this);
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        filesRecyclerView.setAdapter(fileAdapter);
    }

    private void loadSampleFiles() {
        // Sample files
        fileList.add(new ProjectFile(
                "1",
                "Project_Proposal_Final.pdf",
                "url",
                "Christine",
                "10/5/2025",
                "project1"
        ));
        fileList.add(new ProjectFile(
                "2",
                "Requirements_Document.docx",
                "url",
                "Eden",
                "10/6/2025",
                "project1"
        ));
        fileAdapter.notifyDataSetChanged();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select File"));
    }

    private void handleFileUpload(Uri fileUri) {
        // Get file name
        String fileName = getFileName(fileUri);

        // In real app, upload to Firebase Storage here
        String fileId = UUID.randomUUID().toString();
        String currentDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date());

        ProjectFile newFile = new ProjectFile(
                fileId,
                fileName,
                fileUri.toString(),
                "Current User", // Replace with actual user name
                currentDate,
                "project1" // Replace with actual project ID
        );

        fileList.add(0, newFile);
        fileAdapter.notifyItemInserted(0);
        filesRecyclerView.scrollToPosition(0);

        Toast.makeText(getContext(), "File uploaded: " + fileName, Toast.LENGTH_SHORT).show();
    }

    private String getFileName(Uri uri) {
        String fileName = "Unknown";
        if (uri.getScheme().equals("content")) {
            android.database.Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        }
        if (fileName.equals("Unknown")) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }

    @Override
    public void onDownloadClick(ProjectFile file) {
        Toast.makeText(getContext(), "Downloading: " + file.getFileName(), Toast.LENGTH_SHORT).show();
        // In real app, implement download functionality
    }
}