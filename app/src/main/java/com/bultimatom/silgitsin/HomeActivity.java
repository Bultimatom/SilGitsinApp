package com.bultimatom.silgitsin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeActivity extends AppCompatActivity {
    private ReviewStore reviewStore;
    private TextView tvHomeStats;
    private ImageButton btnHomeTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppPreferences.applyTheme(this);
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
        updateThemeIcon();
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
        btnHomeTheme = findViewById(R.id.btnHomeTheme);
        updateThemeIcon();

        btnHomeTheme.setOnClickListener(view -> {
            AppPreferences.setDarkMode(this, !AppPreferences.isDarkMode(this));
            updateThemeIcon();
        });
        findViewById(R.id.btnStartCleaning).setOnClickListener(view ->
                startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.btnOpenGuide).setOnClickListener(view ->
                startActivity(new Intent(this, GuideActivity.class)));
        findViewById(R.id.btnOpenStats).setOnClickListener(view ->
                startActivity(new Intent(this, StatsActivity.class)));
    }

    private void updateStats() {
        int total = reviewStore.getTotalDeleted() + reviewStore.getTotalKept();
        tvHomeStats.setText(total + " islem yapildi  |  "
                + reviewStore.getReviewedCount() + " medya tekrar gosterilmeyecek");
    }

    private void updateThemeIcon() {
        btnHomeTheme.setImageResource(AppPreferences.isDarkMode(this)
                ? R.drawable.ic_action_sun
                : R.drawable.ic_action_moon);
    }
}
