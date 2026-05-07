package com.example.favoriteplaces.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.favoriteplaces.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    public static final String KEY_PHONE = "phone";

    private ActivitySettingsBinding binding;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Pre-fill from SharedPreferences
        binding.etFullName.setText(prefs.getString(LoginActivity.KEY_NAME, "Hawkar"));
        binding.etEmail.setText(prefs.getString(LoginActivity.KEY_EMAIL, "hawkar@example.com"));
        binding.etPhone.setText(prefs.getString(KEY_PHONE, ""));

        binding.btnUpdateProfile.setOnClickListener(v -> updateProfile());
    }

    private void updateProfile() {
        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";

        if (fullName.isEmpty()) {
            binding.tilFullName.setError("Full name is required");
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.tilFullName.setError(null);

        if (email.isEmpty()) {
            binding.tilEmail.setError("Email is required");
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.tilEmail.setError(null);

        prefs.edit()
                .putString(LoginActivity.KEY_NAME, fullName)
                .putString(LoginActivity.KEY_EMAIL, email)
                .putString(KEY_PHONE, phone)
                .apply();

        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
        finish();
    }
}

