package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class AppPreferences {
    private static final String PREFS = "swipe_galeri_prefs";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";
    private static final String KEY_MAIN_HINT_DONE = "main_hint_done";
    private static final String KEY_DARK_MODE = "dark_mode";

    private AppPreferences() {
    }

    public static void applyTheme(Context context) {
        AppCompatDelegate.setDefaultNightMode(isDarkMode(context)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static boolean isDarkMode(Context context) {
        return prefs(context).getBoolean(KEY_DARK_MODE, false);
    }

    public static void setDarkMode(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_DARK_MODE, enabled).apply();
        applyTheme(context);
    }

    public static boolean isOnboardingDone(Context context) {
        return prefs(context).getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public static void setOnboardingDone(Context context) {
        prefs(context).edit().putBoolean(KEY_ONBOARDING_DONE, true).apply();
    }

    public static boolean isMainHintDone(Context context) {
        return prefs(context).getBoolean(KEY_MAIN_HINT_DONE, false);
    }

    public static void setMainHintDone(Context context) {
        prefs(context).edit().putBoolean(KEY_MAIN_HINT_DONE, true).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
