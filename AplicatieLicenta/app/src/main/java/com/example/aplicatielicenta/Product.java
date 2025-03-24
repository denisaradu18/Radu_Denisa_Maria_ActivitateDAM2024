package com.example.aplicatielicenta;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private String id;
    private String title;
    private String description;
    private String username;
    private String pickupTimes;
    private String pickupInstructions;
    private String expirationDate;
    private String category;
    private double latitude;
    private double longitude;
    private List<String> imageUrls;
    private String userId;
    private String quantity;
    private String listForDays;
    private com.google.firebase.Timestamp postedAt;


    public Product() {
        this.imageUrls = new ArrayList<>();
    }
    public Product(String id, String title, String description, String expirationDate,
                   String pickupTimes, String pickupInstructions, String category,
                   double latitude, double longitude, List<String> imageUrls, String username) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.expirationDate = expirationDate;
        this.pickupTimes = pickupTimes;
        this.pickupInstructions = pickupInstructions;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.username = username;
        this.userId = null;
    }

    public Product(String id, String title, String description, String username, String pickupTimes, String pickupInstructions, String expirationDate, String category, double latitude, double longitude, List<String> imageUrls, String userId, String quantity, String listForDays, Timestamp postedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.username = username;
        this.pickupTimes = pickupTimes;
        this.pickupInstructions = pickupInstructions;
        this.expirationDate = expirationDate;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrls = imageUrls;
        this.userId = userId;
        this.quantity = quantity;
        this.listForDays = listForDays;
        this.postedAt = postedAt;
    }

    public Product(String id, String title, String description, String username,
                   String pickupTimes, String pickupInstructions, String expirationDate,
                   String category, double latitude, double longitude,
                   List<String> imageUrls, String userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.username = username;
        this.pickupTimes = pickupTimes;
        this.pickupInstructions = pickupInstructions;
        this.expirationDate = expirationDate;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPickupTimes() {
        return pickupTimes;
    }

    public void setPickupTimes(String pickupTimes) {
        this.pickupTimes = pickupTimes;
    }

    public String getPickupInstructions() {
        return pickupInstructions;
    }

    public void setPickupInstructions(String pickupInstructions) {
        this.pickupInstructions = pickupInstructions;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    // Fix: Add a method to add a single image URL
    public void addImageUrl(String imageUrl) {
        if (this.imageUrls == null) {
            this.imageUrls = new ArrayList<>();
        }
        if (imageUrl != null && !imageUrl.isEmpty()) {
            this.imageUrls.add(imageUrl);
        }
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getListForDays() {
        return listForDays;
    }

    public void setListForDays(String listForDays) {
        this.listForDays = listForDays;
    }

    public Timestamp getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(Timestamp postedAt) {
        this.postedAt = postedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}