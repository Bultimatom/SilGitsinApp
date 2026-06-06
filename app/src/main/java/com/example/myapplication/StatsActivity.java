package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StatsActivity extends AppCompatActivity {
    private ReviewStore reviewStore;
    private TextView tvStatReviewed;
    private TextView tvStatDeleted;
    private TextView tvStatKept;
    private TextView tvStatTotal;

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
    }

    private void bindNavigation() {
        findViewById(R.id.btnStatsBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnStatsClean).setOnClickListener(view ->
                startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.btnResetReviewed).setOnClickListener(view -> {
            reviewStore.resetReviewedPhotos();
            updateStats();
            Toast.makeText(this, "İncelenen fotoğraf listesi sıfırlandı", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateStats() {
        int deleted = reviewStore.getTotalDeleted();
        int kept = reviewStore.getTotalKept();
        tvStatReviewed.setText(String.valueOf(reviewStore.getReviewedCount()));
        tvStatDeleted.setText(String.valueOf(deleted));
        tvStatKept.setText(String.valueOf(kept));
        tvStatTotal.setText(String.valueOf(deleted + kept));
    }
}
