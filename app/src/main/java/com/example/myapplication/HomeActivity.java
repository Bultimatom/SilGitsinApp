package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeActivity extends AppCompatActivity {
    private ReviewStore reviewStore;
    private TextView tvHomeStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        reviewStore = new ReviewStore(this);
        setupSystemBars();
        bindNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStats();
    }

    private void setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homeRoot), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void bindNavigation() {
        tvHomeStats = findViewById(R.id.tvHomeStats);
        findViewById(R.id.btnStartCleaning).setOnClickListener(view ->
                startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.btnOpenGuide).setOnClickListener(view ->
                startActivity(new Intent(this, GuideActivity.class)));
        findViewById(R.id.btnOpenStats).setOnClickListener(view ->
                startActivity(new Intent(this, StatsActivity.class)));
    }

    private void updateStats() {
        int total = reviewStore.getTotalDeleted() + reviewStore.getTotalKept();
        tvHomeStats.setText(total + " işlem yapıldı • "
                + reviewStore.getReviewedCount() + " fotoğraf tekrar gösterilmeyecek");
    }
}
