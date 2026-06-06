package com.bultimatom.silgitsin;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StatsActivity extends AppCompatActivity {
    private ReviewStore reviewStore;
    private TextView tvStatReviewed;
    private TextView tvStatDeleted;
    private TextView tvStatKept;
    private TextView tvStatTotal;
    private TextView tvStatFavorite;
    private TextView tvStatSaved;
    private TextView tvStatImpact;
    private TextView tvStatsNotice;
    private ProgressBar statSavedProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppPreferences.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        reviewStore = new ReviewStore(this);
        setupSystemBars();
        bindViews();
        bindNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStats();
    }

    private void setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.statsRoot), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void bindViews() {
        tvStatReviewed = findViewById(R.id.tvStatReviewed);
        tvStatDeleted = findViewById(R.id.tvStatDeleted);
        tvStatKept = findViewById(R.id.tvStatKept);
        tvStatTotal = findViewById(R.id.tvStatTotal);
        tvStatFavorite = findViewById(R.id.tvStatFavorite);
        tvStatSaved = findViewById(R.id.tvStatSaved);
        tvStatImpact = findViewById(R.id.tvStatImpact);
        tvStatsNotice = findViewById(R.id.tvStatsNotice);
        statSavedProgress = findViewById(R.id.statSavedProgress);
    }

    private void bindNavigation() {
        findViewById(R.id.btnStatsBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnStatsClean).setOnClickListener(view ->
                startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.btnResetReviewed).setOnClickListener(view -> showResetReviewedConfirm());
    }

    private void showResetReviewedConfirm() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dpToPx(20), dpToPx(18), dpToPx(20), dpToPx(16));
        content.setBackgroundResource(R.drawable.bg_stat_panel);

        TextView title = new TextView(this);
        title.setText("Emin misin?");
        title.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        title.setTextSize(22f);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        content.addView(title);

        TextView body = new TextView(this);
        body.setText("\u0130nceledi\u011fin medya listesi unutulacak. Bu dosyalar temizli\u011fe geri gelebilir. Silinen, tutulan ve toplam kazan\u00e7 korunur.");
        body.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        body.setTextSize(14f);
        body.setLineSpacing(dpToPx(3), 1f);
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        bodyParams.setMargins(0, dpToPx(10), 0, dpToPx(16));
        content.addView(body, bodyParams);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        TextView cancel = createDialogButton("Vazge\u00e7", R.drawable.bg_action_ghost, R.color.text_primary);
        cancel.setOnClickListener(view -> dialog.dismiss());
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0, dpToPx(50), 1f);
        cancelParams.setMargins(0, 0, dpToPx(8), 0);
        actions.addView(cancel, cancelParams);

        TextView confirm = createDialogButton("Evet, unut", R.drawable.bg_action_delete, R.color.white);
        confirm.setOnClickListener(view -> {
            reviewStore.resetReviewedPhotos();
            updateStats();
            dialog.dismiss();
            showNotice("\u0130ncelenenler unutuldu");
        });
        LinearLayout.LayoutParams confirmParams = new LinearLayout.LayoutParams(0, dpToPx(50), 1f);
        confirmParams.setMargins(dpToPx(8), 0, 0, 0);
        actions.addView(confirm, confirmParams);

        content.addView(actions);
        dialog.setContentView(content);
        dialog.show();

        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            shownWindow.setLayout(
                    getResources().getDisplayMetrics().widthPixels - dpToPx(36),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private TextView createDialogButton(String text, int background, int textColor) {
        TextView button = new TextView(this);
        button.setText(text);
        button.setGravity(android.view.Gravity.CENTER);
        button.setTextColor(ContextCompat.getColor(this, textColor));
        button.setTextSize(14f);
        button.setTypeface(button.getTypeface(), android.graphics.Typeface.BOLD);
        button.setBackgroundResource(background);
        return button;
    }

    private void showNotice(String message) {
        tvStatsNotice.animate().cancel();
        tvStatsNotice.setText(message);
        tvStatsNotice.setVisibility(android.view.View.VISIBLE);
        tvStatsNotice.setAlpha(0f);
        tvStatsNotice.setTranslationY(-dpToPx(8));
        tvStatsNotice.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(160L)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> tvStatsNotice.postDelayed(() -> tvStatsNotice.animate()
                        .alpha(0f)
                        .translationY(-dpToPx(6))
                        .setDuration(180L)
                        .withEndAction(() -> tvStatsNotice.setVisibility(android.view.View.GONE))
                        .start(), 1600L))
                .start();
    }

    private void updateStats() {
        int deleted = reviewStore.getTotalDeleted();
        int kept = reviewStore.getTotalKept();
        int favorite = reviewStore.getFavoriteCount();
        long savedBytes = reviewStore.getTotalSavedBytes();
        tvStatReviewed.setText(String.valueOf(reviewStore.getReviewedCount()));
        tvStatDeleted.setText(String.valueOf(deleted));
        tvStatKept.setText(String.valueOf(kept));
        tvStatFavorite.setText(String.valueOf(favorite));
        tvStatTotal.setText(String.valueOf(deleted + kept));
        tvStatSaved.setText(formatBytes(savedBytes));
        statSavedProgress.setProgress(progressForSavedBytes(savedBytes));
        tvStatImpact.setText(createImpactText(savedBytes, deleted, kept, favorite));
    }

    private String createImpactText(long savedBytes, int deleted, int kept, int favorite) {
        if (savedBytes <= 0L && deleted == 0 && kept == 0 && favorite == 0) {
            return "Hen\u00fcz alan kazan\u0131m\u0131 yok. Sola kayd\u0131r\u0131p silmeyi onaylad\u0131\u011f\u0131nda burada birikir.";
        }
        return "Sil Gitsin \u015fu ana kadar " + formatBytes(savedBytes)
                + " alan kazand\u0131rd\u0131. "
                + deleted + " silindi, "
                + kept + " tutuldu, "
                + favorite + " favori.";
    }

    private int progressForSavedBytes(long bytes) {
        if (bytes <= 0L) {
            return 0;
        }
        long oneGb = 1024L * 1024L * 1024L;
        int progress = (int) Math.min(100, Math.round((bytes * 100.0) / oneGb));
        return Math.max(progress, 3);
    }

    private String formatBytes(long bytes) {
        double mb = bytes / 1024.0 / 1024.0;
        if (mb < 1024.0) {
            return String.format(java.util.Locale.getDefault(), "%.1f MB", mb);
        }
        return String.format(java.util.Locale.getDefault(), "%.2f GB", mb / 1024.0);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
