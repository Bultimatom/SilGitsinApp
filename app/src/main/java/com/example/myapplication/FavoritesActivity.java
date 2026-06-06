package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
            if (photo.isVideo()) {
                imageView.setImageBitmap(createVideoFrame(photo));
            } else {
                imageView.setImageURI(photo.getUri());
            }
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_surface));
            imageView.setContentDescription(photo.getDisplayName());
            imageView.setOnClickListener(view -> openInGallery(photo));
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
                reviewStore.removeFavorite(photo.getId());
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

    private void openInGallery(PhotoItem photo) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(photo.getUri(), photo.isVideo() ? "video/*" : "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Galeride goster"));
    }

    private Bitmap createVideoFrame(PhotoItem photo) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, photo.getUri());
            return retriever.getFrameAtTime(0);
        } catch (RuntimeException exception) {
            return null;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
