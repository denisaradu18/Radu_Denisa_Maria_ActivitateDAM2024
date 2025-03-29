package com.example.aplicatielicenta.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.aplicatielicenta.adapters.CustomInfoWindowAdapter;
import com.example.aplicatielicenta.product.ProductBottomSheet;
import com.example.aplicatielicenta.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final float DEFAULT_ZOOM = 15f;

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean locationPermissionGranted = false;
    private Location lastKnownLocation;
    private SupportMapFragment mapFragment;
    private Map<String, String> markerImageMap = new HashMap<>(); // <MarkerId, ImageURL>

    private View productCard;
    private ImageView cardImage;
    private TextView cardTitle, cardDescription;
    private Button cardButton;


    public MapFragment() {
        // Constructor gol necesar pentru fragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        ImageButton zoomIn = view.findViewById(R.id.zoom_in_button);
        ImageButton zoomOut = view.findViewById(R.id.zoom_out_button);

        zoomIn.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        zoomOut.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

        // Inițializează Firestore
        db = FirebaseFirestore.getInstance();

        // Inițializează FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Inițializează fragmentul de hartă programatic
        mapFragment = SupportMapFragment.newInstance();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.map_container, mapFragment).commit();

        EditText searchInput = view.findViewById(R.id.search_input);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMarkers(s.toString().trim().toLowerCase());
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });



        // Obține callback-ul când harta este gata
        mapFragment.getMapAsync(this);

        productCard = view.findViewById(R.id.product_card);
        cardImage = view.findViewById(R.id.card_image);
        cardTitle = view.findViewById(R.id.card_title);
        cardDescription = view.findViewById(R.id.card_description);
        cardButton = view.findViewById(R.id.card_button);

        return view;
    }

    private List<Marker> markerList = new ArrayList<>();

    private void filterMarkers(String query) {
        for (Marker marker : markerList) {
            String title = marker.getTitle() != null ? marker.getTitle().toLowerCase() : "";
            String snippet = marker.getSnippet() != null ? marker.getSnippet().toLowerCase() : "";
            boolean visible = title.contains(query) || snippet.contains(query);
            marker.setVisible(visible);
        }
    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(requireContext(), markerImageMap));

        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
        loadProductsFromFirestore();

        mMap.setOnMarkerClickListener(marker -> {
            String productId = (String) marker.getTag();
            if (productId != null) {
                db.collection("products").document(productId).get()
                        .addOnSuccessListener(document -> {
                            if (document.exists()) {
                                String title = document.getString("title");
                                String description = document.getString("description");
                                List<String> imageUrls = (List<String>) document.get("imageUrls");

                                ProductBottomSheet bottomSheet = new ProductBottomSheet(productId, title, description, imageUrls);
                                bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
                            }
                        });
            }
            return true;
        });
        mMap.setOnCameraIdleListener(() -> {
            LatLng center = mMap.getCameraPosition().target;
            float zoom = mMap.getCameraPosition().zoom;

            Log.d("Map", "📍 Centru hartă: " + center.latitude + ", " + center.longitude + " | Zoom: " + zoom);

            // Poți folosi aici: să încarci produse într-un anumit radius de la centru
            loadProductsNear(center.latitude, center.longitude, 10); // 10km de ex
        });

        mMap.setOnMapClickListener(latLng -> {
            if (productCard.getVisibility() == View.VISIBLE) {
                productCard.setVisibility(View.GONE);
            }
        });


    }
    private void loadProductsNear(double lat, double lng, double radiusKm) {
        db.collection("products")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    mMap.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Double productLat = document.getDouble("latitude");
                        Double productLng = document.getDouble("longitude");

                        if (productLat != null && productLng != null) {
                            float[] results = new float[1];
                            Location.distanceBetween(lat, lng, productLat, productLng, results);
                            float distanceInMeters = results[0];
                            if (distanceInMeters <= radiusKm * 1000) {
                                // Afișează marker
                                LatLng position = new LatLng(productLat, productLng);
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(position)
                                        .title(document.getString("title"))
                                        .snippet(document.getString("description"));
                                Marker marker = mMap.addMarker(markerOptions);
                                if (marker != null) {
                                    marker.setTag(document.getId());
                                }
                            }
                        }
                    }
                });
    }



    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Dacă cererea este anulată, array-ul rezultatelor este gol
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(requireActivity(), location -> {
                            if (location != null) {
                                lastKnownLocation = location;
                                LatLng currentLocation = new LatLng(
                                        lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        currentLocation, DEFAULT_ZOOM));
                            } else {
                                requestLocationUpdates();
                            }
                        });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    private void requestLocationUpdates() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    lastKnownLocation = location;
                    LatLng currentLocation = new LatLng(
                            lastKnownLocation.getLatitude(),
                            lastKnownLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            currentLocation, DEFAULT_ZOOM));

                    // Oprim actualizările locației după ce am primit una
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                    break;
                }
            }
        };

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try {
            if (locationPermissionGranted) {
                fusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback, Looper.getMainLooper());
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }


    private void loadProductsFromFirestore() {
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Verificăm dacă produsul are locație validă
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");

                            if (latitude != null && longitude != null) {
                                String title = document.getString("title");
                                String description = document.getString("description");

                                // 🔽 Obținem prima imagine (dacă există)
                                List<String> imageUrls = (List<String>) document.get("imageUrls");
                                String firstImageUrl = (imageUrls != null && !imageUrls.isEmpty()) ?
                                        imageUrls.get(0) : "";

                                LatLng productLocation = new LatLng(latitude, longitude);

                                // Creăm markerul
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(productLocation)
                                        .title(title != null ? title : "Produs fără titlu")
                                        .snippet(description != null ? description : "Fără descriere")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                                com.google.android.gms.maps.model.Marker marker = mMap.addMarker(markerOptions);
                                if (marker != null) {
                                    marker.setTag(document.getId());
                                    markerList.add(marker); // ✅ pentru filtrare

                                    // 🔥 ADĂUGĂM imaginea în map pentru InfoWindow
                                    markerImageMap.put(marker.getId(), firstImageUrl);
                                }
                            }
                        }

                        Log.d(TAG, "Toate produsele au fost încărcate pe hartă.");
                    } else {
                        Log.e(TAG, "Eroare la încărcarea produselor: ", task.getException());
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}