package com.example.aplicatielicenta;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.aplicatielicenta.databinding.ActivityAddFoodBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddFoodActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityAddFoodBinding binding;

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;

    private List<MaterialButton> quantityButtons = new ArrayList<>();
    private int selectedQuantity = 1;
    private boolean imageLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFoodBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map_container);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize date picker for expiration date
        binding.etExpirationDate.setOnClickListener(v -> showDatePickerDialog());

        // Initialize quantity buttons
        setupQuantityButtons();

        // Setup "Other" button
        binding.btnQuantityOther.setOnClickListener(v -> showCustomQuantityDialog());

        // Initialize Spinner for listing days
        setupDaysSpinner();

        // Setup camera/gallery functionality
        setupImageCapture();

        // Setup Post button
        binding.btnPost.setOnClickListener(v -> validateAndPost());
    }

    private void setupQuantityButtons() {
        // Add all quantity buttons to list for easier management
        quantityButtons.add(binding.btnQuantity1);
        quantityButtons.add(binding.btnQuantity2);
        quantityButtons.add(binding.btnQuantity3);
        quantityButtons.add(binding.btnQuantity4);
        quantityButtons.add(binding.btnQuantity5);

        // Set default selection
        updateQuantitySelection(0); // Select first button (quantity 1)

        // Set click listeners for all quantity buttons
        for (int i = 0; i < quantityButtons.size(); i++) {
            final int position = i;
            quantityButtons.get(i).setOnClickListener(v -> updateQuantitySelection(position));
        }
    }

    private void updateQuantitySelection(int position) {
        // Update the selected quantity
        selectedQuantity = position + 1;
        if (position == 4) { // For 5+ button
            selectedQuantity = 5;
        }

        // Update UI to show selection
        for (int i = 0; i < quantityButtons.size(); i++) {
            MaterialButton button = quantityButtons.get(i);
            if (i == position) {
                // Selected button style
                button.setStrokeWidth(0);
                button.setBackgroundColor(getResources().getColor(R.color.green));
                button.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                // Unselected button style
                button.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.button_stroke_width));
                button.setStrokeColor(getColorStateList(R.color.button_stroke_color));
                button.setBackgroundColor(getResources().getColor(android.R.color.white));
                button.setTextColor(getResources().getColor(R.color.button_text_color));
            }
        }
    }

    private void showCustomQuantityDialog() {
        // Create a dialog for custom quantity input
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
                                // Deselect all quantity buttons
                                for (MaterialButton button : quantityButtons) {
                                    button.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.button_stroke_width));
                                    button.setStrokeColor(getColorStateList(R.color.button_stroke_color));
                                    button.setBackgroundColor(getResources().getColor(android.R.color.white));
                                    button.setTextColor(getResources().getColor(R.color.button_text_color));
                                }

                                // Highlight the "Other" button
                                binding.btnQuantityOther.setBackgroundColor(getResources().getColor(R.color.green));
                                binding.btnQuantityOther.setText("Other: " + customQty);
                                binding.btnQuantityOther.setTextColor(getResources().getColor(android.R.color.white));
                            } else {
                                Toast.makeText(AddFoodActivity.this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(AddFoodActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void setupDaysSpinner() {
        Spinner spinnerDays = binding.spinnerDays;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.spinner_days_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDays.setAdapter(adapter);
    }

    private void setupImageCapture() {
        // Setup image selection from camera
        binding.ivCamera.setOnClickListener(v -> showImageSelectionOptions());

        // Setup FAB for adding photos
        binding.fabAddPhoto.setOnClickListener(v -> showImageSelectionOptions());
    }

    private void showImageSelectionOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source")
                .setItems(new CharSequence[]{"Camera", "Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        // Camera option
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        } else {
                            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Gallery option
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        Intent chooser = Intent.createChooser(intent, "Select an image");
                        if (chooser.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(chooser, REQUEST_PICK_IMAGE);
                        } else {
                            Toast.makeText(this, "No gallery app installed", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .create()
                .show();
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddFoodActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format date as mm/dd/yyyy
                    String date = String.format("%02d/%02d/%d", selectedMonth + 1, selectedDay, selectedYear);
                    binding.etExpirationDate.setText(date);
                },
                year, month, day);

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_PICK_IMAGE || requestCode == REQUEST_IMAGE_CAPTURE)
                && resultCode == RESULT_OK && data != null) {

            // Get the main photo container
            FrameLayout photoContainer = binding.getRoot().findViewById(R.id.photo_container);

            // Hide the camera icon and show the actual image
            binding.ivCamera.setVisibility(View.GONE);

            // Create an ImageView with match_parent dimensions
            ImageView imageView = new ImageView(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER; // Centrează imaginea în container
            imageView.setLayoutParams(params);

            if (requestCode == REQUEST_PICK_IMAGE) {
                Uri imageUri = data.getData();
                try {
                    // Load and display the selected image
                    imageView.setImageURI(imageUri);

                    // Remove any existing loaded images and add the new one
                    if (imageLoaded) {
                        // Remove all views except the camera icon and FAB
                        int childCount = photoContainer.getChildCount();
                        for (int i = childCount - 1; i >= 0; i--) {
                            View child = photoContainer.getChildAt(i);
                            if (child != binding.ivCamera && child != binding.fabAddPhoto) {
                                photoContainer.removeView(child);
                            }
                        }
                    }

                    photoContainer.addView(imageView, 0); // Add at the bottom of the stack
                    imageLoaded = true;

                    // Make sure the add photo text reflects the current state
                    TextView addImagesText = binding.getRoot().findViewById(R.id.tv_add_images);
                    if (addImagesText != null) {
                        addImagesText.setText("1 of 10 photos added");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                // Display the captured image
                imageView.setImageBitmap(imageBitmap);

                // Remove any existing loaded images and add the new one
                if (imageLoaded) {
                    // Remove all views except the camera icon and FAB
                    int childCount = photoContainer.getChildCount();
                    for (int i = childCount - 1; i >= 0; i--) {
                        View child = photoContainer.getChildAt(i);
                        if (child != binding.ivCamera && child != binding.fabAddPhoto) {
                            photoContainer.removeView(child);
                        }
                    }
                }

                photoContainer.addView(imageView, 0); // Add at the bottom of the stack
                imageLoaded = true;

                // Make sure the add photo text reflects the current state
                TextView addImagesText = binding.getRoot().findViewById(R.id.tv_add_images);
                if (addImagesText != null) {
                    addImagesText.setText("1 of 10 photos added");
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        // Set initial location (e.g., Bucharest)
        LatLng bucharest = new LatLng(44.4268, 26.1025);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bucharest, 12f));
        mMap.addMarker(new MarkerOptions().position(bucharest).title("Bucharest"));

        // Let user tap to set location
        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        });
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void validateAndPost() {
        // Validate required fields
        if (binding.etTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (binding.etExpirationDate.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select an expiration date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (binding.etPickupTimes.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter pickup times", Toast.LENGTH_SHORT).show();
            return;
        }

        // All validations passed, post the item
        Toast.makeText(this, "Food item posted successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}