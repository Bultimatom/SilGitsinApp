package com.bultimatom.silgitsin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Size;

import androidx.annotation.Nullable;

import java.io.InputStream;

final class MediaThumbnailLoader {
    private MediaThumbnailLoader() {
    }

    @Nullable
    static Bitmap load(Context context, PhotoItem item, int width, int height) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return context.getContentResolver().loadThumbnail(
                        item.getUri(),
                        new Size(width, height),
                        null
                );
            }
            return item.isVideo()
                    ? MediaStore.Video.Thumbnails.getThumbnail(
                    context.getContentResolver(),
                    item.getId(),
                    MediaStore.Video.Thumbnails.MINI_KIND,
                    null
            )
                    : MediaStore.Images.Thumbnails.getThumbnail(
                    context.getContentResolver(),
                    item.getId(),
                    MediaStore.Images.Thumbnails.MINI_KIND,
                    null
            );
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    static Bitmap loadForCard(Context context, PhotoItem item) {
        if (item.isVideo()) {
            return load(context, item, 1200, 1600);
        }
        return decodeImage(context, item, 1440, 2200);
    }

    @Nullable
    static Bitmap loadPreview(Context context, PhotoItem item) {
        if (item.isVideo()) {
            return load(context, item, 1600, 2200);
        }
        return decodeImage(context, item, 2160, 3840);
    }

    @Nullable
    private static Bitmap decodeImage(Context context, PhotoItem item, int reqWidth, int reqHeight) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream input = context.getContentResolver().openInputStream(item.getUri())) {
                BitmapFactory.decodeStream(input, null, bounds);
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return load(context, item, reqWidth, reqHeight);
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = calculateInSampleSize(bounds, reqWidth, reqHeight);
            try (InputStream input = context.getContentResolver().openInputStream(item.getUri())) {
                return BitmapFactory.decodeStream(input, null, options);
            }
        } catch (Throwable ignored) {
            return load(context, item, reqWidth, reqHeight);
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return Math.max(1, inSampleSize);
    }
}
