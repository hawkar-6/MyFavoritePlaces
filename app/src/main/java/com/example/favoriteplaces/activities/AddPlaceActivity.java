package com.example.favoriteplaces.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.favoriteplaces.databinding.ActivityAddPlaceBinding;
import com.example.favoriteplaces.helpers.DatabaseHelper;
import com.example.favoriteplaces.models.PlaceModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationBarView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddPlaceActivity extends AppCompatActivity implements OnMapReadyCallback {

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

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    // ─── Launchers ───────────────────────────────────────────────────────────────

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.ivSelectedImage.setImageURI(uri);
                    binding.ivSelectedImage.setVisibility(android.view.View.VISIBLE);
                    binding.tvImageHint.setText("PHOTO ADDED");

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

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineGranted  = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                    enableMyLocation();
                }
            });

    // ─── Lifecycle ───────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPlaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnAddPhoto.setOnClickListener(v -> checkGalleryPermissionAndOpen());
        binding.fabAddPlace.setOnClickListener(v -> savePlace());
        binding.btnSavePlace.setOnClickListener(v -> savePlace());

        setupBottomNav();
        setupMap();
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

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(com.example.favoriteplaces.R.id.add_place_map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));

            selectedLat = latLng.latitude;
            selectedLng = latLng.longitude;
            locationPicked = true;
            binding.tvLatLng.setText(String.format(Locale.getDefault(), "%.5f, %.5f", selectedLat, selectedLng));

            resolveAddress(latLng);
        });

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (hasLocationPermission()) {
            enableMyLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void enableMyLocation() {
        if (googleMap == null) return;
        try {
            if (hasLocationPermission()) {
                googleMap.setMyLocationEnabled(true);
                moveToCurrentLocation();
            }
        } catch (SecurityException ignored) {}
    }

    private void moveToCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (googleMap == null) return;
                if (location != null) {
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15f));
                } else {
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(2f));
                }
            });
        } catch (SecurityException ignored) {}
    }

    private void resolveAddress(LatLng latLng) {
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        sb.append(address.getAddressLine(i));
                        if (i < address.getMaxAddressLineIndex()) sb.append(", ");
                    }
                    selectedAddress = sb.toString();
                } else {
                    selectedAddress = "";
                }
            } catch (IOException e) {
                selectedAddress = "";
            }
        }).start();
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(com.example.favoriteplaces.R.id.nav_map);
        binding.bottomNav.setOnItemSelectedListener((NavigationBarView.OnItemSelectedListener) item -> {
            int id = item.getItemId();
            if (id == com.example.favoriteplaces.R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
                return true;
            }
            if (id == com.example.favoriteplaces.R.id.nav_map) {
                return true;
            }
            if (id == com.example.favoriteplaces.R.id.nav_profile) {
                Toast.makeText(this, "Profile (coming soon)", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    // ─── Save ────────────────────────────────────────────────────────────────────

    private void savePlace() {
        String title = binding.etPlaceTitle.getText().toString().trim();

        if (title.isEmpty()) {
            binding.tilTitle.setError("Please enter a title");
            Toast.makeText(this, "Please enter a place title", Toast.LENGTH_SHORT).show();
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
