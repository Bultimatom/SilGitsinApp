package com.bultimatom.silgitsin;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MediaRepository {
    private final Context context;

    public MediaRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public List<PhotoItem> loadShuffledPhotos() {
        return loadShuffledPhotos(Collections.emptySet());
    }

    public List<PhotoItem> loadShuffledPhotos(Set<String> skippedIds) {
        List<PhotoItem> media = loadAllMedia();
        media.removeIf(item -> skippedIds.contains(item.getStorageKey())
                || skippedIds.contains(String.valueOf(item.getId())));
        Collections.shuffle(media);
        return media;
    }

    public List<PhotoItem> loadPhotosByIds(Set<String> ids) {
        List<PhotoItem> selectedPhotos = new ArrayList<>();
        for (PhotoItem photo : loadAllMedia()) {
            if (ids.contains(photo.getStorageKey()) || ids.contains(String.valueOf(photo.getId()))) {
                selectedPhotos.add(photo);
            }
        }
        return selectedPhotos;
    }

    public boolean exists(PhotoItem photo) {
        String[] projection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            projection = new String[] {
                    MediaStore.MediaColumns._ID,
                    MediaStore.MediaColumns.IS_TRASHED
            };
        } else {
            projection = new String[] { MediaStore.MediaColumns._ID };
        }

        try (Cursor cursor = context.getContentResolver().query(
                photo.getUri(),
                projection,
                null,
                null,
                null
        )) {
            if (cursor == null || !cursor.moveToFirst()) {
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                int trashedColumn = cursor.getColumnIndex(MediaStore.MediaColumns.IS_TRASHED);
                return trashedColumn < 0 || cursor.getInt(trashedColumn) == 0;
            }
            return true;
        } catch (Exception ignored) {
            return true;
        }
    }

    private List<PhotoItem> loadAllMedia() {
        List<PhotoItem> media = new ArrayList<>();
        media.addAll(loadMedia(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_MODIFIED + " DESC",
                false
        ));
        media.addAll(loadMedia(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_MODIFIED + " DESC",
                true
        ));
        media.sort((left, right) -> Long.compare(right.getDateModified(), left.getDateModified()));
        Set<String> seenUris = new HashSet<>();
        media.removeIf(item -> !seenUris.add(item.getStorageKey()));
        return media;
    }

    private List<PhotoItem> loadMedia(
            Uri collection,
            String idField,
            String nameField,
            String bucketField,
            String dateField,
            String sizeField,
            String sortOrder,
            boolean video
    ) {
        String[] projection = new String[] {
                idField,
                nameField,
                bucketField,
                dateField,
                sizeField
        };
        List<PhotoItem> items = new ArrayList<>();

        String selection = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            selection = MediaStore.MediaColumns.IS_PENDING + " = 0 AND "
                    + MediaStore.MediaColumns.IS_TRASHED + " = 0";
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = MediaStore.MediaColumns.IS_PENDING + " = 0";
        }

        try (Cursor cursor = context.getContentResolver()
                .query(collection, projection, selection, null, sortOrder)) {
            if (cursor == null) {
                return items;
            }

            int idColumn = cursor.getColumnIndexOrThrow(idField);
            int nameColumn = cursor.getColumnIndexOrThrow(nameField);
            int bucketColumn = cursor.getColumnIndexOrThrow(bucketField);
            int dateColumn = cursor.getColumnIndexOrThrow(dateField);
            int sizeColumn = cursor.getColumnIndexOrThrow(sizeField);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                Uri uri = Uri.withAppendedPath(collection, String.valueOf(id));
                String name = cursor.getString(nameColumn);
                String bucket = cursor.getString(bucketColumn);

                items.add(new PhotoItem(
                        id,
                        uri,
                        name == null ? (video ? "Video" : "Fotograf") : name,
                        buildContextLabel(bucket, video ? "Video" : ""),
                        cursor.getLong(dateColumn),
                        cursor.getLong(sizeColumn),
                        video
                ));
            }
        }

        return items;
    }

    private String buildContextLabel(String bucket, String fallback) {
        if (bucket == null) {
            return fallback;
        }
        String clean = bucket.trim();
        if (clean.isEmpty()
                || clean.equalsIgnoreCase("Camera")
                || clean.equalsIgnoreCase("Screenshots")
                || clean.equalsIgnoreCase("Download")) {
            return fallback;
        }
        return clean;
    }
}
