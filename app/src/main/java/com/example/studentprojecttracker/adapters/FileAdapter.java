package com.example.studentprojecttracker.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.models.FileModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<FileModel> fileList;

    public FileAdapter(List<FileModel> fileList) {
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileModel file = fileList.get(position);

        holder.tvFileName.setText(file.getName());

        // Format upload date
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String dateStr = sdf.format(new Date(file.getUploadedAt()));
        holder.tvFileInfo.setText("Uploaded on " + dateStr);

        // Open file when clicked
        holder.itemView.setOnClickListener(v -> openFile(v.getContext(), file.getUrl()));

        // Download button
        holder.btnDownload.setOnClickListener(v -> openFile(v.getContext(), file.getUrl()));
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    private void openFile(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Unable to open file", Toast.LENGTH_SHORT).show();
        }
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFileIcon;
        TextView tvFileName;
        TextView tvFileInfo;
        ImageView btnDownload;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFileIcon = itemView.findViewById(R.id.imgFileIcon);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileInfo = itemView.findViewById(R.id.tvFileInfo);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
    }
}