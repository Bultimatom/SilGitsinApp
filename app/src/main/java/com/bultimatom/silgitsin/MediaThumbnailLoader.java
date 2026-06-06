package com.bultimatom.silgitsin;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Size;

import androidx.annotation.Nullable;

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
}
