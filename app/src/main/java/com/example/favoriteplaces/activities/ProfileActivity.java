package com.example.favoriteplaces.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.favoriteplaces.databinding.ActivityProfileBinding;
import com.example.favoriteplaces.helpers.DatabaseHelper;
import com.google.android.material.navigation.NavigationBarView;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = DatabaseHelper.getInstance(this);
        int total = dbHelper.getAllPlaces() != null ? dbHelper.getAllPlaces().size() : 0;
        binding.tvTotalPlaces.setText(String.valueOf(total));

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString(LoginActivity.KEY_NAME, "Hawkar");
        String email = prefs.getString(LoginActivity.KEY_EMAIL, "hawkar@example.com");
        binding.tvName.setText(name);
        binding.tvEmail.setText(email);

        binding.btnSettings.setOnClickListener(v ->
                Toast.makeText(this, "Settings (coming soon)", Toast.LENGTH_SHORT).show());
        binding.btnLogout.setOnClickListener(v -> logout());

        setupBottomNav();
    }

    private void logout() {
        getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(com.example.favoriteplaces.R.id.nav_profile);
        binding.bottomNav.setOnItemSelectedListener((NavigationBarView.OnItemSelectedListener) item -> {
            int id = item.getItemId();
            if (id == com.example.favoriteplaces.R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            if (id == com.example.favoriteplaces.R.id.nav_map) {
                startActivity(new Intent(this, AddPlaceActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            if (id == com.example.favoriteplaces.R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }
}

