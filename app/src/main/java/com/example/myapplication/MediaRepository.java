package com.example.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MediaRepository {
    private final Context context;

    public MediaRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public List<PhotoItem> loadShuffledPhotos() {
        return loadShuffledPhotos(Collections.emptySet());
    }

    public List<PhotoItem> loadShuffledPhotos(java.util.Set<String> skippedIds) {
        Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.SIZE
        };
        String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
        List<PhotoItem> photos = new ArrayList<>();

        try (Cursor cursor = context.getContentResolver()
                .query(collection, projection, null, null, sortOrder)) {
            if (cursor == null) {
                return photos;
            }

            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            int dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                Uri uri = Uri.withAppendedPath(collection, String.valueOf(id));
                String name = cursor.getString(nameColumn);
                if (skippedIds.contains(String.valueOf(id))) {
                    continue;
                }

                photos.add(new PhotoItem(
                        id,
                        uri,
                        name == null ? "Fotoğraf" : name,
                        cursor.getLong(dateColumn),
                        cursor.getLong(sizeColumn)
                ));
            }
        }

        Collections.shuffle(photos);
        return photos;
    }
}
