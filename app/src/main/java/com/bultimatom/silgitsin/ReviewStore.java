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
    private static final String KEY_TOTAL_SAVED_BYTES = "total_saved_bytes";
    private static final String KEY_FAVORITE_IDS = "favorite_ids";

    private final SharedPreferences prefs;

    public ReviewStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public Set<String> getReviewedIds() {
        return new HashSet<>(prefs.getStringSet(KEY_REVIEWED_IDS, new HashSet<>()));
    }

    public void markReviewed(PhotoItem photo) {
        Set<String> ids = getReviewedIds();
        ids.add(photo.getStorageKey());
        ids.remove(String.valueOf(photo.getId()));
        prefs.edit().putStringSet(KEY_REVIEWED_IDS, ids).apply();
    }

    public void unmarkReviewed(PhotoItem photo) {
        Set<String> ids = getReviewedIds();
        ids.remove(photo.getStorageKey());
        ids.remove(String.valueOf(photo.getId()));
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

    public void addSavedBytes(long bytes) {
        if (bytes <= 0L) {
            return;
        }
        prefs.edit().putLong(KEY_TOTAL_SAVED_BYTES, getTotalSavedBytes() + bytes).apply();
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

    public long getTotalSavedBytes() {
        return prefs.getLong(KEY_TOTAL_SAVED_BYTES, 0L);
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

    public boolean isFavorite(PhotoItem photo) {
        Set<String> ids = getFavoriteIds();
        return ids.contains(photo.getStorageKey()) || ids.contains(String.valueOf(photo.getId()));
    }

    public void addFavorite(PhotoItem photo) {
        Set<String> ids = getFavoriteIds();
        ids.add(photo.getStorageKey());
        ids.remove(String.valueOf(photo.getId()));
        prefs.edit().putStringSet(KEY_FAVORITE_IDS, ids).apply();
    }

    public void removeFavorite(PhotoItem photo) {
        Set<String> ids = getFavoriteIds();
        ids.remove(photo.getStorageKey());
        ids.remove(String.valueOf(photo.getId()));
        prefs.edit().putStringSet(KEY_FAVORITE_IDS, ids).apply();
    }

    public int getFavoriteCount() {
        return getFavoriteIds().size();
    }
}
