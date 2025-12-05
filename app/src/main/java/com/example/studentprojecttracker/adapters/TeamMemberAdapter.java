package com.example.studentprojecttracker.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.models.TeamMember;
import com.example.studentprojecttracker.models.User;
import com.example.studentprojecttracker.utils.FirestoreHelper;
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
            // Display member name
            displayMemberName(member);

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
         * Display member name with Firestore lookup if needed
         */
        private void displayMemberName(TeamMember member) {
            String name = member.getName();
            String email = member.getEmail();
            String userId = member.getUserId();

            Log.d("TeamMemberAdapter", "Member - Name: " + name + ", Email: " + email + ", UserId: " + userId);

            // If we have a valid display name (not email-like), use it
            if (name != null && !name.isEmpty() && !name.contains("@")) {
                memberNameText.setText(name);
                return;
            }

            // Show placeholder while loading
            String placeholder = extractUsernameFromEmail(email != null ? email : userId);
            memberNameText.setText(placeholder);

            // Try to load actual name from Firestore
            String identifier = email != null ? email : userId;
            if (identifier != null && !identifier.isEmpty()) {
                Log.d("TeamMemberAdapter", "Loading name from Firestore for: " + identifier);

                FirestoreHelper firestoreHelper = new FirestoreHelper();

                // Use email query since we're dealing with emails
                if (identifier.contains("@")) {
                    firestoreHelper.getUserProfileByEmail(identifier, new FirestoreHelper.UserCallback() {
                        @Override
                        public void onSuccess(User user) {
                            String displayName = user.getName();
                            Log.d("TeamMemberAdapter", "Firestore SUCCESS - Name: " + displayName);

                            if (displayName != null && !displayName.isEmpty() && !displayName.contains("@")) {
                                memberNameText.setText(displayName);
                            }
                            // If name is still email-like, keep the placeholder
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("TeamMemberAdapter", "Firestore ERROR: " + error);
                            // Keep placeholder on error
                        }
                    });
                } else {
                    // Try by userId if not an email
                    firestoreHelper.getUserProfile(identifier, new FirestoreHelper.UserCallback() {
                        @Override
                        public void onSuccess(User user) {
                            String displayName = user.getName();
                            Log.d("TeamMemberAdapter", "Firestore SUCCESS - Name: " + displayName);

                            if (displayName != null && !displayName.isEmpty() && !displayName.contains("@")) {
                                memberNameText.setText(displayName);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("TeamMemberAdapter", "Firestore ERROR: " + error);
                        }
                    });
                }
            }
        }

        /**
         * Extract and capitalize username from email
         */
        private String extractUsernameFromEmail(String email) {
            if (email != null && email.contains("@")) {
                String username = email.substring(0, email.indexOf("@"));
                if (!username.isEmpty()) {
                    return username.substring(0, 1).toUpperCase() + username.substring(1);
                }
                return username;
            }
            return email != null ? email : "Unknown User";
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