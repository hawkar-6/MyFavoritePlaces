package com.example.favoriteplaces.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.favoriteplaces.R;
import com.example.favoriteplaces.databinding.ActivityPlaceDetailBinding;
import com.example.favoriteplaces.helpers.DatabaseHelper;
import com.example.favoriteplaces.models.PlaceModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class PlaceDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_PLACE_ID = "extra_place_id";

    private ActivityPlaceDetailBinding binding;
    private DatabaseHelper dbHelper;
    private PlaceModel place;
    private GoogleMap googleMap;

    private final ActivityResultLauncher<Intent> editPlaceLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Reload place from DB and refresh UI
                    PlaceModel updated = dbHelper.getPlaceById(place.getId());
                    if (updated != null) {
                        place = updated;
                        populateUi();
                        refreshMapMarker();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = DatabaseHelper.getInstance(this);

        int placeId = getIntent().getIntExtra(EXTRA_PLACE_ID, -1);
        if (placeId == -1) {
            Toast.makeText(this, "Invalid place", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        place = dbHelper.getPlaceById(placeId);
        if (place == null) {
            Toast.makeText(this, "Place not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateUi();
        initMap();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            confirmDelete();
            return true;
        } else if (item.getItemId() == R.id.action_edit) {
            openEditMode();
            return true;
        } else if (item.getItemId() == R.id.action_open_maps) {
            openInGoogleMaps();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ─── UI Population ───────────────────────────────────────────────────────────

    private void populateUi() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(place.getTitle());
        }

        binding.tvDetailTitle.setText(place.getTitle());

        String address = place.getAddress() != null && !place.getAddress().isEmpty()
                ? place.getAddress()
                : String.format(Locale.getDefault(), "%.5f, %.5f", place.getLatitude(), place.getLongitude());
        binding.tvDetailAddress.setText(address);

        binding.tvDetailCoords.setText(String.format(Locale.getDefault(),
                "Lat: %.6f  |  Lng: %.6f", place.getLatitude(), place.getLongitude()));

        // Image
        if (place.getImageUri() != null && !place.getImageUri().isEmpty()) {
            try {
                binding.ivDetailImage.setImageURI(Uri.parse(place.getImageUri()));
            } catch (Exception e) {
                binding.ivDetailImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            binding.ivDetailImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    // ─── Map ─────────────────────────────────────────────────────────────────────

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.detail_map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        refreshMapMarker();
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
    }

    private void refreshMapMarker() {
        if (googleMap == null || place == null) return;
        googleMap.clear();
        LatLng placeLatLng = new LatLng(place.getLatitude(), place.getLongitude());
        googleMap.addMarker(new MarkerOptions()
                .position(placeLatLng)
                .title(place.getTitle())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 15f));
    }

    // ─── Actions ─────────────────────────────────────────────────────────────────

    private void openEditMode() {
        Intent intent = new Intent(this, AddPlaceActivity.class);
        intent.putExtra(AddPlaceActivity.EXTRA_EDIT_PLACE_ID, place.getId());
        intent.putExtra(AddPlaceActivity.EXTRA_EDIT_TITLE, place.getTitle());
        intent.putExtra(AddPlaceActivity.EXTRA_EDIT_IMAGE_URI, place.getImageUri());
        intent.putExtra(AddPlaceActivity.EXTRA_EDIT_LATITUDE, place.getLatitude());
        intent.putExtra(AddPlaceActivity.EXTRA_EDIT_LONGITUDE, place.getLongitude());
        intent.putExtra(AddPlaceActivity.EXTRA_EDIT_ADDRESS, place.getAddress());
        editPlaceLauncher.launch(intent);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Place")
                .setMessage("Delete \"" + place.getTitle() + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    dbHelper.deletePlace(place.getId());
                    Toast.makeText(this, "Place deleted", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openInGoogleMaps() {
        Uri gmmUri = Uri.parse(String.format(Locale.getDefault(),
                "geo:%f,%f?q=%f,%f(%s)",
                place.getLatitude(), place.getLongitude(),
                place.getLatitude(), place.getLongitude(),
                Uri.encode(place.getTitle())));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback: browser
            Uri browserUri = Uri.parse(String.format(Locale.getDefault(),
                    "https://www.google.com/maps/search/?api=1&query=%f,%f",
                    place.getLatitude(), place.getLongitude()));
            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
        }
    }
}
