package com.example.aplicatielicenta.main;

import android.content.Intent;
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
import com.example.aplicatielicenta.notification.NotificationActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment {


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

        View toolbar = view.findViewById(R.id.include_toolbar);
        ToolbarManager toolbarManager = new ToolbarManager(requireContext(), toolbar);
        toolbarManager.initGreetingAndLocation(this);
        toolbarManager.setNotificationClick(v -> {
            Intent i = new Intent(requireContext(), NotificationActivity.class);
            startActivity(i);
        });

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

        loadProducts();

        setupSearch();
        setupChips();
        Bundle args = getArguments();
        if (args != null) {
        }


        return view;
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
                .whereEqualTo("isAvailable", true)
                .get()
                .addOnSuccessListener(snapshot -> {
                    foodList.clear();
                    nonFoodList.clear();

                    AtomicInteger productsProcessed = new AtomicInteger(0);
                    int totalProducts = snapshot.size();

                    if (totalProducts == 0) {
                        updateAdapters();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Product p = doc.toObject(Product.class);
                        String productId = doc.getId();

                        FirebaseFirestore.getInstance().collection("transactions")
                                .whereEqualTo("productId", productId)
                                .whereIn("status", Arrays.asList("pending", "accepted","completed"))
                                .get()
                                .addOnSuccessListener(transSnap -> {
                                    if (transSnap.isEmpty()) {
                                        if (p.getCategory() != null) {
                                            String cat = p.getCategory().toLowerCase().trim();
                                            if (cat.equals("food")) {
                                                foodList.add(p);
                                            } else if (cat.equals("nonfood") || cat.equals("non-food")) {
                                                nonFoodList.add(p);
                                            }
                                        }
                                    } else {
                                        Log.d("loadProducts", "⛔ Produsul " + productId + " are tranzacții active");
                                    }

                                    if (productsProcessed.incrementAndGet() == totalProducts) {
                                        updateAdapters();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("loadProducts", "Eroare la verificarea tranzacțiilor", e);
                                    if (productsProcessed.incrementAndGet() == totalProducts) {
                                        updateAdapters();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "❌ Eroare la încărcare produse", e));
    }

    private void updateAdapters() {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                foodAdapter.updateList(foodList);
                nonFoodAdapter.updateList(nonFoodList);
                Log.d("loadProducts", "✅ UI actualizat: food=" + foodList.size() + ", nonFood=" + nonFoodList.size());
            });
        }
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

            return;
        }

        client.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLatitude = location.getLatitude();
                userLongitude = location.getLongitude();

                foodAdapter.setUserLocation(userLatitude, userLongitude);
                nonFoodAdapter.setUserLocation(userLatitude, userLongitude);

                if (isAdded()) {
                    Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

                }
            }
        });
    }

}
