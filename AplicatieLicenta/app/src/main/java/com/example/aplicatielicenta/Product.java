package com.example.aplicatielicenta;

public class Product {
    private String id;
    private String title;
    private String description;
    private String expirationDate;
    private String pickupTimes;
    private String pickupInstructions;
    private String category;
    private double latitude;
    private double longitude;
    private String imageUrl;
    private String username;

    public Product() {}

    public Product(String id,String title, String description, String expirationDate, String pickupTimes,
                   String pickupInstructions, String category, double latitude, double longitude,
                   String imageUrl, String username) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.expirationDate = expirationDate;
        this.pickupTimes = pickupTimes;
        this.pickupInstructions = pickupInstructions;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getExpirationDate() { return expirationDate; }
    public String getPickupTimes() { return pickupTimes; }
    public String getPickupInstructions() { return pickupInstructions; }
    public String getCategory() { return category; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getImageUrl() { return imageUrl; }
    public String getUsername() { return username; } // ✅ Getter pentru username
}
