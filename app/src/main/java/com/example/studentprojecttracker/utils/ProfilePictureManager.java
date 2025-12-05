package com.example.studentprojecttracker.utils;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton class to manage profile picture loading and caching
 */
public class ProfilePictureManager {
    private static final String TAG = "ProfilePictureManager";
    private static ProfilePictureManager instance;

    private FirestoreHelper firestoreHelper;
    private Map<String, String> photoUrlCache; // userId -> photoUrl

    private ProfilePictureManager() {
        this.firestoreHelper = new FirestoreHelper();
        this.photoUrlCache = new HashMap<>();
    }

    public static synchronized ProfilePictureManager getInstance() {
        if (instance == null) {
            instance = new ProfilePictureManager();
        }
        return instance;
    }

    /**
     * Load profile picture from Firebase Auth or Firestore
     * @param context Context for Glide
     * @param userId User ID or email
     * @param imageView Target ImageView
     */
    /**
     * Load profile picture from Firebase Auth or Firestore
     * @param context Context for Glide
     * @param userId User ID or email
     * @param imageView Target ImageView
     */
    public void loadProfilePicture(Context context, String userId, ImageView imageView) {
        if (context == null || userId == null || imageView == null) {
            return;
        }

        // Check if we have cached URL
        if (photoUrlCache.containsKey(userId)) {
            String photoUrl = photoUrlCache.get(userId);
            if (photoUrl != null && !photoUrl.isEmpty()) {
                loadImageWithGlide(context, photoUrl, imageView);
                return;
            }
        }

        // Check if it's the current user - use Firebase Auth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && userId.equals(currentUser.getEmail())) {
            if (currentUser.getPhotoUrl() != null) {
                String photoUrl = currentUser.getPhotoUrl().toString();
                photoUrlCache.put(userId, photoUrl);
                loadImageWithGlide(context, photoUrl, imageView);
                return;
            }
        }

        // Load from Firestore - use email query if it looks like an email
        if (userId.contains("@")) {
            loadProfilePictureByEmail(context, userId, imageView);
        } else {
            loadProfilePictureFromFirestore(context, userId, imageView);
        }
    }

    /**
     * Load profile picture from Firestore by email
     */
    private void loadProfilePictureByEmail(Context context, String email, ImageView imageView) {
        firestoreHelper.getUserProfileByEmail(email, new FirestoreHelper.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                    photoUrlCache.put(email, user.getPhotoUrl());
                    loadImageWithGlide(context, user.getPhotoUrl(), imageView);
                } else {
                    imageView.setImageResource(R.drawable.ic_avatar_placeholder);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading profile picture for " + email + ": " + error);
                imageView.setImageResource(R.drawable.ic_avatar_placeholder);
            }
        });
    }

    /**
     * Load profile picture directly from URL
     * @param context Context for Glide
     * @param photoUrl Photo URL
     * @param imageView Target ImageView
     */
    public void loadProfilePictureFromUrl(Context context, String photoUrl, ImageView imageView) {
        if (context == null || photoUrl == null || imageView == null) {
            return;
        }

        if (photoUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_avatar_placeholder);
            return;
        }

        loadImageWithGlide(context, photoUrl, imageView);
    }

    /**
     * Load profile picture from Firestore
     */
    private void loadProfilePictureFromFirestore(Context context, String userId, ImageView imageView) {
        firestoreHelper.getUserProfile(userId, new FirestoreHelper.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                    photoUrlCache.put(userId, user.getPhotoUrl());
                    loadImageWithGlide(context, user.getPhotoUrl(), imageView);
                } else {
                    imageView.setImageResource(R.drawable.ic_avatar_placeholder);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading profile picture for " + userId + ": " + error);
                imageView.setImageResource(R.drawable.ic_avatar_placeholder);
            }
        });
    }

    /**
     * Load image using Glide with circular crop and placeholder
     */
    private void loadImageWithGlide(Context context, String photoUrl, ImageView imageView) {
        Glide.with(context)
                .load(photoUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_avatar_placeholder)
                .error(R.drawable.ic_avatar_placeholder)
                .into(imageView);
    }

    /**
     * Preload profile pictures for a list of user IDs
     * This improves performance when showing lists of users
     * @param context Context
     * @param userIds List of user IDs to preload
     */
    public void preloadProfilePictures(Context context, List<String> userIds) {
        if (context == null || userIds == null || userIds.isEmpty()) {
            return;
        }

        for (String userId : userIds) {
            if (!photoUrlCache.containsKey(userId)) {
                firestoreHelper.getUserProfile(userId, new FirestoreHelper.UserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                            photoUrlCache.put(userId, user.getPhotoUrl());
                        }
                    }

                    @Override
                    public void onError(String error) {
                        // Silently fail for preloading
                    }
                });
            }
        }
    }

    /**
     * Clear cache for a specific user
     * Call this after a user updates their profile picture
     * @param userId User ID to clear
     */
    public void clearCache(String userId) {
        if (userId != null) {
            photoUrlCache.remove(userId);
        }
    }

    /**
     * Clear entire cache
     * Call this on logout
     */
    public void clearAllCache() {
        photoUrlCache.clear();
    }

    /**
     * Manually cache a photo URL
     * @param userId User ID
     * @param photoUrl Photo URL
     */
    public void cachePhotoUrl(String userId, String photoUrl) {
        if (userId != null && photoUrl != null) {
            photoUrlCache.put(userId, photoUrl);
        }
    }
}