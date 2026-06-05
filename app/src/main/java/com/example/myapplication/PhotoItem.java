package com.example.myapplication;

import android.net.Uri;

import java.util.Locale;

public class PhotoItem {
    private final long id;
    private final Uri uri;
    private final String displayName;
    private final long dateModified;
    private final long size;

    public PhotoItem(long id, Uri uri, String displayName, long dateModified, long size) {
        this.id = id;
        this.uri = uri;
        this.displayName = displayName;
        this.dateModified = dateModified;
        this.size = size;
    }

    public long getId() {
        return id;
    }

    public Uri getUri() {
        return uri;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getDateModified() {
        return dateModified;
    }

    public long getSize() {
        return size;
    }

    public String getReadableSize() {
        double kb = size / 1024.0;
        if (kb < 1024) {
            return String.format(Locale.getDefault(), "%.0f KB", kb);
        }
        return String.format(Locale.getDefault(), "%.1f MB", kb / 1024.0);
    }
}
