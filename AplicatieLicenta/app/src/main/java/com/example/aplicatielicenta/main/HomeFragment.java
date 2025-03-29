package com.example.aplicatielicenta.main;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicatielicenta.R;
import com.example.aplicatielicenta.adapters.ProductAdapter;
import com.example.aplicatielicenta.models.Product;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvLocation;
    private EditText searchBar;
    private Chip btnAll, btnFood, btnNonFood;
    private RecyclerView rvFood, rvNonFood;

    private ProductAdapter foodAdapter, nonFoodAdapter;
    private List<Product> fullProductList = new ArrayList<>();
    private List<Product> foodList = new ArrayList<>();
    private List<Product> nonFoodList = new ArrayList<>();

    private double userLatitude = 0.0;
    private double userLongitude = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvLocation = view.findViewById(R.id.tv_location);
        searchBar = view.findViewById(R.id.search_bar);
        btnAll = view.findViewById(R.id.btn_all);
        btnFood = view.findViewById(R.id.btn_food);
        btnNonFood = view.findViewById(R.id.btn_non_food);
        rvFood = view.findViewById(R.id.rv_food);
        rvNonFood = view.findViewById(R.id.rv_non_food);

        rvFood.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvNonFood.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        foodAdapter = new ProductAdapter(new ArrayList<>(), userLatitude, userLongitude);
        nonFoodAdapter = new ProductAdapter(new ArrayList<>(), userLatitude, userLongitude);

        rvFood.setAdapter(foodAdapter);
        rvNonFood.setAdapter(nonFoodAdapter);

        requestLocationPermission();
        loadUsername();
        loadProducts();

        setupSearch();
        setupChips();
        Bundle args = getArguments();
        if (args != null) {
        }


        return view;
    }

    private void loadUsername() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    String username = doc.getString("username");
                    updateGreeting(username != null ? username : "User");
                });
    }

    private void updateGreeting(String username) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = (hour < 12) ? "Good morning" : (hour < 18) ? "Good afternoon" : "Good evening";
        tvGreeting.setText(greeting + ", " + username);
    }

    private void updateLocationUI(String locationName) {
        tvLocation.setText(locationName);
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSearch(s.toString());
            }
        });
    }

    private void setupChips() {
        btnAll.setOnClickListener(v -> {
            foodAdapter.updateList(foodList);
            nonFoodAdapter.updateList(nonFoodList);
        });

        btnFood.setOnClickListener(v -> {
            foodAdapter.updateList(foodList);
            nonFoodAdapter.updateList(new ArrayList<>());
        });

        btnNonFood.setOnClickListener(v -> {
            nonFoodAdapter.updateList(nonFoodList);
            foodAdapter.updateList(new ArrayList<>());
        });
    }

    private void filterSearch(String query) {
        List<Product> filteredFood = new ArrayList<>();
        List<Product> filteredNonFood = new ArrayList<>();

        for (Product p : foodList) {
            if (p.getTitle().toLowerCase().contains(query.toLowerCase()))
                filteredFood.add(p);
        }

        for (Product p : nonFoodList) {
            if (p.getTitle().toLowerCase().contains(query.toLowerCase()))
                filteredNonFood.add(p);
        }

        foodAdapter.updateList(filteredFood);
        nonFoodAdapter.updateList(filteredNonFood);
    }

    private void loadProducts() {
        FirebaseFirestore.getInstance().collection("products")
                .get()
                .addOnSuccessListener(snapshot -> {
                    foodList.clear();
                    nonFoodList.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Product p = doc.toObject(Product.class);
                        Log.d("loadProducts", "Produs găsit: " + p.getTitle() + " cu categoria: '" + p.getCategory() + "'");

                        if (p.getCategory() != null) {
                            String categoryLower = p.getCategory().trim().toLowerCase();

                            if (categoryLower.equals("food")) {
                                foodList.add(p);
                            } else if (categoryLower.equals("nonfood") ||
                                    categoryLower.equals("non-food")) {
                                nonFoodList.add(p);
                            }
                        }
                    }

                    // Always check if the fragment is still attached to an activity
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            foodAdapter.updateList(foodList);
                            nonFoodAdapter.updateList(nonFoodList);

                            Log.d("loadProducts", "✅ Food: " + foodList.size() + " | Non-Food: " + nonFoodList.size());
                        });
                    } else {
                        // Handle the case when the fragment is no longer attached
                        Log.w("loadProducts", "Fragment not attached to Activity when data loaded");

                        // Update lists without using UI thread
                        foodAdapter.updateList(foodList);
                        nonFoodAdapter.updateList(nonFoodList);
                        Log.d("loadProducts", "✅ Food: " + foodList.size() + " | Non-Food: " + nonFoodList.size());
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "❌ Eroare la încărcare produse", e));
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        } else {
            fetchUserLocation();
        }
    }

    private void fetchUserLocation() {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tvLocation.setText("Permission not granted");
            return;
        }

        client.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLatitude = location.getLatitude();
                userLongitude = location.getLongitude();

                // 🟢 AICI ACTUALIZĂM ADAPTERELE CU LOCAȚIA
                foodAdapter.setUserLocation(userLatitude, userLongitude);
                nonFoodAdapter.setUserLocation(userLatitude, userLongitude);

                if (isAdded()) {
                    Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(userLatitude, userLongitude, 1);
                        if (!addresses.isEmpty()) {
                            updateLocationUI(addresses.get(0).getSubLocality() + ", " + addresses.get(0).getLocality());
                        }
                    } catch (IOException e) {
                        tvLocation.setText("Location not found");
                    }
                }
            }
        });
    }

}
