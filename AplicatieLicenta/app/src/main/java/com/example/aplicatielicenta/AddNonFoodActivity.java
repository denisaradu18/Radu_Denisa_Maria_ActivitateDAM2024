package com.example.aplicatielicenta;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.aplicatielicenta.databinding.ActivityAddNonFoodBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AddNonFoodActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityAddNonFoodBinding binding;

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;

    private List<MaterialButton> quantityButtons = new ArrayList<>();
    private int selectedQuantity = 1;
    private boolean imageLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNonFoodBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map_container);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize quantity buttons
        setupQuantityButtons();

        // Setup "Other" button
        binding.btnQuantityOther.setOnClickListener(v -> showCustomQuantityDialog());

        // Setup camera/gallery functionality
        setupImageCapture();

        // Setup Post button
        binding.btnPost.setOnClickListener(v -> validateAndPost());
    }

    private void setupQuantityButtons() {
        quantityButtons.add(binding.btnQuantity1);
        quantityButtons.add(binding.btnQuantity2);
        quantityButtons.add(binding.btnQuantity3);
        quantityButtons.add(binding.btnQuantity4);
        quantityButtons.add(binding.btnQuantity5);

        updateQuantitySelection(0);

        for (int i = 0; i < quantityButtons.size(); i++) {
            final int position = i;
            quantityButtons.get(i).setOnClickListener(v -> updateQuantitySelection(position));
        }
    }

    private void updateQuantitySelection(int position) {
        selectedQuantity = position + 1;
        if (position == 4) {
            selectedQuantity = 5;
        }
        for (int i = 0; i < quantityButtons.size(); i++) {
            MaterialButton button = quantityButtons.get(i);
            button.setBackgroundColor(i == position ? getResources().getColor(R.color.green) : getResources().getColor(android.R.color.white));
            button.setTextColor(i == position ? getResources().getColor(android.R.color.white) : getResources().getColor(R.color.button_text_color));
        }
    }

    private void showCustomQuantityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_custom_quantity, null);
        EditText etCustomQuantity = view.findViewById(R.id.et_custom_quantity);

        builder.setTitle("Enter custom quantity")
                .setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    String quantityStr = etCustomQuantity.getText().toString().trim();
                    if (!quantityStr.isEmpty()) {
                        try {
                            int customQty = Integer.parseInt(quantityStr);
                            if (customQty > 0) {
                                selectedQuantity = customQty;
                                binding.btnQuantityOther.setBackgroundColor(getResources().getColor(R.color.green));
                                binding.btnQuantityOther.setText("Other: " + customQty);
                                binding.btnQuantityOther.setTextColor(getResources().getColor(android.R.color.white));
                            } else {
                                Toast.makeText(AddNonFoodActivity.this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(AddNonFoodActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void setupImageCapture() {
        binding.ivCamera.setOnClickListener(v -> showImageSelectionOptions());
        binding.fabAddPhoto.setOnClickListener(v -> showImageSelectionOptions());
    }

    private void showImageSelectionOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source")
                .setItems(new CharSequence[]{"Camera", "Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    } else {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(Intent.createChooser(intent, "Select an image"), REQUEST_PICK_IMAGE);
                    }
                })
                .create()
                .show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        LatLng defaultLocation = new LatLng(44.4268, 26.1025);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    private void validateAndPost() {
        if (binding.etTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Item posted successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
