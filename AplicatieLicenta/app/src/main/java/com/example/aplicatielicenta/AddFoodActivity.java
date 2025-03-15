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
    private LatLng selectedLatLng = null; // Variabilă pentru locația selectată

    private Uri selectedImageUri = null;

    // URL-ul API pentru încărcare în Cloudinary
    private static final String CLOUDINARY_UPLOAD_URL = "https://api.cloudinary.com/v1_1/du55ivt1v/image/upload";

    private static final String UPLOAD_PRESET = "Photos";

    private List<Uri> selectedImages = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private FusedLocationProviderClient fusedLocationProviderClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFoodBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        selectedImages = new ArrayList<>();
        imageAdapter = new ImageAdapter(this, selectedImages, this::removeImage);

        // Configurăm RecyclerView
        binding.recyclerViewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewImages.setAdapter(imageAdapter);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize back button
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

        // Initialize date picker for expiration date
        binding.etExpirationDate.setOnClickListener(v -> showDatePickerDialog());

        // Setup camera/gallery functionality
        setupImageCapture();


        // Setup Post button

        binding.btnSelectLocation.setOnClickListener(v -> {
            if (selectedLocation == null) {
                // Deschide activitatea de selectare locație dacă nu ai deja o locație
                Intent intent = new Intent(AddFoodActivity.this, LocationPickerActivity.class);
                startActivityForResult(intent, REQUEST_LOCATION_PICKER);
            } else {
                // Trimite locația înapoi dacă deja ai selectat-o
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

    }

    private void setupImageCapture() {
        // Setup image selection from camera
        binding.ivCamera.setOnClickListener(v -> showImageSelectionOptions());

        // Setup FAB for adding photos
        binding.fabAddPhoto.setOnClickListener(v -> showImageSelectionOptions());
    }

    private void removeImage(int position) {
        if (position >= 0 && position < selectedImages.size()) {
            selectedImages.remove(position);
            imageAdapter.notifyDataSetChanged(); // Actualizăm RecyclerView-ul după ștergere
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
                    // Selectează din galerie
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

        if (resultCode == RESULT_OK && data != null) {

            // ✅ Dacă utilizatorul a selectat o locație
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

            // ✅ Dacă utilizatorul a selectat o imagine din galerie
            else if (requestCode == REQUEST_PICK_IMAGE) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    selectedImageUri = imageUri;
                    binding.ivCamera.setImageURI(selectedImageUri);
                } else {
                    Toast.makeText(this, "Failed to get image from gallery", Toast.LENGTH_SHORT).show();
                }
            }

            // ✅ Dacă utilizatorul a făcut o fotografie cu camera
            else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (data.getExtras() != null) {
                    Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                    if (imageBitmap != null) {
                        selectedImageUri = getImageUriFromBitmap(imageBitmap);
                        if (selectedImageUri != null) {
                            binding.ivCamera.setImageURI(selectedImageUri);
                        } else {
                            Toast.makeText(this, "Error converting image to URI", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Camera returned no data", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Operation canceled or failed", Toast.LENGTH_SHORT).show();
        }
    }


    // Convertim un Bitmap într-un URI pentru a-l putea salva
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

        // Verifică permisiunea și obține locația curentă
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                }
            });
        }

        // Permite utilizatorului să selecteze o locație
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

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_LOCATION_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                enableMyLocation();
//            } else {
//                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    private void saveProductToFirestore(Map<String, Object> productData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("products")
                .add(productData) // 🔥 Firebase generează automat un ID aici
                .addOnSuccessListener(documentReference -> {
                    // ✅ Obține ID-ul generat și actualizează documentul
                    String productId = documentReference.getId();
                    productData.put("id", productId);

                    documentReference.update("id", productId)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show();
                                finish(); // Închide activitatea după postare
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update product ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void validateAndPost() {
        Log.d("DEBUG", "Intrat în validateAndPost()");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.e("ERROR", "Utilizatorul nu este autentificat!");
            Toast.makeText(this, "You need to be logged in to post!", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("DEBUG", "Utilizator autentificat: " + currentUser.getUid());


        String userId = currentUser.getUid(); // Obține UID-ul utilizatorului curent

        String title = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String expirationDate = binding.etExpirationDate.getText().toString().trim();
        String pickupTimes = binding.etPickupTimes.getText().toString().trim();
        String pickupInstructions = binding.etPickupInstructions.getText().toString().trim();
        String category = "Food";

        if (selectedLatLng == null) {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isEmpty() || expirationDate.isEmpty() || pickupTimes.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // CORECTARE: Nu mai salvăm locația ca obiect separat, ci direct în document
        Map<String, Object> product = new HashMap<>();
        product.put("title", title);
        product.put("description", description);
        product.put("expirationDate", expirationDate);
        product.put("pickupTimes", pickupTimes);
        product.put("pickupInstructions", pickupInstructions);
        product.put("category", category);
        product.put("latitude", selectedLatLng.latitude);  // Salvăm direct latitudinea
        product.put("longitude", selectedLatLng.longitude); // Salvăm direct longitudinea
        product.put("userId", userId); // Salvăm UID-ul utilizatorului

        // Adăugăm imaginea dacă există
        if (selectedImageUri != null) {
            uploadImageToCloudinary(product);
        } else {
            // Salvăm produsul fără imagine
            saveProductToFirestore(product);
        }
    }

    // Modificăm metodele pentru a lucra cu un singur Map


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

    private void uploadImageToCloudinary(Map<String, Object> product) {
        if (selectedImageUri == null) {
            Log.e("ERROR", "selectedImageUri este null!");
            return;
        }

        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
        } catch (IOException e) {
            Log.e("ERROR", "Eroare la conversia imaginii: " + e.getMessage());
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
                    Log.e("ERROR", "Eroare la încărcarea imaginii: " + response.message());
                    return;
                }

                String responseBody = response.body().string();
                JSONObject jsonObject = new JSONObject(responseBody);
                String imageUrl = jsonObject.getString("secure_url");

                Log.d("DEBUG", "Imagine încărcată cu succes: " + imageUrl);
                product.put("imageUrl", imageUrl);

                runOnUiThread(() -> saveProductToFirestore(product));
            } catch (Exception e) {
                Log.e("ERROR", "Eroare la încărcarea imaginii: " + e.getMessage());
            }
        }).start();
    }


}