package com.example.favoriteplaces.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.favoriteplaces.databinding.ActivityMapPickerBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityMapPickerBinding binding;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private LatLng selectedLatLng = null;
    private String selectedAddress = "";

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineGranted  = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                    enableMyLocation();
                } else {
                    Toast.makeText(this, "Location permission denied. Tap map to pick location.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!isMapsApiKeyConfigured()) {
            Toast.makeText(this, "Google Maps API key is missing. Add MAPS_API_KEY to local.properties.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Pick a Location");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Init map fragment
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(com.example.favoriteplaces.R.id.map_picker_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        binding.btnConfirmLocation.setOnClickListener(v -> confirmLocation());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ─── Map Callbacks ───────────────────────────────────────────────────────────

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Tap to pick location
        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            selectedLatLng = latLng;
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Selected Location"));

            // Reverse geocode on background thread
            resolveAddress(latLng);
            binding.tvSelectedCoords.setText(
                    String.format(Locale.getDefault(), "%.5f, %.5f", latLng.latitude, latLng.longitude));
        });

        checkLocationPermission();
    }

    // ─── Location Permission & My-Location ───────────────────────────────────────

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
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void moveToCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15f));
                } else {
                    // Default fallback: world view
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(2f));
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // ─── Geocoding ───────────────────────────────────────────────────────────────

    private void resolveAddress(LatLng latLng) {
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        sb.append(address.getAddressLine(i));
                        if (i < address.getMaxAddressLineIndex()) sb.append(", ");
                    }
                    selectedAddress = sb.toString();
                    runOnUiThread(() ->
                            binding.tvSelectedAddress.setText(selectedAddress));
                } else {
                    selectedAddress = "";
                    runOnUiThread(() -> binding.tvSelectedAddress.setText("Address not found"));
                }
            } catch (IOException e) {
                selectedAddress = "";
                runOnUiThread(() -> binding.tvSelectedAddress.setText("Unable to get address"));
            }
        }).start();
    }

    // ─── Confirm ─────────────────────────────────────────────────────────────────

    private void confirmLocation() {
        if (selectedLatLng == null) {
            Toast.makeText(this, "Tap on the map to select a location", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra(AddPlaceActivity.EXTRA_LATITUDE,  selectedLatLng.latitude);
        resultIntent.putExtra(AddPlaceActivity.EXTRA_LONGITUDE, selectedLatLng.longitude);
        resultIntent.putExtra(AddPlaceActivity.EXTRA_ADDRESS,   selectedAddress);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private boolean isMapsApiKeyConfigured() {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData == null) return false;
            String key = ai.metaData.getString("com.google.android.geo.API_KEY", "");
            return key != null && !key.trim().isEmpty();
        } catch (NameNotFoundException e) {
            return false;
        }
    }
}
