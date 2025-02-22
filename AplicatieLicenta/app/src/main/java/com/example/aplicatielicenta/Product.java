package com.example.aplicatielicenta;

public class Product {
    private String name;
    private String category;  // "Food", "Non-Food", etc.
    private double distance;
    private int imageResId;   // pentru imagine locală din drawable

    public Product(String name, String category, double distance, int imageResId) {
        this.name = name;
        this.category = category;
        this.distance = distance;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getDistance() {
        return distance;
    }

    public int getImageResId() {
        return imageResId;
    }
}
