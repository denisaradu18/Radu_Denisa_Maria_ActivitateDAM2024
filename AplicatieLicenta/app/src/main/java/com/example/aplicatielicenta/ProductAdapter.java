package com.example.aplicatielicenta;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.location.Location;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private Set<String> favoriteProducts = new HashSet<>();
    private double userLatitude, userLongitude;

    // Constructor normal
    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
        this.userLatitude = 0.0;
        this.userLongitude = 0.0;
    }

    // Constructor cu coordonate
    public ProductAdapter(List<Product> productList, double userLatitude, double userLongitude) {
        this.productList = productList;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.titleTextView.setText(product.getTitle());
        holder.usernameTextView.setText("Posted by: " + product.getUsername());

        // Debugging: Afișăm în log coordonatele produsului și utilizatorului
        Log.d("ProductAdapter", "User Location: Lat = " + userLatitude + ", Lng = " + userLongitude);
        Log.d("ProductAdapter", "Product Location: Lat = " + product.getLatitude() + ", Lng = " + product.getLongitude());

        // Verificăm dacă locația este validă (și coordonatele nu sunt 0.0)
        if (userLatitude != 0.0 && userLongitude != 0.0 &&
                product.getLatitude() != 0.0 && product.getLongitude() != 0.0) {
            double distance = calculateDistance(userLatitude, userLongitude, product.getLatitude(), product.getLongitude());
            Log.d("ProductAdapter", "Calculated Distance: " + distance + " km"); // Verificăm distanța în log
            holder.distanceTextView.setText(String.format("%.1f km", distance));
        } else {
            Log.e("ProductAdapter", "Locația utilizatorului sau a produsului este invalidă!");
            Log.e("ProductAdapter", "User: " + userLatitude + "," + userLongitude +
                    " | Product: " + product.getLatitude() + "," + product.getLongitude());
            holder.distanceTextView.setText("Distance unknown");
        }

        // Încărcăm imaginea produsului dacă există URL
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_image) // Înlocuiește cu imaginea ta placeholder
                    .error(R.drawable.error_image) // Înlocuiește cu imaginea ta de eroare
                    .into(holder.productImageView);
        } else {
            // Set a default image if no URL is provided
            holder.productImageView.setImageResource(R.drawable.placeholder_image); // Înlocuiește cu imaginea ta default
        }
        holder.favoriteButton.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            if (favoriteProducts.contains(product.getId())) {
                // Eliminăm produsul din favorite
                db.collection("favorites")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("productId", product.getId())
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                document.getReference().delete();
                            }
                            favoriteProducts.remove(product.getId());
                            holder.favoriteButton.setImageResource(R.drawable.ic_star_border);
                            Toast.makeText(holder.itemView.getContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Adăugăm produsul la favorite
                HashMap<String, Object> favorite = new HashMap<>();
                favorite.put("userId", userId);
                favorite.put("productId", product.getId());
                favorite.put("title", product.getTitle());
                favorite.put("imageUrl", product.getImageUrl());

                db.collection("favorites")
                        .add(favorite)
                        .addOnSuccessListener(documentReference -> {
                            favoriteProducts.add(product.getId());
                            holder.favoriteButton.setImageResource(R.drawable.ic_star_filled);
                            Toast.makeText(holder.itemView.getContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<Product> newList) {
        if (newList == null || newList.isEmpty()) {
            Log.e("ProductAdapter", "❌ Lista de produse este goală! Nu o actualizăm.");
            return;
        }

        productList.clear();
        productList.addAll(newList);
        notifyDataSetChanged();
    }


    // ✅ Method to calculate distance in km
    private double calculateDistance(double userLat, double userLng, double productLat, double productLng) {
        float[] results = new float[1];
        Location.distanceBetween(userLat, userLng, productLat, productLng, results);
        return results[0] / 1000; // Convert meters to kilometers
    }

    // ✅ Fixed `ProductViewHolder`
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView,usernameTextView, distanceTextView;
        ImageView productImageView;
        ImageButton favoriteButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.product_title);
            usernameTextView=itemView.findViewById(R.id.product_username);
            distanceTextView = itemView.findViewById(R.id.product_distance); // ✅ This should match XML ID
            productImageView = itemView.findViewById(R.id.product_image);
            favoriteButton = itemView.findViewById(R.id.btn_favorite);
        }
    }
}
