package com.example.studentprojecttracker.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.models.MemberProgress;

import java.util.List;

public class MemberProgressAdapter extends RecyclerView.Adapter<MemberProgressAdapter.MemberProgressViewHolder> {

    private List<MemberProgress> memberProgressList;

    public MemberProgressAdapter(List<MemberProgress> memberProgressList) {
        this.memberProgressList = memberProgressList;
    }

    @NonNull
    @Override
    public MemberProgressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_progress, parent, false);
        return new MemberProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberProgressViewHolder holder, int position) {
        MemberProgress member = memberProgressList.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return memberProgressList.size();
    }

    static class MemberProgressViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgAvatar;
        private TextView tvMemberName, tvMemberEmail, tvTaskProgress;
        private TextView tvStatusBadge, tvProgressPercentage;

        public MemberProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberEmail = itemView.findViewById(R.id.tvMemberEmail);
            tvTaskProgress = itemView.findViewById(R.id.tvTaskProgress);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvProgressPercentage = itemView.findViewById(R.id.tvProgressPercentage);
        }

        public void bind(MemberProgress member) {
            tvMemberName.setText(member.getMemberName());
            tvMemberEmail.setText(member.getMemberEmail());
            tvTaskProgress.setText(member.getTasksCompleted() + "/" + member.getTasksTotal() + " tasks completed");
            tvStatusBadge.setText(member.getStatus());
            tvProgressPercentage.setText(member.getProgressPercentage() + "%");

            // Set status badge background
            Drawable background;
            if ("Ahead".equals(member.getStatus())) {
                background = ContextCompat.getDrawable(itemView.getContext(), R.drawable.badge_ahead);
            } else if ("Behind".equals(member.getStatus())) {
                background = ContextCompat.getDrawable(itemView.getContext(), R.drawable.badge_high); // Red/Orange
            } else {
                background = ContextCompat.getDrawable(itemView.getContext(), R.drawable.badge_on_track);
            }
            tvStatusBadge.setBackground(background);
        }
    }
}