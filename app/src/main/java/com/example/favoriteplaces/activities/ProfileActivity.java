package com.example.favoriteplaces.activities;

import android.content.Intent;
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

        binding.btnSettings.setOnClickListener(v ->
                Toast.makeText(this, "Settings (coming soon)", Toast.LENGTH_SHORT).show());
        binding.btnLogout.setOnClickListener(v ->
                Toast.makeText(this, "Logout (coming soon)", Toast.LENGTH_SHORT).show());

        setupBottomNav();
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

