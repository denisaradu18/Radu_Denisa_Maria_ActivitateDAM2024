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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> fullProductList; // Lista de produse
    private EditText searchBar;
    private ImageView searchIcon;
    private TabLayout tabLayout;
    private double userLatitude = 0.0;
    private double userLongitude = 0.0;

    // 🔹 Constructor care acceptă o listă de produse
    public HomeFragment(List<Product> productList) {
        this.fullProductList = productList;
    }

    // 🔹 Constructor gol necesar pentru Fragment
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
        // Legăm view-urile
        recyclerView = view.findViewById(R.id.recyclerView);
        searchBar    = view.findViewById(R.id.search_bar);
        searchIcon   = view.findViewById(R.id.search_icon);
        tabLayout    = view.findViewById(R.id.tabLayout);

        // Setăm adapterul
        productAdapter = new ProductAdapter(new ArrayList<>() , userLatitude, userLongitude);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(productAdapter);

        // Dacă lista de produse este goală, o încărcăm din Firestore
        if (fullProductList == null || fullProductList.isEmpty()) {
            loadProductsFromFirestore();
        }

        // Listener pentru tab-uri
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterByTab(tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                filterByTab(tab.getText().toString());
            }
        });

        // Search live (ori de câte ori se tastează)
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBySearch(s.toString());
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
            getUserLocation(); // ✅ Dacă permisiunea există, obținem locația
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

//    private void loadProductsFromFirestore() {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        // Inițializăm/resetăm lista de produse
//        fullProductList = new ArrayList<>();
//
//        db.collection("products")
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        List<Product> tempProductList = new ArrayList<>();
//                        List<String> userIds = new ArrayList<>(); // Pentru a ține evidența userIds
//
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            // Preluăm fiecare câmp, cu fallback la valori implicite
//                            String title = document.getString("title") != null ? document.getString("title") : "No title";
//                            String description = document.getString("description") != null ? document.getString("description") : "No description";
//                            String expirationDate = document.getString("expirationDate") != null ? document.getString("expirationDate") : "Unknown";
//                            String pickupTimes = document.getString("pickupTimes") != null ? document.getString("pickupTimes") : "Not specified";
//                            String pickupInstructions = document.getString("pickupInstructions") != null ? document.getString("pickupInstructions") : "None";
//                            String category = document.getString("category") != null ? document.getString("category") : "Uncategorized";
//                            String imageUrl = document.getString("imageUrl") != null ? document.getString("imageUrl") : "";
//                            String userId = document.getString("userId") != null ? document.getString("userId") : null;
//
//                            // CORECTARE: Verificăm dacă latitudinea și longitudinea sunt stocate direct sau în obiectul location
//                            double latitude = 0.0;
//                            double longitude = 0.0;
//
//                            if (document.getDouble("latitude") != null) {
//                                // Direct în document
//                                latitude = document.getDouble("latitude");
//                                longitude = document.getDouble("longitude") != null ? document.getDouble("longitude") : 0.0;
//                            } else if (document.get("location") != null) {
//                                // În obiectul location
//                                Map<String, Object> location = (Map<String, Object>) document.get("location");
//                                if (location != null) {
//                                    if (location.get("latitude") instanceof Double) {
//                                        latitude = (Double) location.get("latitude");
//                                    }
//                                    if (location.get("longitude") instanceof Double) {
//                                        longitude = (Double) location.get("longitude");
//                                    }
//                                }
//                            }
//
//                            // Debugging pentru locație
//                            Log.d("Firestore", "Produs: " + title + " | Lat: " + latitude + " | Lng: " + longitude);
//
//                            // Creăm produsul temporar cu username implicit
//                            Product newProduct = new Product(title, description, expirationDate, pickupTimes, pickupInstructions,
//                                    category, latitude, longitude, imageUrl, "Loading...");
//
//                            tempProductList.add(newProduct);
//                            userIds.add(userId); // Salvăm userId-ul în aceeași ordine ca și produsele
//                        }
//
//                        // Actualizăm lista și adaptorul cu produsele, chiar înainte de a obține username-urile
//                        fullProductList.addAll(tempProductList);
//                        productAdapter.updateList(new ArrayList<>(fullProductList));
//
//                        // Acum obținem username-urile pentru fiecare produs
//                        for (int i = 0; i < tempProductList.size(); i++) {
//                            final int productIndex = i;
//                            String userId = userIds.get(i);
//
//                            if (userId != null && !userId.isEmpty()) {
//                                db.collection("users").document(userId)
//                                        .get()
//                                        .addOnSuccessListener(userDoc -> {
//                                            if (userDoc.exists() && productIndex < fullProductList.size()) {
//                                                String username = userDoc.getString("username") != null ?
//                                                        userDoc.getString("username") : "Unknown User";
//
//                                                // Actualizăm username-ul produsului în lista noastră
//                                                Product updatedProduct = fullProductList.get(productIndex);
//                                                // Creăm un nou obiect Product cu username-ul actualizat
//                                                Product productWithUsername = new Product(
//                                                        updatedProduct.getTitle(),
//                                                        updatedProduct.getDescription(),
//                                                        updatedProduct.getExpirationDate(),
//                                                        updatedProduct.getPickupTimes(),
//                                                        updatedProduct.getPickupInstructions(),
//                                                        updatedProduct.getCategory(),
//                                                        updatedProduct.getLatitude(),
//                                                        updatedProduct.getLongitude(),
//                                                        updatedProduct.getImageUrl(),
//                                                        username
//                                                );
//
//                                                // Înlocuim produsul în listă
//                                                fullProductList.set(productIndex, productWithUsername);
//
//                                                // Actualizăm adaptorul după fiecare actualizare de username
//                                                productAdapter.updateList(new ArrayList<>(fullProductList));
//                                            }
//                                        })
//                                        .addOnFailureListener(e -> Log.e("Firestore", "Eroare la obținerea utilizatorului: " + e.getMessage()));
//                            }
//                        }
//
//                    } else {
//                        Log.e("Firestore", "Eroare la încărcarea produselor: ", task.getException());
//                    }
//                });
//    }



    private void loadProductsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Inițializăm/resetăm lista de produse
        fullProductList = new ArrayList<>();

        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Product> tempProductList = new ArrayList<>();
                        List<String> userIds = new ArrayList<>(); // Pentru a ține evidența userIds

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Preluăm fiecare câmp, cu fallback la valori implicite
                            String title = document.getString("title") != null ? document.getString("title") : "No title";
                            String description = document.getString("description") != null ? document.getString("description") : "No description";
                            String expirationDate = document.getString("expirationDate") != null ? document.getString("expirationDate") : "Unknown";
                            String pickupTimes = document.getString("pickupTimes") != null ? document.getString("pickupTimes") : "Not specified";
                            String pickupInstructions = document.getString("pickupInstructions") != null ? document.getString("pickupInstructions") : "None";
                            String category = document.getString("category") != null ? document.getString("category") : "Uncategorized";
                            String imageUrl = document.getString("imageUrl") != null ? document.getString("imageUrl") : "";
                            String userId = document.getString("userId") != null ? document.getString("userId") : null;
                            String productId = document.getId();
                            // CORECTARE: Verificăm dacă latitudinea și longitudinea sunt stocate direct sau în obiectul location
                            double latitude = 0.0;
                            double longitude = 0.0;

                            if (document.getDouble("latitude") != null) {
                                // Direct în document
                                latitude = document.getDouble("latitude");
                                longitude = document.getDouble("longitude") != null ? document.getDouble("longitude") : 0.0;
                            } else if (document.get("location") != null) {
                                // În obiectul location
                                Map<String, Object> location = (Map<String, Object>) document.get("location");
                                if (location != null) {
                                    if (location.get("latitude") instanceof Double) {
                                        latitude = (Double) location.get("latitude");
                                    }
                                    if (location.get("longitude") instanceof Double) {
                                        longitude = (Double) location.get("longitude");
                                    }
                                }
                            }

                            // Debugging pentru locație
                            Log.d("Firestore", "Produs: " + title + " | Lat: " + latitude + " | Lng: " + longitude);
                            // Debugging pentru categorie
                            Log.d("Firestore", "Produs: " + title + " | Categorie: '" + category + "'");

                            // Creăm produsul temporar cu username implicit
                            Product newProduct = new Product(productId,title, description, expirationDate, pickupTimes, pickupInstructions,
                                    category, latitude, longitude, imageUrl, "Loading...");

                            tempProductList.add(newProduct);
                            userIds.add(userId); // Salvăm userId-ul în aceeași ordine ca și produsele
                        }

                        // Adăugăm produsele în lista completă
                        fullProductList.addAll(tempProductList);

                        // Nu actualizăm adaptorul imediat - vom aștepta până obținem toate username-urile

                        // Folosim un contor atomic pentru a ști când am terminat toate cererile de username
                        AtomicInteger completedRequests = new AtomicInteger(0);
                        final int totalRequests = userIds.size();

                        if (totalRequests == 0) {
                            // Dacă nu avem produse, actualizăm adaptorul imediat
                            productAdapter.updateList(new ArrayList<>(fullProductList));
                            return;
                        }

                        // Acum obținem username-urile pentru fiecare produs
                        for (int i = 0; i < tempProductList.size(); i++) {
                            final int productIndex = i;
                            String userId = userIds.get(i);

                            if (userId != null && !userId.isEmpty()) {
                                db.collection("users").document(userId)
                                        .get()
                                        .addOnSuccessListener(userDoc -> {
                                            if (userDoc.exists() && productIndex < fullProductList.size()) {
                                                String username = userDoc.getString("username") != null ?
                                                        userDoc.getString("username") : "Unknown User";

                                                // Actualizăm username-ul produsului în lista noastră
                                                Product updatedProduct = fullProductList.get(productIndex);
                                                // Creăm un nou obiect Product cu username-ul actualizat
                                                Product productWithUsername = new Product(
                                                        updatedProduct.getId(),
                                                        updatedProduct.getTitle(),
                                                        updatedProduct.getDescription(),
                                                        updatedProduct.getExpirationDate(),
                                                        updatedProduct.getPickupTimes(),
                                                        updatedProduct.getPickupInstructions(),
                                                        updatedProduct.getCategory(),
                                                        updatedProduct.getLatitude(),
                                                        updatedProduct.getLongitude(),
                                                        updatedProduct.getImageUrl(),
                                                        username
                                                );

                                                // Înlocuim produsul în listă
                                                fullProductList.set(productIndex, productWithUsername);

                                                // Verificăm dacă toate cererile de username au fost completate
                                                if (completedRequests.incrementAndGet() == totalRequests) {
                                                    // Doar după ce avem toate username-urile, actualizăm adaptorul
                                                    Log.d("Firestore", "Toate username-urile au fost încărcate, actualizăm UI-ul");
                                                    productAdapter.updateList(new ArrayList<>(fullProductList));
                                                }
                                            } else {
                                                // Incrementăm contorul chiar dacă documentul nu există
                                                if (completedRequests.incrementAndGet() == totalRequests) {
                                                    productAdapter.updateList(new ArrayList<>(fullProductList));
                                                }
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Eroare la obținerea utilizatorului: " + e.getMessage());
                                            // Incrementăm contorul și în caz de eroare
                                            if (completedRequests.incrementAndGet() == totalRequests) {
                                                productAdapter.updateList(new ArrayList<>(fullProductList));
                                            }
                                        });
                            } else {
                                // Dacă nu avem un userId, incrementăm contorul și continuăm
                                if (completedRequests.incrementAndGet() == totalRequests) {
                                    productAdapter.updateList(new ArrayList<>(fullProductList));
                                }
                            }
                        }
                    } else {
                        Log.e("Firestore", "Eroare la încărcarea produselor: ", task.getException());
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
        List<Product> filteredList = new ArrayList<>();
        for (Product product : fullProductList) {
            if (product.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productAdapter.updateList(filteredList);
    }
    private void requestNewLocationData() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        com.google.android.gms.location.LocationRequest locationRequest = com.google.android.gms.location.LocationRequest.create();
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // Actualizare la fiecare 5 secunde
        locationRequest.setFastestInterval(2000);
        locationRequest.setNumUpdates(1);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    userLatitude = locationResult.getLastLocation().getLatitude();
                    userLongitude = locationResult.getLastLocation().getLongitude();
                    Log.d("HomeFragment", "📍 Locație nouă obținută: Lat = " + userLatitude + ", Lng = " + userLongitude);

                    // Actualizăm adapterul cu locația corectă
                    productAdapter = new ProductAdapter(fullProductList, userLatitude, userLongitude);
                    recyclerView.setAdapter(productAdapter);
                    productAdapter.notifyDataSetChanged();
                }
            }
        }, null);
    }


    private void getUserLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLatitude = location.getLatitude();
                        userLongitude = location.getLongitude();
                        Log.d("HomeFragment", "✅ Locație utilizator: Lat = " + userLatitude + ", Lng = " + userLongitude);

                        // IMPORTANT: Creăm un nou adapter cu locația corectă
                        productAdapter = new ProductAdapter(fullProductList, userLatitude, userLongitude);
                        recyclerView.setAdapter(productAdapter);

                        // Reîncărcăm lista de produse după ce avem locația
                        loadProductsFromFirestore();
                    } else {
                        Log.e("HomeFragment", "⚠️ GPS dezactivat sau locație indisponibilă!");
                        Toast.makeText(getContext(), "GPS dezactivat sau locație indisponibilă", Toast.LENGTH_SHORT).show();

                        // Încercăm să obținem locația în mod activ (în unele dispozitive getLastLocation poate returna null)
                        requestNewLocationData();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "⚠️ Eroare la obținerea locației!", e);
                    Toast.makeText(getContext(), "Eroare la obținerea locației: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
