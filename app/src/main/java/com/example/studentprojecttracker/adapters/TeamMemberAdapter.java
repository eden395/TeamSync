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
import com.example.studentprojecttracker.models.TeamMember;
import com.example.studentprojecttracker.utils.ProfilePictureManager;

import java.util.List;

public class TeamMemberAdapter extends RecyclerView.Adapter<TeamMemberAdapter.TeamMemberViewHolder> {

    private List<TeamMember> teamMemberList;
    private ProfilePictureManager profilePictureManager;

    public TeamMemberAdapter(List<TeamMember> teamMemberList) {
        this.teamMemberList = teamMemberList;
        this.profilePictureManager = ProfilePictureManager.getInstance();
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
        holder.bind(member, profilePictureManager);
    }

    @Override
    public int getItemCount() {
        return teamMemberList.size();
    }

    public void updateData(List<TeamMember> newList) {
        this.teamMemberList.clear();
        this.teamMemberList.addAll(newList);
        notifyDataSetChanged();
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

        public void bind(TeamMember member, ProfilePictureManager pictureManager) {
            // Get display name (prioritize proper name over email)
            String displayName = getDisplayName(member);
            memberNameText.setText(displayName);

            statusBadge.setText(member.getStatus());
            tasksAssignedText.setText(String.valueOf(member.getTasksAssigned()));
            completionText.setText(member.getCompletionPercentage() + "%");

            // Set status badge background
            int backgroundResource = getStatusBackground(member.getStatus());
            statusBadge.setBackgroundResource(backgroundResource);

            // Load profile picture using ProfilePictureManager
            loadProfilePicture(member, pictureManager);
        }

        /**
         * Get proper display name from TeamMember
         * Priority: Name (if not email-like) > Username from email > "Unknown User"
         */
        private String getDisplayName(TeamMember member) {
            String name = member.getName();

            // Check if name is valid (not null, not empty, not an email)
            if (name != null && !name.isEmpty() && !name.contains("@")) {
                return name;
            }

            // Try to extract username from email
            String email = member.getEmail();
            if (email != null && !email.isEmpty()) {
                if (email.contains("@")) {
                    // Extract part before @
                    String username = email.substring(0, email.indexOf("@"));
                    // Capitalize first letter
                    if (!username.isEmpty()) {
                        return username.substring(0, 1).toUpperCase() + username.substring(1);
                    }
                    return username;
                }
                // Email doesn't contain @, use as is
                return email;
            }

            // No valid name or email found
            return "Unknown User";
        }

        /**
         * Get background resource based on status
         */
        private int getStatusBackground(String status) {
            if ("Ahead".equals(status)) {
                return R.drawable.badge_ahead;
            } else if ("Behind".equals(status)) {
                return R.drawable.badge_behind;
            } else {
                // "On Track" or default
                return R.drawable.badge_on_track;
            }
        }

        /**
         * Load profile picture with fallback chain
         */
        private void loadProfilePicture(TeamMember member, ProfilePictureManager pictureManager) {
            // Priority: photoUrl > userId > email > placeholder
            if (member.getPhotoUrl() != null && !member.getPhotoUrl().isEmpty()) {
                pictureManager.loadProfilePictureFromUrl(
                        itemView.getContext(),
                        member.getPhotoUrl(),
                        avatarImage
                );
            } else if (member.getUserId() != null && !member.getUserId().isEmpty()) {
                pictureManager.loadProfilePicture(
                        itemView.getContext(),
                        member.getUserId(),
                        avatarImage
                );
            } else if (member.getEmail() != null && !member.getEmail().isEmpty()) {
                pictureManager.loadProfilePicture(
                        itemView.getContext(),
                        member.getEmail(),
                        avatarImage
                );
            } else {
                avatarImage.setImageResource(R.drawable.ic_avatar_placeholder);
            }
        }
    }
}