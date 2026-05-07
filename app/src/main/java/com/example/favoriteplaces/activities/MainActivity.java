package com.example.favoriteplaces.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.favoriteplaces.adapters.PlacesAdapter;
import com.example.favoriteplaces.databinding.ActivityMainBinding;
import com.example.favoriteplaces.helpers.DatabaseHelper;
import com.example.favoriteplaces.models.PlaceModel;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PlacesAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<PlaceModel> placeList;

    // Launcher to refresh list when returning from AddPlaceActivity
    private final ActivityResultLauncher<Intent> addPlaceLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    refreshPlaceList();
                    Snackbar.make(binding.getRoot(), "Place saved successfully!", Snackbar.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Favorite Places");
        }

        dbHelper = DatabaseHelper.getInstance(this);
        setupRecyclerView();
        setupBottomNav();
        setupFab();
        refreshPlaceList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPlaceList();
    }

    // ─── Setup ───────────────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        placeList = dbHelper.getAllPlaces();
        adapter = new PlacesAdapter(this, placeList);

        binding.rvPlaces.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPlaces.setAdapter(adapter);
        binding.rvPlaces.setHasFixedSize(true);

        // Click → Detail screen
        adapter.setOnPlaceClickListener(new PlacesAdapter.OnPlaceClickListener() {
            @Override
            public void onPlaceClick(PlaceModel place, int position) {
                Intent intent = new Intent(MainActivity.this, PlaceDetailActivity.class);
                intent.putExtra(PlaceDetailActivity.EXTRA_PLACE_ID, place.getId());
                startActivity(intent);
            }

            @Override
            public void onPlaceLongClick(PlaceModel place, int position) {
                showDeleteDialog(place, position);
            }
        });

        // Swipe-to-delete
        ItemTouchHelper.SimpleCallback swipeCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView rv,
                                         @NonNull RecyclerView.ViewHolder vh,
                                         @NonNull RecyclerView.ViewHolder target) { return false; }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        PlaceModel deletedPlace = placeList.get(position);
                        dbHelper.deletePlace(deletedPlace.getId());
                        adapter.removeItem(position);
                        updateEmptyState();

                        Snackbar.make(binding.getRoot(), "Place deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", v -> {
                                    dbHelper.addPlace(deletedPlace);
                                    refreshPlaceList();
                                }).show();
                    }
                };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvPlaces);
    }

    private void setupFab() {
        binding.fabAddPlace.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddPlaceActivity.class);
            addPlaceLauncher.launch(intent);
        });
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(com.example.favoriteplaces.R.id.nav_home);
        binding.bottomNav.setOnItemSelectedListener((NavigationBarView.OnItemSelectedListener) item -> {
            int id = item.getItemId();
            if (id == com.example.favoriteplaces.R.id.nav_home) {
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                return true;
            }
            if (id == com.example.favoriteplaces.R.id.nav_map) {
                startActivity(new Intent(this, AddPlaceActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            if (id == com.example.favoriteplaces.R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private void refreshPlaceList() {
        placeList = dbHelper.getAllPlaces();
        adapter.updateList(placeList);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (placeList == null || placeList.isEmpty()) {
            binding.rvPlaces.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.rvPlaces.setVisibility(View.VISIBLE);
            binding.layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void showDeleteDialog(PlaceModel place, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Place")
                .setMessage("Are you sure you want to delete \"" + place.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deletePlace(place.getId());
                    adapter.removeItem(position);
                    updateEmptyState();
                    Toast.makeText(this, "Place deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
