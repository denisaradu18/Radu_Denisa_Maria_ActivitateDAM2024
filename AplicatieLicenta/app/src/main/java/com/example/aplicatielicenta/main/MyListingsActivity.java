package com.example.aplicatielicenta.main;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicatielicenta.R;
import com.example.aplicatielicenta.adapters.ProductAdapter;
import com.example.aplicatielicenta.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyListingsActivity extends AppCompatActivity {

    private RecyclerView rvActiveListings, rvInactiveListings;
    private ProductAdapter activeAdapter, inactiveAdapter;
    private List<Product> activeList = new ArrayList<>();
    private List<Product> inactiveList = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;
    private TextView tvEmptyMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listings); // folosește noul XML

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Listings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rvActiveListings = findViewById(R.id.rv_active_listings);
        rvInactiveListings = findViewById(R.id.rv_inactive_listings);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);

        activeAdapter = new ProductAdapter(activeList, 0, 0);
        inactiveAdapter = new ProductAdapter(inactiveList, 0, 0, true); // true => stil diferit (vezi mai jos)

        rvActiveListings.setLayoutManager(new LinearLayoutManager(this));
        rvInactiveListings.setLayoutManager(new LinearLayoutManager(this));
        rvActiveListings.setAdapter(activeAdapter);
        rvInactiveListings.setAdapter(inactiveAdapter);

        loadUserListings();
    }

    private void loadUserListings() {
        db.collection("products")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    activeList.clear();
                    inactiveList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        Product product = doc.toObject(Product.class);
                        if (product.isAvailable()) {
                            activeList.add(product);
                        } else {
                            inactiveList.add(product);
                        }
                    }

                    activeAdapter.notifyDataSetChanged();
                    inactiveAdapter.notifyDataSetChanged();
                    checkEmptyState();
                })
                .addOnFailureListener(e -> {
                    tvEmptyMessage.setText("Error loading listings.");
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                });
    }

    private void checkEmptyState() {
        if (activeList.isEmpty() && inactiveList.isEmpty()) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
