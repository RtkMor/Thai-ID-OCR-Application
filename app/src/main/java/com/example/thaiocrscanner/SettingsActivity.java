package com.example.thaiocrscanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Objects.requireNonNull(getSupportActionBar()).hide();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);

        // night mode switch
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch btnToggleDark = findViewById(R.id.NightModeSwitch);
        boolean isDarkModeOn = sharedPreferences.getBoolean(Constants.NIGHT_MODE_ENABLED, false);
        btnToggleDark.setChecked(isDarkModeOn);

        // Listen for Switch changes
        btnToggleDark.setOnCheckedChangeListener((buttonView, isChecked) -> {

            // Toggle between light and dark mode
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

            // Update the night mode setting in SharedPreferences
            editor = sharedPreferences.edit();
            editor.putBoolean(Constants.NIGHT_MODE_ENABLED, isChecked);
            editor.apply();
        });

        // Handle back button click
        findViewById(R.id.backBtnSettings).setOnClickListener(v -> {
            finish();
        });
    }
}
