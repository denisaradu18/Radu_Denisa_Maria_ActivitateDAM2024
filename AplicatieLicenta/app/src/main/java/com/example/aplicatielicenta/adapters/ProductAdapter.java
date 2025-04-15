package com.example.aplicatielicenta.adapters;

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
import com.example.aplicatielicenta.main.EditProductActivity;
import com.example.aplicatielicenta.product.ProductDetailActivity;
import com.example.aplicatielicenta.R;
import com.example.aplicatielicenta.models.Product;
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

    private String currentUserId;

    private boolean isInactiveMode;


    // Constructor pentru mod inactiv
    public ProductAdapter(List<Product> productList, double userLatitude, double userLongitude, boolean isInactiveMode) {
        this(productList, userLatitude, userLongitude);
        this.isInactiveMode = isInactiveMode;
    }

    // Constructor pentru mod normal
    public ProductAdapter(List<Product> productList, double userLatitude, double userLongitude) {
        this.productList = productList;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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

        if (product.getUserId() != null && product.getUserId().equals(currentUserId)) {
            holder.editButton.setVisibility(View.VISIBLE); // 👁️ Arată butonul doar owner-ului
            holder.editButton.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), EditProductActivity.class);
                intent.putExtra("PRODUCT_ID", product.getId());
                intent.putExtra("CATEGORY", product.getCategory());
                intent.putExtra("title", product.getTitle());
                intent.putExtra("description", product.getDescription());
                intent.putExtra("pickupTimes", product.getPickupTimes());
                intent.putExtra("pickupInstructions", product.getPickupInstructions());
                intent.putExtra("latitude", product.getLatitude());
                intent.putExtra("longitude", product.getLongitude());
                intent.putExtra("expirationDate", product.getExpirationDate()); // ✅
                intent.putExtra("quantity", product.getQuantity());             // ✅

                String listForDaysStr = product.getListForDays();
                int listForDaysInt = 0;
                try {
                    if (listForDaysStr != null && !listForDaysStr.isEmpty()) {
                        listForDaysInt = Integer.parseInt(listForDaysStr);
                    }
                } catch (NumberFormatException e) {
                    Log.e("ProductAdapter", "Error parsing listForDays: " + listForDaysStr, e);
                }
                intent.putExtra("listForDays", listForDaysInt);

                if (product.getImageUrls() != null) {
                    intent.putStringArrayListExtra("imageUrls", new ArrayList<>(product.getImageUrls()));
                }

                v.getContext().startActivity(intent);
            });
        } else {
            holder.editButton.setVisibility(View.GONE);
        }


        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {

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
            intent.putExtra("PRODUCT_ID", product.getId());

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
        if (isInactiveMode) {
            // Stil vizual: opacitate redusă și text roșu
            holder.itemView.setAlpha(0.5f);
            holder.favoriteButton.setVisibility(View.GONE); // ascunde favorite
            holder.editButton.setVisibility(View.GONE); // sau lasă-l dacă vrei să editeze

            // Adaugă badge sau text
            holder.usernameTextView.setText("Inactiv / Vândut");
            holder.distanceTextView.setText("Indisponibil");
        }


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

        originalProductList.clear();
        originalProductList.addAll(newList);

        productList.clear();
        productList.addAll(newList);
        notifyDataSetChanged();

        Log.d("ProductAdapter", "✅ Adapter updated with " + newList.size() + " items");
    }

    public void setUserLocation(double lat, double lng) {
        this.userLatitude = lat;
        this.userLongitude = lng;
        notifyDataSetChanged();
    }


    public void resetList() {
        productList.clear();
        productList.addAll(originalProductList);
        notifyDataSetChanged();
    }


    private double calculateDistance(double userLat, double userLng, double productLat, double productLng) {
        float[] results = new float[1];
        Location.distanceBetween(userLat, userLng, productLat, productLng, results);
        return results[0] / 1000;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView,usernameTextView, distanceTextView;
        ImageView productImageView;
        ImageButton favoriteButton;
        ImageButton editButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.product_title);
            usernameTextView=itemView.findViewById(R.id.product_username);
            distanceTextView = itemView.findViewById(R.id.product_distance);
            productImageView = itemView.findViewById(R.id.product_image);
            favoriteButton = itemView.findViewById(R.id.btn_favorite);
            editButton = itemView.findViewById(R.id.btn_edit);
        }
    }
}