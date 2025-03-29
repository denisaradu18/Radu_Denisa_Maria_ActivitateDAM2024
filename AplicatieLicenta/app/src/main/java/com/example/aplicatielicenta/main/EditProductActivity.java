package com.example.aplicatielicenta.main;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.aplicatielicenta.R;
import com.example.aplicatielicenta.adapters.ImageAdapter;
import com.example.aplicatielicenta.adapters.ImageUrlAdapter;
import com.example.aplicatielicenta.models.Product;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EditProductActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText etTitle, etDescription, etPickupTimes, etPickupInstructions;
    private MaterialButton btnSave, btnSelectLocation;
    private RecyclerView recyclerViewImages;

    private ImageUrlAdapter imageAdapter;

    private GoogleMap map;
    private double latitude, longitude;
    private String productId;
    private List<String> imageUrls;
    private EditText etExpirationDate;
    private Spinner spinnerQuantity, spinnerListForDays;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etPickupTimes = findViewById(R.id.et_pickup_times);
        etPickupInstructions = findViewById(R.id.et_pickup_instructions);
        btnSave = findViewById(R.id.btn_post);
        btnSelectLocation = findViewById(R.id.btn_select_location);
        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        recyclerViewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        etExpirationDate = findViewById(R.id.et_expiration_date);
        spinnerQuantity = findViewById(R.id.spinner_quantity);
        spinnerListForDays = findViewById(R.id.spinner_list_for_days);

        ArrayAdapter<CharSequence> quantityAdapter = ArrayAdapter.createFromResource(this,
                R.array.quantity_options, android.R.layout.simple_spinner_item);
        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        ArrayAdapter<CharSequence> daysAdapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_days_options, android.R.layout.simple_spinner_item);
        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_container);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Intent intent = getIntent();
        productId = intent.getStringExtra("PRODUCT_ID");
        etTitle.setText(intent.getStringExtra("title"));
        etDescription.setText(intent.getStringExtra("description"));
        etPickupTimes.setText(intent.getStringExtra("pickupTimes"));
        etPickupInstructions.setText(intent.getStringExtra("pickupInstructions"));
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);
        imageUrls = intent.getStringArrayListExtra("imageUrls");

        String expirationDate = intent.getStringExtra("expirationDate");
        String quantity = intent.getStringExtra("quantity");
        int listForDays = intent.getIntExtra("listForDays", 0);

        if (expirationDate != null) {
            etExpirationDate.setText(expirationDate);
        }

        spinnerQuantity.setAdapter(quantityAdapter);
        spinnerQuantity.post(() -> {
            if (quantity != null && !quantity.isEmpty()) {
                Log.d("EditProduct", "Setting quantity spinner to: " + quantity);
                boolean found = false;
                for (int i = 0; i < spinnerQuantity.getCount(); i++) {
                    if (quantity.equals(spinnerQuantity.getItemAtPosition(i).toString())) {
                        spinnerQuantity.setSelection(i);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    setSpinnerSelection(spinnerQuantity, quantity);
                }
            }
        });

        spinnerListForDays.setAdapter(daysAdapter);
        spinnerListForDays.post(() -> {
            Log.d("EditProduct", "Setting days spinner to index: " + listForDays);

            Log.d("EditProduct", "Spinner Days count: " + spinnerListForDays.getCount());

            if (listForDays >= 0 && listForDays < spinnerListForDays.getCount()) {
                spinnerListForDays.setSelection(listForDays);
                Log.d("EditProduct", "Selected day at position: " + listForDays +
                        ", Value: " + spinnerListForDays.getItemAtPosition(listForDays));
            } else {
                Log.e("EditProduct", "Invalid listForDays value: " + listForDays +
                        ", outside range 0-" + (spinnerListForDays.getCount() - 1));

                spinnerListForDays.setSelection(0);
            }
        });

        if (imageUrls == null) imageUrls = new ArrayList<>();

        imageAdapter = new ImageUrlAdapter(this, imageUrls);
        recyclerViewImages.setAdapter(imageAdapter);

        btnSelectLocation.setOnClickListener(v -> {
            FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }

            locationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    if (map != null) {
                        LatLng newLocation = new LatLng(latitude, longitude);
                        map.clear();
                        map.addMarker(new MarkerOptions().position(newLocation).title("Your Location"));
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 15));
                    }
                }
            });
        });

        btnSave.setOnClickListener(v -> updateProduct());
    }
    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }


    private void updateProduct() {
        FirebaseFirestore.getInstance().collection("products")
                .document(productId)
                .update("title", etTitle.getText().toString(),
                        "description", etDescription.getText().toString(),
                        "pickupTimes", etPickupTimes.getText().toString(),
                        "pickupInstructions", etPickupInstructions.getText().toString(),
                        "latitude", latitude,
                        "longitude", longitude,
                        "expirationDate", etExpirationDate.getText().toString(),
                        "quantity", spinnerQuantity.getSelectedItem().toString(),
                        "listForDays", String.valueOf(spinnerListForDays.getSelectedItemPosition())
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Product updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed.", Toast.LENGTH_SHORT).show());
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        if (latitude != 0 && longitude != 0) {
            LatLng position = new LatLng(latitude, longitude);
            map.addMarker(new MarkerOptions().position(position).title("Current Location"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        }
    }
}
