package com.example.favoriteplaces.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.favoriteplaces.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "session_prefs";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_NAME = "name";

    private ActivityLoginBinding binding;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            goToMain();
            return;
        }

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";

        if (email.isEmpty()) {
            binding.tilEmail.setError("Email is required");
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.tilEmail.setError(null);

        if (password.isEmpty()) {
            binding.tilPassword.setError("Password is required");
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.tilPassword.setError(null);

        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_EMAIL, email)
                .putString(KEY_NAME, "Hawkar")
                .apply();

        goToMain();
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

