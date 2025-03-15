package com.example.aplicatielicenta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.aplicatielicenta.databinding.ActivityMainBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
                replaceFragment(new HomeFragment(productList));
                return true;
            } else if (itemId == R.id.map) {
                replaceFragment(new MapFragment());
                return true;
            } else if (itemId == R.id.inbox) {
                replaceFragment(new InboxFragment());
                return true;
            } else if (itemId == R.id.account) {
                replaceFragment(new AccountFragment());
                return true;
            }
            return false;
        });

        binding.fab.setOnClickListener(v -> {
            // Construiește un dialog cu două opțiuni
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getString(R.string.choose_product_type)); // Titlu luat din strings.xml
            builder.setItems(new CharSequence[]{
                    getString(R.string.food_option),
                    getString(R.string.non_food_option)
            }, (dialog, which) -> {
                if (which == 0) {
                    // Utilizatorul a ales "Food"
                    Intent intent = new Intent(MainActivity.this, AddFoodActivity.class);
                    startActivity(intent);
                } else {
                    // Utilizatorul a ales "Non-Food"
                    Intent intent = new Intent(MainActivity.this, AddNonFoodActivity.class);
                    startActivity(intent);
                }
            });
            builder.show();
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

                        // Actualizăm UI-ul după ce am încărcat datele
                        runOnUiThread(() -> {
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.frame_layout, new HomeFragment(productList));
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
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}
