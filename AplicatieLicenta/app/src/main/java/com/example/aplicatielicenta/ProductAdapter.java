package com.example.aplicatielicenta;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private Set<String> favoriteProducts = new HashSet<>();
    private double userLatitude, userLongitude;
    private List<Product> originalProductList = new ArrayList<>();


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
        // 🔍 Debugging: Afișează în Logcat dacă username-ul este setat corect
        Log.d("ProductAdapter", "📌 Produs: " + product.getTitle() + " | Username: " + product.getUsername());

        if (product.getUsername() == null || product.getUsername().isEmpty()) {
            Log.e("ProductAdapter", "⚠️ Username indisponibil pentru " + product.getTitle());
            holder.usernameTextView.setText("Posted by: Unknown");
        } else {
            holder.usernameTextView.setText("Posted by: " + product.getUsername());
        }
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

        // FIX: Încărcăm imaginea produsului din lista de imagini, luând prima imagine dacă există
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            // Luăm prima imagine din lista de URL-uri
            String firstImageUrl = product.getImageUrls().get(0);
            Log.d("ProductAdapter", "Loading image: " + firstImageUrl);

            Glide.with(holder.itemView.getContext())
                    .load(firstImageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(holder.productImageView);
        } else {
            Log.w("ProductAdapter", "No images available for product: " + product.getTitle());
            holder.productImageView.setImageResource(R.drawable.placeholder_image);
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
                            if (!querySnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    document.getReference().delete();
                                }
                                favoriteProducts.remove(product.getId());
                                holder.favoriteButton.setImageResource(R.drawable.ic_star_border);
                                Toast.makeText(holder.itemView.getContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("Favorites", "Favorite not found in Firebase!");
                            }
                        });
            } else {
                // Adăugăm produsul la favorite
                HashMap<String, Object> favorite = new HashMap<>();
                favorite.put("userId", userId);
                favorite.put("productId", product.getId());
                favorite.put("title", product.getTitle());

                favorite.put("imageUrl", product.getImageUrls() != null && !product.getImageUrls().isEmpty() ?
                        product.getImageUrls().get(0) : null);

                db.collection("favorites")
                        .add(favorite)
                        .addOnSuccessListener(documentReference -> {
                            favoriteProducts.add(product.getId());
                            holder.favoriteButton.setImageResource(R.drawable.ic_star_filled);
                            Toast.makeText(holder.itemView.getContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
            intent.putExtra("PRODUCT_ID", product.getId()); // ✅ Asigură-te că trimitem productId-ul corect!

            // Trimitem și celelalte informații despre produs
            intent.putExtra("title", product.getTitle());
            intent.putExtra("description", product.getDescription());

            if (product.getImageUrls() != null) {
                intent.putStringArrayListExtra("imageUrls", new ArrayList<>(product.getImageUrls()));
            } else {
                intent.putStringArrayListExtra("imageUrls", new ArrayList<>());
            }

            intent.putExtra("pickupTimes", product.getPickupTimes());
            intent.putExtra("latitude", product.getLatitude());
            intent.putExtra("longitude", product.getLongitude());
            intent.putExtra("username", product.getUsername());
            intent.putExtra("dateAdded", "1 day ago");

            v.getContext().startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<Product> newList) {
        if (newList == null) {
            Log.e("ProductAdapter", "❌ Lista de produse este null!");
            return;
        }

        if (originalProductList.isEmpty()) {
            originalProductList.addAll(productList); // Salvează lista inițială
        }

        productList.clear();
        productList.addAll(newList);
        notifyDataSetChanged();
    }

    public void resetList() {
        productList.clear();
        productList.addAll(originalProductList);
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