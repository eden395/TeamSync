package com.student.teamsync.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.student.teamsync.R;
import com.student.teamsync.models.TeamMember;

import java.util.List;

public class TeamMemberAdapter extends RecyclerView.Adapter<TeamMemberAdapter.TeamMemberViewHolder> {

    private List<TeamMember> teamMemberList;

    public TeamMemberAdapter(List<TeamMember> teamMemberList) {
        this.teamMemberList = teamMemberList;
    }

    @NonNull
    @Override
    public TeamMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team_member, parent, false);
        return new TeamMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamMemberViewHolder holder, int position) {
        TeamMember member = teamMemberList.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return teamMemberList.size();
    }

    static class TeamMemberViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatarImage;
        private TextView memberNameText;
        private TextView statusBadge;
        private TextView tasksAssignedText;
        private TextView completionText;

        public TeamMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.avatarImage);
            memberNameText = itemView.findViewById(R.id.memberNameText);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            tasksAssignedText = itemView.findViewById(R.id.tasksAssignedText);
            completionText = itemView.findViewById(R.id.completionText);
        }

        public void bind(TeamMember member) {
            memberNameText.setText(member.getName());
            statusBadge.setText(member.getStatus());
            tasksAssignedText.setText(String.valueOf(member.getTasksAssigned()));
            completionText.setText(member.getCompletionPercentage() + "%");

            // Set status badge background
            Drawable background;
            if ("Ahead".equals(member.getStatus())) {
                background = ContextCompat.getDrawable(itemView.getContext(), R.drawable.badge_ahead);
            } else {
                background = ContextCompat.getDrawable(itemView.getContext(), R.drawable.badge_on_track);
            }
            statusBadge.setBackground(background);
        }
    }
}
