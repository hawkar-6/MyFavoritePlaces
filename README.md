# Favorite Places Saver 📍

A complete Android application for saving and viewing your favorite locations on a map.

---

## ✨ Features
- 📋 **List all saved places** with thumbnail + title in a RecyclerView
- ➕ **Add new places** with title, gallery image, and map-picked location
- 🗺️ **Interactive map picker** — tap anywhere on Google Maps to set coordinates
- 📍 **Detail screen** with collapsing toolbar hero image + embedded map
- 🗑️ **Swipe-to-delete** with undo Snackbar, or long-press for dialog
- 🔗 **Open in Google Maps** from the detail screen
- 💾 **SQLite persistence** via a singleton DatabaseHelper
- 🔒 **Runtime permissions** for gallery (API 33+ and legacy) and location

---

## 📁 Package Structure

```
com.example.favoriteplaces/
├── activities/
│   ├── MainActivity.java          # Home screen — RecyclerView + FAB
│   ├── AddPlaceActivity.java      # Form to create a new place
│   ├── MapPickerActivity.java     # Full-screen map to pick a location
│   └── PlaceDetailActivity.java   # Collapsing detail view with mini-map
├── adapters/
│   └── PlacesAdapter.java         # RecyclerView adapter with ViewHolder
├── helpers/
│   └── DatabaseHelper.java        # SQLite CRUD singleton
└── models/
    └── PlaceModel.java            # Plain Java data model (id, title, imageUri, lat, lng, address)
```

---

## 🗄️ Database Schema

| Column      | Type    | Description                    |
|-------------|---------|--------------------------------|
| `id`        | INTEGER | Primary key, auto-increment    |
| `title`     | TEXT    | Place name (required)          |
| `image_uri` | TEXT    | Content URI string from gallery|
| `latitude`  | REAL    | WGS-84 latitude                |
| `longitude` | REAL    | WGS-84 longitude               |
| `address`   | TEXT    | Reverse-geocoded address       |

---

## 🚀 Setup Instructions

### 1. Clone / import the project
Open the `FavoritePlacesSaver` folder in **Android Studio Hedgehog** (or later).

### 2. Get a Google Maps API key
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create or select a project
3. Enable **Maps SDK for Android**
4. Create an API key under **Credentials**
5. (Recommended) Restrict the key to your app's package name + SHA-1

### 3. Add the API key
Add your key to `local.properties` (this file is not committed):

```properties
MAPS_API_KEY=YOUR_REAL_KEY_HERE
```

The manifest reads it via a Gradle `manifestPlaceholders` variable (`${MAPS_API_KEY}`).

### 4. Build & run
Sync Gradle → Run on a device or emulator with **Google Play Services** installed.

---

## 🔑 Permissions Requested

| Permission                  | When          | Why                          |
|-----------------------------|---------------|------------------------------|
| `ACCESS_FINE_LOCATION`      | Map opens     | Show current location button |
| `ACCESS_COARSE_LOCATION`    | Map opens     | Fallback location            |
| `READ_MEDIA_IMAGES` (API 33+) | Gallery tap | Pick photo                   |
| `READ_EXTERNAL_STORAGE` (API ≤32) | Gallery tap | Pick photo              |
| `INTERNET`                  | Always        | Load map tiles               |

---

## 🏗️ Architecture Decisions

- **View Binding** — enabled in `build.gradle`; all layouts use generated `Binding` classes
- **Singleton DatabaseHelper** — thread-safe single instance via `getInstance(Context)`
- **ActivityResultLauncher** — modern replacement for `onActivityResult`; used for gallery, permissions, and map picker
- **Geocoder** — runs on a background thread to avoid blocking the main thread
- **ItemTouchHelper** — swipe-to-delete on the RecyclerView with a Snackbar undo
- **CollapsingToolbarLayout** — hero image on the detail screen that parallax-collapses on scroll

---

## 📦 Dependencies

```gradle
// Maps & Location
com.google.android.gms:play-services-maps:19.0.0
com.google.android.gms:play-services-location:21.3.0

// Material Design
com.google.android.material:material:1.12.0

// AndroidX
androidx.appcompat:appcompat:1.7.0
androidx.recyclerview:recyclerview:1.3.2
androidx.cardview:cardview:1.0.0
```

---

## 💡 Extending the App

| Feature                | How to add                                                  |
|------------------------|-------------------------------------------------------------|
| Edit a place           | Add `EditPlaceActivity`, re-use `AddPlaceActivity` layout   |
| Search / filter        | Add `SearchView` to `MainActivity` toolbar                   |
| Share place            | `Intent.ACTION_SEND` with coordinates + title               |
| Dark mode              | Add `values-night/themes.xml` with `DayNight` parent theme  |
| Room migration         | Replace `DatabaseHelper` with `@Database` Room classes      |
