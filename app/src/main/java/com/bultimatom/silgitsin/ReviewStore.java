package com.bultimatom.silgitsin;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class ReviewStore {
    private static final String PREFS_NAME = "gallery_cleaner_review_store";
    private static final String KEY_REVIEWED_IDS = "reviewed_ids";
    private static final String KEY_TOTAL_KEPT = "total_kept";
    private static final String KEY_TOTAL_DELETED = "total_deleted";
    private static final String KEY_FAVORITE_IDS = "favorite_ids";

    private final SharedPreferences prefs;

    public ReviewStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public Set<String> getReviewedIds() {
        return new HashSet<>(prefs.getStringSet(KEY_REVIEWED_IDS, new HashSet<>()));
    }

    public void markReviewed(long photoId) {
        Set<String> ids = getReviewedIds();
        ids.add(String.valueOf(photoId));
        prefs.edit().putStringSet(KEY_REVIEWED_IDS, ids).apply();
    }

    public void unmarkReviewed(long photoId) {
        Set<String> ids = getReviewedIds();
        ids.remove(String.valueOf(photoId));
        prefs.edit().putStringSet(KEY_REVIEWED_IDS, ids).apply();
    }

    public void addKept() {
        prefs.edit().putInt(KEY_TOTAL_KEPT, getTotalKept() + 1).apply();
    }

    public void removeKept() {
        prefs.edit().putInt(KEY_TOTAL_KEPT, Math.max(getTotalKept() - 1, 0)).apply();
    }

    public void addDeleted() {
        prefs.edit().putInt(KEY_TOTAL_DELETED, getTotalDeleted() + 1).apply();
    }

    public void removeDeleted() {
        prefs.edit().putInt(KEY_TOTAL_DELETED, Math.max(getTotalDeleted() - 1, 0)).apply();
    }

    public int getTotalKept() {
        return prefs.getInt(KEY_TOTAL_KEPT, 0);
    }

    public int getTotalDeleted() {
        return prefs.getInt(KEY_TOTAL_DELETED, 0);
    }

    public int getReviewedCount() {
        return getReviewedIds().size();
    }

    public void resetReviewedPhotos() {
        prefs.edit().remove(KEY_REVIEWED_IDS).apply();
    }

    public Set<String> getFavoriteIds() {
        return new HashSet<>(prefs.getStringSet(KEY_FAVORITE_IDS, new HashSet<>()));
    }

    public void addFavorite(long photoId) {
        Set<String> ids = getFavoriteIds();
        ids.add(String.valueOf(photoId));
        prefs.edit().putStringSet(KEY_FAVORITE_IDS, ids).apply();
    }

    public void removeFavorite(long photoId) {
        Set<String> ids = getFavoriteIds();
        ids.remove(String.valueOf(photoId));
        prefs.edit().putStringSet(KEY_FAVORITE_IDS, ids).apply();
    }

    public int getFavoriteCount() {
        return getFavoriteIds().size();
    }
}
