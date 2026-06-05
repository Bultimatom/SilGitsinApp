package com.example.myapplication;

import android.app.Activity;
import android.app.RecoverableSecurityException;
import android.content.IntentSender;
import android.os.Build;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;

import java.util.ArrayList;
import java.util.List;

public final class MediaDeleteHelper {
    private MediaDeleteHelper() {
    }

    public static boolean requestDelete(
            Activity activity,
            List<PhotoItem> photos,
            ActivityResultLauncher<IntentSenderRequest> launcher
    ) {
        if (photos.isEmpty()) {
            return true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            IntentSender sender = MediaStore
                    .createDeleteRequest(activity.getContentResolver(), collectUris(photos))
                    .getIntentSender();
            launcher.launch(new IntentSenderRequest.Builder(sender).build());
            return false;
        }

        try {
            boolean allDeleted = true;
            for (PhotoItem photo : photos) {
                allDeleted = activity.getContentResolver().delete(photo.getUri(), null, null) > 0 && allDeleted;
            }
            return allDeleted;
        } catch (SecurityException exception) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q
                    && exception instanceof RecoverableSecurityException) {
                IntentSender sender = ((RecoverableSecurityException) exception)
                        .getUserAction()
                        .getActionIntent()
                        .getIntentSender();
                launcher.launch(new IntentSenderRequest.Builder(sender).build());
            }
            return false;
        }
    }

    private static List<android.net.Uri> collectUris(List<PhotoItem> photos) {
        List<android.net.Uri> uris = new ArrayList<>();
        for (PhotoItem photo : photos) {
            uris.add(photo.getUri());
        }
        return uris;
    }
}
