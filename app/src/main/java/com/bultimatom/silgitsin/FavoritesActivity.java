package com.bultimatom.silgitsin;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    private MediaRepository mediaRepository;
    private ReviewStore reviewStore;
    private GridLayout gridFavorites;
    private TextView tvEmptyFavorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppPreferences.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        mediaRepository = new MediaRepository(this);
        reviewStore = new ReviewStore(this);
        gridFavorites = findViewById(R.id.gridFavorites);
        tvEmptyFavorites = findViewById(R.id.tvEmptyFavorites);
        findViewById(R.id.btnFavoritesBack).setOnClickListener(view -> finish());
        setupSystemBars();
        loadFavorites();
    }

    private void setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.favoritesRoot), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void loadFavorites() {
        new Thread(() -> {
            List<PhotoItem> photos = mediaRepository.loadPhotosByIds(reviewStore.getFavoriteIds());
            runOnUiThread(() -> renderFavorites(photos));
        }).start();
    }

    private void renderFavorites(List<PhotoItem> photos) {
        gridFavorites.removeAllViews();
        tvEmptyFavorites.setVisibility(photos.isEmpty() ? View.VISIBLE : View.GONE);

        int margin = dpToPx(4);
        int size = (getResources().getDisplayMetrics().widthPixels - dpToPx(48)) / 3;
        for (PhotoItem photo : photos) {
            FrameLayout cell = new FrameLayout(this);

            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(MediaThumbnailLoader.load(this, photo, 420, 420));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_surface));
            imageView.setContentDescription(photo.getDisplayName());
            imageView.setOnClickListener(view -> showPreview(photo));
            cell.addView(imageView, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));

            ImageButton removeFavorite = new ImageButton(this);
            removeFavorite.setImageResource(R.drawable.ic_action_heart);
            removeFavorite.setBackgroundResource(R.drawable.bg_action_ghost);
            removeFavorite.setPadding(dpToPx(7), dpToPx(7), dpToPx(7), dpToPx(7));
            removeFavorite.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            removeFavorite.setContentDescription("Favoriden cikar");
            removeFavorite.setOnClickListener(view -> {
                reviewStore.removeFavorite(photo);
                loadFavorites();
            });
            FrameLayout.LayoutParams heartParams = new FrameLayout.LayoutParams(
                    dpToPx(38),
                    dpToPx(38),
                    android.view.Gravity.TOP | android.view.Gravity.END
            );
            heartParams.setMargins(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
            cell.addView(removeFavorite, heartParams);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(margin, margin, margin, margin);
            gridFavorites.addView(cell, params);
        }
    }

    private void showPreview(PhotoItem photo) {
        if (photo.isVideo()) {
            showVideoPreview(photo);
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        FrameLayout container = new FrameLayout(this);
        container.setBackgroundColor(ContextCompat.getColor(this, R.color.black));

        ZoomableImageView imageView = new ZoomableImageView(this);
        imageView.setImageBitmap(MediaThumbnailLoader.loadPreview(this, photo));
        imageView.setContentDescription(photo.getDisplayName());
        container.addView(imageView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        ImageButton closeButton = new ImageButton(this);
        closeButton.setImageResource(R.drawable.ic_back_custom);
        closeButton.setColorFilter(ContextCompat.getColor(this, R.color.white));
        closeButton.setBackgroundColor(Color.TRANSPARENT);
        closeButton.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        closeButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        closeButton.setContentDescription("Geri");
        closeButton.setOnClickListener(view -> dialog.dismiss());
        FrameLayout.LayoutParams hintParams = new FrameLayout.LayoutParams(
                dpToPx(48),
                dpToPx(48)
        );
        hintParams.setMargins(dpToPx(18), dpToPx(42), dpToPx(18), dpToPx(18));
        container.addView(closeButton, hintParams);

        dialog.setContentView(container);
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            shownWindow.setBackgroundDrawableResource(android.R.color.black);
        }
    }

    private void showVideoPreview(PhotoItem photo) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        FrameLayout container = new FrameLayout(this);
        container.setBackgroundColor(ContextCompat.getColor(this, R.color.black));

        VideoView videoView = new VideoView(this);
        videoView.setVideoURI(photo.getUri());
        container.addView(videoView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setOnPreparedListener(player -> {
            videoView.start();
            mediaController.show(2500);
        });
        videoView.setOnErrorListener((player, what, extra) -> {
            dialog.dismiss();
            return true;
        });

        TextView closeHint = new TextView(this);
        closeHint.setText("Kapat");
        closeHint.setTextColor(ContextCompat.getColor(this, R.color.white));
        closeHint.setTextSize(14f);
        closeHint.setPadding(dpToPx(14), dpToPx(10), dpToPx(14), dpToPx(10));
        closeHint.setBackgroundColor(0x66000000);
        closeHint.setOnClickListener(view -> dialog.dismiss());
        FrameLayout.LayoutParams hintParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        hintParams.setMargins(dpToPx(18), dpToPx(42), dpToPx(18), dpToPx(18));
        container.addView(closeHint, hintParams);

        dialog.setOnDismissListener(view -> videoView.stopPlayback());
        dialog.setContentView(container);
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            shownWindow.setBackgroundDrawableResource(android.R.color.black);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
