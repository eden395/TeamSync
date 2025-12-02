package com.student.teamsync.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.student.teamsync.R;
import com.student.teamsync.models.ProjectFile;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<ProjectFile> fileList;
    private OnFileClickListener listener;

    public interface OnFileClickListener {
        void onDownloadClick(ProjectFile file);
    }

    public FileAdapter(List<ProjectFile> fileList, OnFileClickListener listener) {
        this.fileList = fileList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        ProjectFile file = fileList.get(position);
        holder.bind(file);
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    class FileViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgFileIcon, btnDownload;
        private TextView tvFileName, tvFileInfo;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFileIcon = itemView.findViewById(R.id.imgFileIcon);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileInfo = itemView.findViewById(R.id.tvFileInfo);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }

        public void bind(ProjectFile file) {
            tvFileName.setText(file.getFileName());
            tvFileInfo.setText(file.getUploadedBy() + " • " + file.getUploadDate());

            btnDownload.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDownloadClick(file);
                }
            });
        }
    }
}