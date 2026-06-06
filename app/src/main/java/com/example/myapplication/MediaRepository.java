package com.example.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
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
        media.removeIf(item -> skippedIds.contains(String.valueOf(item.getId())));
        Collections.shuffle(media);
        return media;
    }

    public List<PhotoItem> loadPhotosByIds(Set<String> ids) {
        List<PhotoItem> selectedPhotos = new ArrayList<>();
        for (PhotoItem photo : loadAllMedia()) {
            if (ids.contains(String.valueOf(photo.getId()))) {
                selectedPhotos.add(photo);
            }
        }
        return selectedPhotos;
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

        try (Cursor cursor = context.getContentResolver()
                .query(collection, projection, null, null, sortOrder)) {
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
