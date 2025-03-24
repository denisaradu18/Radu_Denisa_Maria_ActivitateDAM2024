package com.example.aplicatielicenta;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.aplicatielicenta.databinding.ActivityAddFoodBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddFoodActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng selectedLocation;
    private ActivityAddFoodBinding binding;

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int REQUEST_LOCATION_PICKER = 3;

    private List<MaterialButton> quantityButtons = new ArrayList<>();

    private boolean imageLoaded = false;
    private LatLng selectedLatLng = null;

    private List<Uri> selectedImages = new ArrayList<>();
    private ImageAdapter imageAdapter;


    private static final String CLOUDINARY_UPLOAD_URL = "https://api.cloudinary.com/v1_1/du55ivt1v/image/upload";

    private static final String UPLOAD_PRESET = "Photos";

    private FusedLocationProviderClient fusedLocationProviderClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFoodBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        selectedImages = new ArrayList<>();
        imageAdapter = new ImageAdapter(this, selectedImages, this::removeImage);

        binding.recyclerViewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewImages.setAdapter(imageAdapter);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        binding.btnBack.setOnClickListener(v -> finish());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1002);
        }

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map_container);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        binding.etExpirationDate.setOnClickListener(v -> showDatePickerDialog());

        setupImageCapture();



        binding.btnSelectLocation.setOnClickListener(v -> {
            if (selectedLocation == null) {
                Intent intent = new Intent(AddFoodActivity.this, LocationPickerActivity.class);
                startActivityForResult(intent, REQUEST_LOCATION_PICKER);
            } else {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", selectedLocation.latitude);
                resultIntent.putExtra("longitude", selectedLocation.longitude);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        binding.btnPost.setOnClickListener(v -> {
            Log.d("DEBUG", "Butonul Post a fost apăsat");
            validateAndPost();
        });

        Spinner spinnerDays = findViewById(R.id.spinner_list_for_days);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.spinner_days_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDays.setAdapter(adapter);

        Spinner spinnerQuantity = findViewById(R.id.spinner_quantity);
        ArrayAdapter<CharSequence> quantityAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.quantity_options,
                android.R.layout.simple_spinner_item);
        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQuantity.setAdapter(quantityAdapter);


    }

    private void setupImageCapture() {
        // Only use the FAB for adding photos
        binding.fabAddPhoto.setOnClickListener(v -> showImageSelectionOptions());
    }

    private void removeImage(int position) {
        if (position >= 0 && position < selectedImages.size()) {
            selectedImages.remove(position);
            imageAdapter.notifyDataSetChanged();
        }
    }


    private void showImageSelectionOptions() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Select Image Source")
            .setItems(new CharSequence[]{"Camera", "Gallery"}, (dialog, which) -> {
                if (which == 0) {
                    // Deschide camera
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    } else {
                        Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_PICK_IMAGE);
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

                    String date = String.format("%02d/%02d/%d", selectedMonth + 1, selectedDay, selectedYear);
                    binding.etExpirationDate.setText(date);
                },
                year, month, day);

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            if (requestCode == REQUEST_LOCATION_PICKER) {
                double latitude = data.getDoubleExtra("latitude", 0.0);
                double longitude = data.getDoubleExtra("longitude", 0.0);

                if (latitude != 0.0 && longitude != 0.0) {
                    selectedLatLng = new LatLng(latitude, longitude);

                    if (mMap != null) {
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(selectedLatLng).title("Selected Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15f));
                    }

                    binding.btnSelectLocation.setText("Location Selected");
                } else {
                    Toast.makeText(this, "Invalid location received!", Toast.LENGTH_SHORT).show();
                }
            }

            else if (requestCode == REQUEST_PICK_IMAGE) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    Log.d("DEBUG", "Galerie: imagine selectată - " + imageUri.toString());
                    selectedImages.add(imageUri);
                    imageAdapter.notifyDataSetChanged();
                } else {
                    Log.e("ERROR", "Galerie: imaginea selectată este NULL");
                    Toast.makeText(this, "Failed to load image from gallery", Toast.LENGTH_SHORT).show();
                }
            }

            if (data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                if (imageBitmap != null) {
                    Uri imageUri = getImageUriFromBitmap(imageBitmap);
                    if (imageUri != null) {
                        selectedImages.add(imageUri);
                        imageAdapter.notifyDataSetChanged();

                        selectedImages.add(imageUri);
                        imageAdapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(this, "Error converting image to URI", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Camera returned no data", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Operation canceled or failed", Toast.LENGTH_SHORT).show();
        }
    }


    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "CapturedImage", null);
        if (path == null) {
            return null;
        }

        return Uri.parse(path);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                }
            });
        }

        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            selectedLocation = latLng;
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

    private void saveProductToFirestore(Map<String, Object> productData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Generate the ID first
        String productId = db.collection("products").document().getId();

        // Add the ID to the product data before writing
        productData.put("id", productId);

        // Always ensure imageUrl exists (required by your rules)
        if (!productData.containsKey("imageUrl")) {
            List<String> imageUrls = (List<String>) productData.get("imageUrls");
            productData.put("imageUrl", imageUrls != null && !imageUrls.isEmpty() ? imageUrls.get(0) : "");
        }

        // Now create the document with a specific ID
        db.collection("products").document(productId)
                .set(productData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

//    private void validateAndPost() {
//        Log.d("DEBUG", "Intrat în validateAndPost()");
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//        FirebaseUser currentUser = auth.getCurrentUser();
//
//        if (currentUser == null) {
//            Log.e("ERROR", "Utilizatorul nu este autentificat!");
//            Toast.makeText(this, "You need to be logged in to post!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        Log.d("DEBUG", "Utilizator autentificat: " + currentUser.getUid());
//
//        String userId = currentUser.getUid();
//        String title = binding.etTitle.getText().toString().trim();
//        String description = binding.etDescription.getText().toString().trim();
//        String expirationDate = binding.etExpirationDate.getText().toString().trim();
//        String pickupTimes = binding.etPickupTimes.getText().toString().trim();
//        String pickupInstructions = binding.etPickupInstructions.getText().toString().trim();
//        String category = "Food";
//        Spinner spinnerQuantity = findViewById(R.id.spinner_quantity);
//        String quantity = spinnerQuantity.getSelectedItem().toString();
//        if (quantity.equals("Select quantity")) {
//            Toast.makeText(this, "Please select a valid quantity", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Spinner spinnerDays = findViewById(R.id.spinner_list_for_days);
//        String listForDays = spinnerDays.getSelectedItem().toString();
//
//
//        if (selectedLatLng == null) {
//            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (title.isEmpty() || expirationDate.isEmpty() || pickupTimes.isEmpty()) {
//            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        ArrayList<String> imageUrls = new ArrayList<>();
//
//        Map<String, Object> product = new HashMap<>();
//        product.put("title", title);
//        product.put("description", description);
//        product.put("expirationDate", expirationDate);
//        product.put("pickupTimes", pickupTimes);
//        product.put("pickupInstructions", pickupInstructions);
//        product.put("category", category);
//        product.put("latitude", selectedLatLng.latitude);
//        product.put("longitude", selectedLatLng.longitude);
//        product.put("userId", userId);
//        product.put("quantity", quantity);
//        product.put("listForDays", listForDays);
//        product.put("postedAt", com.google.firebase.Timestamp.now());
//
//        product.put("imageUrls", imageUrls);
//
//        product.put("username", actualUsername); // îl iei din FirebaseAuth sau user snapshot
//
//
//        if (!selectedImages.isEmpty()) {
//            final int[] uploadedCount = {0};
//            final int totalImages = selectedImages.size();
//
//            Log.d("DEBUG", "Starting upload of " + totalImages + " images");
//
//            for (Uri imageUri : selectedImages) {
//                uploadImageToCloudinary(imageUri, new CloudinaryUploadCallback() {
//                    @Override
//                    public void onSuccess(String imageUrl) {
//                        Log.d("DEBUG", "Image uploaded successfully: " + imageUrl);
//
//                        imageUrls.add(imageUrl);
//                        uploadedCount[0]++;
//
//                        if (uploadedCount[0] == totalImages) {
//                            product.put("imageUrls", imageUrls);
//
//                            if (!imageUrls.isEmpty()) {
//                                product.put("imageUrl", imageUrls.get(0));
//                            }
//
//                            Log.d("DEBUG", "All images uploaded. Saving product with " + imageUrls.size() + " images");
//                            saveProductToFirestore(product);
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(String errorMessage) {
//                        Log.e("ERROR", "Failed to upload image: " + errorMessage);
//                        uploadedCount[0]++;
//
//                        if (uploadedCount[0] == totalImages) {
//                            product.put("imageUrls", imageUrls);
//                            if (!imageUrls.isEmpty()) {
//                                product.put("imageUrl", imageUrls.get(0));
//                            }
//                            Log.d("DEBUG", "Some images failed to upload. Saving product with " + imageUrls.size() + " images");
//                            saveProductToFirestore(product);
//                        }
//                    }
//                });
//            }
//        } else {
//            Log.d("DEBUG", "No images to upload. Saving product.");
//            saveProductToFirestore(product);
//        }
//    }

    private void validateAndPost() {
        Log.d("DEBUG", "Intrat în validateAndPost()");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in to post!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // 🔽 Obținem username-ul din Firestore (colecția "users")
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(userSnapshot -> {
                    String username = userSnapshot.getString("username");
                    if (username == null) {
                        username = "Unknown";
                    }

                    // După ce am obținut username-ul, continuăm cu postarea
                    postProduct(userId, username);

                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "❌ Eroare la obținerea username-ului", e);
                    Toast.makeText(this, "Error retrieving user info", Toast.LENGTH_SHORT).show();
                });
    }

    private void postProduct(String userId, String username) {
        String title = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String expirationDate = binding.etExpirationDate.getText().toString().trim();
        String pickupTimes = binding.etPickupTimes.getText().toString().trim();
        String pickupInstructions = binding.etPickupInstructions.getText().toString().trim();
        String category = "Food";

        Spinner spinnerQuantity = findViewById(R.id.spinner_quantity);
        String quantity = spinnerQuantity.getSelectedItem().toString();
        if (quantity.equals("Select quantity")) {
            Toast.makeText(this, "Please select a valid quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        Spinner spinnerDays = findViewById(R.id.spinner_list_for_days);
        String listForDays = spinnerDays.getSelectedItem().toString();

        if (selectedLatLng == null) {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isEmpty() || expirationDate.isEmpty() || pickupTimes.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> imageUrls = new ArrayList<>();
        Map<String, Object> product = new HashMap<>();
        product.put("title", title);
        product.put("description", description);
        product.put("expirationDate", expirationDate);
        product.put("pickupTimes", pickupTimes);
        product.put("pickupInstructions", pickupInstructions);
        product.put("category", category);
        product.put("latitude", selectedLatLng.latitude);
        product.put("longitude", selectedLatLng.longitude);
        product.put("userId", userId);
        product.put("username", username); // ✅ Acum îl avem!
        product.put("quantity", quantity);
        product.put("listForDays", listForDays);
        product.put("postedAt", com.google.firebase.Timestamp.now());
        product.put("imageUrls", imageUrls);

        if (!selectedImages.isEmpty()) {
            final int[] uploadedCount = {0};
            final int totalImages = selectedImages.size();

            for (Uri imageUri : selectedImages) {
                uploadImageToCloudinary(imageUri, new CloudinaryUploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        imageUrls.add(imageUrl);
                        uploadedCount[0]++;
                        if (uploadedCount[0] == totalImages) {
                            product.put("imageUrls", imageUrls);
                            product.put("imageUrl", imageUrls.get(0));
                            saveProductToFirestore(product);
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        uploadedCount[0]++;
                        if (uploadedCount[0] == totalImages) {
                            product.put("imageUrls", imageUrls);
                            if (!imageUrls.isEmpty()) {
                                product.put("imageUrl", imageUrls.get(0));
                            }
                            saveProductToFirestore(product);
                        }
                    }
                });
            }
        } else {
            saveProductToFirestore(product);
        }
    }


    interface CloudinaryUploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
    }

    private void uploadImageToCloudinary(Uri imageUri, CloudinaryUploadCallback callback) {
        if (imageUri == null) {
            callback.onFailure("Image URI is null");
            return;
        }

        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        } catch (IOException e) {
            callback.onFailure("Error converting image: " + e.getMessage());
            return;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg", RequestBody.create(imageBytes, MediaType.parse("image/jpeg")))
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .build();

        Request request = new Request.Builder()
                .url(CLOUDINARY_UPLOAD_URL)
                .post(requestBody)
                .build();

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> callback.onFailure("Error uploading image: " + response.message()));
                    return;
                }

                String responseBody = response.body().string();
                JSONObject jsonObject = new JSONObject(responseBody);
                String imageUrl = jsonObject.getString("secure_url");

                Log.d("DEBUG", "Image uploaded successfully: " + imageUrl);
                runOnUiThread(() -> callback.onSuccess(imageUrl));
            } catch (Exception e) {
                runOnUiThread(() -> callback.onFailure("Error uploading image: " + e.getMessage()));
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1002) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }


}

