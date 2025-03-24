package com.example.aplicatielicenta;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";
    private ViewPager2 imageSlider;
    private ImageSliderAdapter imageAdapter;
    private ImageView backIcon, favoriteIcon, reportIcon;
    private TextView productTitle, productDescription, pickupTimes, location, username, addedDate;
    private Button requestButton;
    private TabLayout tabLayout;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String productId;
    private String productOwnerId;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // Initialize UI
        imageSlider = findViewById(R.id.imageSlider);
        tabLayout = findViewById(R.id.tabLayout);
        backIcon = findViewById(R.id.back_icon);
        favoriteIcon = findViewById(R.id.favorite_icon);
        reportIcon = findViewById(R.id.report_icon);
        productTitle = findViewById(R.id.product_title);
        productDescription = findViewById(R.id.product_description);
        pickupTimes = findViewById(R.id.pickup_times);
        location = findViewById(R.id.location);
        username = findViewById(R.id.username);
        addedDate = findViewById(R.id.added_date);
        requestButton = findViewById(R.id.request_button);
        progressBar = findViewById(R.id.progressBar);

        showLoading(true);

        // Get Product ID from Intent
        Intent intent = getIntent();
        productId = intent.getStringExtra("PRODUCT_ID");

        if (productId != null) {
            Log.d(TAG, "Loading product with ID: " + productId);
            loadProductFromFirestore();
        } else {
            Toast.makeText(this, "Error: No product ID found.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Button Actions
        backIcon.setOnClickListener(v -> finish());
        favoriteIcon.setOnClickListener(v -> Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show());
        reportIcon.setOnClickListener(v -> showReportDialog());
        requestButton.setOnClickListener(v -> sendRequest());
    }

    private void loadProductFromFirestore() {
        db.collection("products").document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Product data: " + documentSnapshot.getData());
                        displayProductData(documentSnapshot);
                    } else {
                        Log.e(TAG, "Product not found: " + productId);
                        Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading product", e);
                    Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayProductData(DocumentSnapshot document) {
        productOwnerId = document.getString("userId"); // User who posted the product
        if (productOwnerId == null || productOwnerId.isEmpty()) {
            Toast.makeText(this, "Error: Product owner unknown.", Toast.LENGTH_SHORT).show();
            return;
        }

        productTitle.setText(document.getString("title"));
        productDescription.setText(document.getString("description"));
        pickupTimes.setText(document.getString("pickupTimes"));

        productOwnerId = document.getString("userId");

        String usernameStr = document.getString("username");
        if (usernameStr != null && !usernameStr.isEmpty()) {
            username.setText("Posted by: " + usernameStr);
        } else {
            username.setText("Posted by: Unknown");
        }


        com.google.firebase.Timestamp postedAt = document.getTimestamp("postedAt");
        if (postedAt != null) {
            long millis = postedAt.toDate().getTime();
            long now = System.currentTimeMillis();
            long diffMillis = now - millis;
            long days = diffMillis / (1000 * 60 * 60 * 24);

            String text = (days == 0) ? "Added today" :
                    (days == 1) ? "Added yesterday" :
                            "Added " + days + " days ago";
            addedDate.setText(text);
        } else {
            addedDate.setText("Added date unknown");
        }

        // Load location
        Double latitude = document.getDouble("latitude");
        Double longitude = document.getDouble("longitude");
        if (latitude != null && longitude != null) {
            location.setText(getAddressFromCoordinates(latitude, longitude));
        } else {
            location.setText("Unknown location");
        }

        // Load images
        List<String> imageUrls = (List<String>) document.get("imageUrls");
        if (imageUrls == null || imageUrls.isEmpty()) {
            imageUrls = new ArrayList<>();
            imageUrls.add("https://via.placeholder.com/300");
        }

        imageAdapter = new ImageSliderAdapter(this, imageUrls);
        imageSlider.setAdapter(imageAdapter);
        new TabLayoutMediator(tabLayout, imageSlider, (tab, position) -> {}).attach();

        showLoading(false);
    }

    private void sendRequest() {
        if (productOwnerId == null || productOwnerId.isEmpty()) {
            Toast.makeText(this, "Error: Product owner unknown.", Toast.LENGTH_SHORT).show();
            return;
        }

        String transactionId = db.collection("transactions").document().getId();

        // Create transaction
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("transactionId", transactionId);
        transaction.put("buyerId", currentUserId);
        transaction.put("sellerId", productOwnerId);
        transaction.put("productId", productId);
        transaction.put("timestamp", System.currentTimeMillis());
        transaction.put("status", "pending");

        db.collection("transactions").document(transactionId)
                .set(transaction)
                .addOnSuccessListener(aVoid -> {
                    sendInitialMessage(transactionId);
                    Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show();
                    openChat(transactionId);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Request failed", Toast.LENGTH_SHORT).show());
    }

    private void sendInitialMessage(String transactionId) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", currentUserId);
        messageData.put("receiverId", productOwnerId);
        messageData.put("message", "Hello! I'm interested in this product.");
        messageData.put("timestamp", System.currentTimeMillis());

        db.collection("transactions").document(transactionId).collection("messages")
                .add(messageData)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Message sent successfully!"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send message: ", e));
    }


    private void openChat(String transactionId) {
        // Add logging to debug
        Log.d(TAG, "Opening chat with transactionId: " + transactionId + " and receiverId: " + productOwnerId);

        if (transactionId == null || transactionId.isEmpty() || productOwnerId == null || productOwnerId.isEmpty()) {
            Toast.makeText(this, "Error: Invalid transaction details.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, TransactionChatActivity.class);
        intent.putExtra("transactionId", transactionId);
        intent.putExtra("receiverId", productOwnerId);
        startActivity(intent);
    }

    private void showReportDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Report Product")
                .setItems(new String[]{"Food safety", "Wrong category", "Offensive", "Other"},
                        (dialog, which) -> Toast.makeText(this, "Report sent", Toast.LENGTH_SHORT).show())
                .show();
    }

    private String getAddressFromCoordinates(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown location";
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        int visibility = isLoading ? View.INVISIBLE : View.VISIBLE;
        productTitle.setVisibility(visibility);
        productDescription.setVisibility(visibility);
        pickupTimes.setVisibility(visibility);
        location.setVisibility(visibility);
        username.setVisibility(visibility);
        addedDate.setVisibility(visibility);
        requestButton.setVisibility(visibility);
    }
}
