package com.example.aplicatielicenta;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> fullProductList; // Lista de produse
    private EditText searchBar;
    private ImageView searchIcon;

    private double userLatitude = 0.0;
    private double userLongitude = 0.0;
    private MaterialButton btnAll, btnFood, btnNonFood;


    public HomeFragment(List<Product> productList) {
        this.fullProductList = productList;
    }

    public HomeFragment() {
        this.fullProductList = new ArrayList<>(); // Inițializare goală
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        requestLocationPermission();

        recyclerView = view.findViewById(R.id.recyclerView);
        searchBar    = view.findViewById(R.id.search_bar);
        searchIcon   = view.findViewById(R.id.search_icon);
        btnAll = view.findViewById(R.id.btn_all);
        btnFood = view.findViewById(R.id.btn_food);
        btnNonFood = view.findViewById(R.id.btn_non_food);
        btnAll.setOnClickListener(v -> filterByTab("All"));
        btnFood.setOnClickListener(v -> filterByTab("Food"));
        btnNonFood.setOnClickListener(v -> filterByTab("Non-Food"));

        productAdapter = new ProductAdapter(new ArrayList<>() , userLatitude, userLongitude);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(productAdapter);

        if (fullProductList == null || fullProductList.isEmpty()) {
            loadProductsFromFirestore();
        }
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    productAdapter.resetList();
                } else {
                    filterBySearch(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        return view;
    }
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            getUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation(); // ✅ Dacă permisiunea este acordată, obținem locația
        } else {
            Toast.makeText(getContext(), "Permisiunea de locație a fost refuzată", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadProductsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("Firestore", "📥 Începem încărcarea produselor...");

        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Firestore", "✅ Produse încărcate! Număr: " + task.getResult().size());

                        fullProductList.clear();
                        List<Product> tempProductList = new ArrayList<>();
                        Set<String> uniqueUserIds = new HashSet<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title") != null ? document.getString("title") : "No title";
                            String description = document.getString("description") != null ? document.getString("description") : "No description";
                            String category = document.getString("category") != null ? document.getString("category") : "Uncategorized";

                            List<String> imageUrls = (List<String>) document.get("imageUrls");
                            if (imageUrls == null) imageUrls = new ArrayList<>();

                            String userId = document.getString("userId");
                            String productId = document.getId();

                            Log.d("Firestore", "🛒 Produs: " + title + " | UserId: " + userId);

                            double latitude = document.getDouble("latitude") != null ? document.getDouble("latitude") : 0.0;
                            double longitude = document.getDouble("longitude") != null ? document.getDouble("longitude") : 0.0;

                            Product newProduct = new Product(
                                    productId, title, description, "", "", "",
                                    category, latitude, longitude, imageUrls, "Loading..."
                            );

                            tempProductList.add(newProduct);
                            uniqueUserIds.add(userId);

                        }

                        fullProductList.addAll(tempProductList);

                        if (uniqueUserIds.isEmpty()) {
                            Log.d("Firestore", "⚠️ Nu există userId-uri pentru produse");
                            productAdapter.updateList(new ArrayList<>(fullProductList));
                            return;
                        }

                        AtomicInteger completedRequests = new AtomicInteger(0);
                        int totalRequests = tempProductList.size(); // câte produse sunt, nu câți useri unici

                        for (int i = 0; i < tempProductList.size(); i++) {
                            final int index = i;

                            String userId = tempProductList.get(i).getUserId(); // ✅ accesezi userId-ul direct din produs

                            if (userId != null && !userId.isEmpty()) {
                                db.collection("users").document(userId)
                                        .get()
                                        .addOnSuccessListener(userDoc -> {
                                            if (userDoc.exists()) {
                                                String username = userDoc.getString("username");
                                                Log.d("Firestore", "👤 UserId: " + userId + " | Username: " + username);

                                                if (username != null && !username.isEmpty()) {
                                                    fullProductList.get(index).setUsername(username);
                                                } else {
                                                    fullProductList.get(index).setUsername("Anonim");
                                                }
                                            } else {
                                                Log.d("Firestore", "⚠️ UserId: " + userId + " NU are document în `users`!");
                                                fullProductList.get(index).setUsername("Anonim");
                                            }

                                            if (completedRequests.incrementAndGet() == totalRequests) {
                                                Log.d("Firestore", "🎉 Toate username-urile au fost încărcate!");
                                                productAdapter.updateList(new ArrayList<>(fullProductList));
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "❌ Eroare la preluarea username-ului!", e);
                                            fullProductList.get(index).setUsername("Anonim");

                                            if (completedRequests.incrementAndGet() == totalRequests) {
                                                productAdapter.updateList(new ArrayList<>(fullProductList));
                                            }
                                        });
                            } else {
                                Log.d("Firestore", "⚠️ UserId este NULL pentru produsul " + fullProductList.get(index).getTitle());
                                fullProductList.get(index).setUsername("Anonim");

                                if (completedRequests.incrementAndGet() == totalRequests) {
                                    productAdapter.updateList(new ArrayList<>(fullProductList));
                                }
                            }
                        }
                    } else {
                        Log.e("Firestore", "❌ Eroare la încărcarea produselor", task.getException());
                    }
                });
    }

    private void filterByTab(String category) {
        List<Product> filteredList = new ArrayList<>();

        Log.d("CategoryFilter", "Selected tab: '" + category + "'");

        if (category.equalsIgnoreCase("All")) {
            filteredList.addAll(fullProductList);
        } else {
            for (Product product : fullProductList) {
                Log.d("CategoryFilter", "Product: " + product.getTitle() +
                        ", Category: '" + product.getCategory() + "'");

                if (product.getCategory().trim().equalsIgnoreCase(category.trim())) {
                    filteredList.add(product);
                }
            }
        }

        Log.d("CategoryFilter", "Total products: " + fullProductList.size() +
                ", Filtered products: " + filteredList.size());

        productAdapter.updateList(filteredList);
    }

    private void filterBySearch(String query) {
        if (query.isEmpty()) {
            productAdapter.updateList(new ArrayList<>(fullProductList));
        } else {
            List<Product> filteredList = new ArrayList<>();
            for (Product product : fullProductList) {
                if (product.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(product);
                }
            }
            productAdapter.updateList(filteredList);
        }
    }

    private void requestNewLocationData() {
        if (getActivity() == null) return;

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        com.google.android.gms.location.LocationRequest locationRequest = com.google.android.gms.location.LocationRequest.create();
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setNumUpdates(1);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    userLatitude = locationResult.getLastLocation().getLatitude();
                    userLongitude = locationResult.getLastLocation().getLongitude();
                    Log.d("HomeFragment", "📍 Locație nouă: Lat = " + userLatitude + ", Lng = " + userLongitude);

                    // Evităm crash dacă fragmentul nu este atașat
                    if (getActivity() != null) {
                        productAdapter = new ProductAdapter(fullProductList, userLatitude, userLongitude);
                        recyclerView.setAdapter(productAdapter);
                        productAdapter.notifyDataSetChanged();
                    }
                }
            }
        }, null);
    }

    private void getUserLocation() {
        if (getActivity() == null) return;

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLatitude = location.getLatitude();
                        userLongitude = location.getLongitude();
                        Log.d("HomeFragment", "✅ Locație utilizator: Lat = " + userLatitude + ", Lng = " + userLongitude);

                        if (getActivity() != null) {
                            productAdapter = new ProductAdapter(fullProductList, userLatitude, userLongitude);
                            recyclerView.setAdapter(productAdapter);
                        }
                    } else {
                        Log.e("HomeFragment", "⚠️ GPS dezactivat sau locație indisponibilă!");
                        requestNewLocationData();
                    }
                })
                .addOnFailureListener(e -> Log.e("HomeFragment", "⚠️ Eroare la obținerea locației!", e));
    }

}
