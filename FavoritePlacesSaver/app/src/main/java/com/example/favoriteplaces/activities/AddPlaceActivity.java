package com.example.favoriteplaces.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.favoriteplaces.databinding.ActivityAddPlaceBinding;
import com.example.favoriteplaces.helpers.DatabaseHelper;
import com.example.favoriteplaces.models.PlaceModel;

public class AddPlaceActivity extends AppCompatActivity {

    public static final String EXTRA_LATITUDE  = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_ADDRESS   = "extra_address";

    private ActivityAddPlaceBinding binding;
    private DatabaseHelper dbHelper;

    private Uri selectedImageUri  = null;
    private double selectedLat    = 0.0;
    private double selectedLng    = 0.0;
    private String selectedAddress = "";
    private boolean locationPicked = false;

    // ─── Launchers ───────────────────────────────────────────────────────────────

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.ivSelectedImage.setImageURI(uri);
                    binding.tvImageHint.setText("Image selected ✓");

                    // Persist permission across restarts
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException ignored) {}
                }
            });

    private final ActivityResultLauncher<String[]> galleryPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean granted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        ? result.getOrDefault(Manifest.permission.READ_MEDIA_IMAGES, false)
                        : result.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false);
                if (Boolean.TRUE.equals(granted)) {
                    openGallery();
                } else {
                    Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> mapPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    selectedLat     = data.getDoubleExtra(EXTRA_LATITUDE, 0.0);
                    selectedLng     = data.getDoubleExtra(EXTRA_LONGITUDE, 0.0);
                    selectedAddress = data.getStringExtra(EXTRA_ADDRESS);
                    if (selectedAddress == null) selectedAddress = "";
                    locationPicked = true;

                    String locationText = selectedAddress.isEmpty()
                            ? String.format("%.5f, %.5f", selectedLat, selectedLng)
                            : selectedAddress;
                    binding.tvSelectedLocation.setText(locationText);
                    binding.tvLocationHint.setText("Location picked ✓");
                }
            });

    // ─── Lifecycle ───────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPlaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add New Place");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = DatabaseHelper.getInstance(this);

        binding.btnPickImage.setOnClickListener(v -> checkGalleryPermissionAndOpen());
        binding.btnPickLocation.setOnClickListener(v -> openMapPicker());
        binding.btnSavePlace.setOnClickListener(v -> savePlace());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ─── Gallery ─────────────────────────────────────────────────────────────────

    private void checkGalleryPermissionAndOpen() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            galleryPermissionLauncher.launch(new String[]{permission});
        }
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    // ─── Map ─────────────────────────────────────────────────────────────────────

    private void openMapPicker() {
        Intent intent = new Intent(this, MapPickerActivity.class);
        mapPickerLauncher.launch(intent);
    }

    // ─── Save ────────────────────────────────────────────────────────────────────

    private void savePlace() {
        String title = binding.etPlaceTitle.getText().toString().trim();

        if (title.isEmpty()) {
            binding.tilTitle.setError("Please enter a title");
            return;
        }
        binding.tilTitle.setError(null);

        if (!locationPicked) {
            Toast.makeText(this, "Please pick a location on the map", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageUriStr = selectedImageUri != null ? selectedImageUri.toString() : "";

        PlaceModel place = new PlaceModel(title, imageUriStr, selectedLat, selectedLng, selectedAddress);
        long id = dbHelper.addPlace(place);

        if (id != -1) {
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error saving place. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
