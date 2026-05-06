package com.example.favoriteplaces.models;

public class PlaceModel {

    private int id;
    private String title;
    private String imageUri;
    private double latitude;
    private double longitude;
    private String address;

    // Constructor for new places (no ID yet)
    public PlaceModel(String title, String imageUri, double latitude, double longitude, String address) {
        this.title = title;
        this.imageUri = imageUri;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    // Constructor for places retrieved from DB (with ID)
    public PlaceModel(int id, String title, String imageUri, double latitude, double longitude, String address) {
        this.id = id;
        this.title = title;
        this.imageUri = imageUri;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
