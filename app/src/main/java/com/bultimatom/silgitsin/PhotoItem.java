package com.bultimatom.silgitsin;

import android.net.Uri;

import java.util.Locale;

public class PhotoItem {
    private final long id;
    private final Uri uri;
    private final String displayName;
    private final String contextLabel;
    private final long dateModified;
    private final long size;
    private final boolean video;

    public PhotoItem(long id, Uri uri, String displayName, long dateModified, long size) {
        this(id, uri, displayName, "", dateModified, size, false);
    }

    public PhotoItem(long id, Uri uri, String displayName, String contextLabel, long dateModified, long size) {
        this(id, uri, displayName, contextLabel, dateModified, size, false);
    }

    public PhotoItem(long id, Uri uri, String displayName, String contextLabel, long dateModified, long size, boolean video) {
        this.id = id;
        this.uri = uri;
        this.displayName = displayName;
        this.contextLabel = contextLabel == null ? "" : contextLabel;
        this.dateModified = dateModified;
        this.size = size;
        this.video = video;
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

    public String getContextLabel() {
        return contextLabel;
    }

    public long getDateModified() {
        return dateModified;
    }

    public long getSize() {
        return size;
    }

    public boolean isVideo() {
        return video;
    }

    public String getStorageKey() {
        return uri.toString();
    }

    public String getDuplicateKey() {
        return displayName.toLowerCase(Locale.ROOT).trim() + "|" + size + "|" + video;
    }

    public String getReadableSize() {
        double kb = size / 1024.0;
        if (kb < 1024) {
            return String.format(Locale.getDefault(), "%.0f KB", kb);
        }
        return String.format(Locale.getDefault(), "%.1f MB", kb / 1024.0);
    }
}
