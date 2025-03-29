package com.example.aplicatielicenta.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.aplicatielicenta.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class ProductBottomSheet extends BottomSheetDialogFragment {

    private String title, description, productId;
    private List<String> imageUrls;

    public ProductBottomSheet(String productId, String title, String description, List<String> imageUrls) {
        this.productId = productId;
        this.title = title;
        this.description = description;
        this.imageUrls = imageUrls;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_product, container, false);

        ImageView image = view.findViewById(R.id.bottom_image);
        TextView titleText = view.findViewById(R.id.bottom_title);
        TextView descText = view.findViewById(R.id.bottom_description);
        Button button = view.findViewById(R.id.bottom_button);

        titleText.setText(title);
        descText.setText(description);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            Glide.with(requireContext()).load(imageUrls.get(0)).into(image);
        } else {
            image.setImageResource(R.drawable.placeholder_image);
        }

        button.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("PRODUCT_ID", productId);
            startActivity(intent);
            dismiss();
        });

        return view;
    }
}
