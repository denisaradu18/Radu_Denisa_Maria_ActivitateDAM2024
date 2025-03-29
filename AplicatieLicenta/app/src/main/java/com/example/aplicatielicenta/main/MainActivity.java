package com.example.aplicatielicenta.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.aplicatielicenta.product.AddFoodActivity;
import com.example.aplicatielicenta.product.AddNonFoodActivity;
import com.example.aplicatielicenta.R;
import com.example.aplicatielicenta.databinding.ActivityMainBinding;
import com.example.aplicatielicenta.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private List<Product> productList = new ArrayList<>(); // Lista cu produse din Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Încarcă produsele din Firestore în background
        new Thread(this::loadProductsFromFirestore).start();

        // Inițializăm primul fragment (HomeFragment)
        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setBackground(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                HomeFragment homeFragment = new HomeFragment();
                Bundle bundle = new Bundle();
                homeFragment.setArguments(bundle);
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                fragmentTransaction.replace(R.id.container, homeFragment);
                fragmentTransaction.commit();
                return true;
            } else if (itemId == R.id.map) {
                replaceFragment(new MapFragment());
                return true;
            } else if (itemId == R.id.inbox) {
                replaceFragment(new FragmentInbox());
                return true;
            } else if (itemId == R.id.account) {
                replaceFragment(new AccountFragment());
                return true;
            }
            return false;
        });

        binding.fab.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getString(R.string.choose_product_type));
            builder.setItems(new CharSequence[]{
                    getString(R.string.food_option),
                    getString(R.string.non_food_option)
            }, (dialog, which) -> {
                if (which == 0) {
                    Intent intent = new Intent(MainActivity.this, AddFoodActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, AddNonFoodActivity.class);
                    startActivity(intent);
                }
            });
            builder.show();
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new MapFragment())
                .commit();




        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d("FCM", "FCM Token: " + token);

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(user.getUid())
                                .update("fcmToken", token)
                                .addOnSuccessListener(aVoid -> Log.d("FCM", "Token saved successfully"))
                                .addOnFailureListener(e -> Log.e("FCM", "Failed to save token", e));
                    }
                });

    }

    private void loadProductsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear(); // Curățăm lista înainte de a adăuga produse noi
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            productList.add(product);
                        }

                        runOnUiThread(() -> {
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            HomeFragment homeFragment = new HomeFragment();
                            Bundle bundle = new Bundle();
                            homeFragment.setArguments(bundle);
                            fragmentTransaction.replace(R.id.container, homeFragment);

                            fragmentTransaction.commit();
                        });

                    } else {
                        Log.e("Firestore", "Error loading products", task.getException());
                    }
                });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }


}
