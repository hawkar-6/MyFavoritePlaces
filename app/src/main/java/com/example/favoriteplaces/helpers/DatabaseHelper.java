package com.example.favoriteplaces.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.favoriteplaces.models.PlaceModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "favoritePlaces.db";
    private static final int DATABASE_VERSION = 1;

    // Table Name
    public static final String TABLE_PLACES = "places";

    // Column Names
    public static final String COL_ID        = "id";
    public static final String COL_TITLE     = "title";
    public static final String COL_IMAGE_URI = "image_uri";
    public static final String COL_LATITUDE  = "latitude";
    public static final String COL_LONGITUDE = "longitude";
    public static final String COL_ADDRESS   = "address";

    // Create Table SQL
    private static final String CREATE_TABLE_PLACES =
            "CREATE TABLE " + TABLE_PLACES + " (" +
            COL_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_TITLE     + " TEXT NOT NULL, " +
            COL_IMAGE_URI + " TEXT, " +
            COL_LATITUDE  + " REAL NOT NULL, " +
            COL_LONGITUDE + " REAL NOT NULL, " +
            COL_ADDRESS   + " TEXT" +
            ");";

    private static DatabaseHelper instance;

    // Singleton pattern
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PLACES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES);
        onCreate(db);
    }

    // ─── CRUD Operations ────────────────────────────────────────────────────────

    /** Insert a new place. Returns the row ID, or -1 on error. */
    public long addPlace(PlaceModel place) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE,     place.getTitle());
        values.put(COL_IMAGE_URI, place.getImageUri());
        values.put(COL_LATITUDE,  place.getLatitude());
        values.put(COL_LONGITUDE, place.getLongitude());
        values.put(COL_ADDRESS,   place.getAddress());

        long id = db.insertOrThrow(TABLE_PLACES, null, values);
        db.close();
        return id;
    }

    /** Retrieve all places, newest first. */
    public List<PlaceModel> getAllPlaces() {
        List<PlaceModel> places = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_PLACES + " ORDER BY " + COL_ID + " DESC";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                PlaceModel place = cursorToPlace(cursor);
                places.add(place);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return places;
    }

    /** Retrieve a single place by its ID. Returns null if not found. */
    public PlaceModel getPlaceById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PLACES,
                null,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );
        PlaceModel place = null;
        if (cursor.moveToFirst()) {
            place = cursorToPlace(cursor);
        }
        cursor.close();
        db.close();
        return place;
    }

    /** Update an existing place. Returns number of rows affected. */
    public int updatePlace(PlaceModel place) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE,     place.getTitle());
        values.put(COL_IMAGE_URI, place.getImageUri());
        values.put(COL_LATITUDE,  place.getLatitude());
        values.put(COL_LONGITUDE, place.getLongitude());
        values.put(COL_ADDRESS,   place.getAddress());

        int rows = db.update(TABLE_PLACES, values, COL_ID + " = ?",
                new String[]{String.valueOf(place.getId())});
        db.close();
        return rows;
    }

    /** Delete a place by its ID. Returns number of rows deleted. */
    public int deletePlace(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_PLACES, COL_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    // ─── Helper ─────────────────────────────────────────────────────────────────

    private PlaceModel cursorToPlace(Cursor cursor) {
        int id         = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
        String title   = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
        String imgUri  = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_URI));
        double lat     = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LATITUDE));
        double lng     = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LONGITUDE));
        String address = cursor.getString(cursor.getColumnIndexOrThrow(COL_ADDRESS));
        return new PlaceModel(id, title, imgUri, lat, lng, address);
    }
}
