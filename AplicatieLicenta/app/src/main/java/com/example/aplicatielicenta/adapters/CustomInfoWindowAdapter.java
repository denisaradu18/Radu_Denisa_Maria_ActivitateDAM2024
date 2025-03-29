package com.example.aplicatielicenta.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.aplicatielicenta.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Map;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View window;
    private final Context context;
    private final Map<String, String> markerImageMap;

    public CustomInfoWindowAdapter(Context context, Map<String, String> markerImageMap) {
        this.context = context;
        this.markerImageMap = markerImageMap;
        this.window = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
    }

    private void renderWindowText(Marker marker, View view) {
        TextView title = view.findViewById(R.id.info_title);
        ImageView image = view.findViewById(R.id.info_image);

        title.setText(marker.getTitle());

        String imageUrl = markerImageMap.get(marker.getId());
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context).load(imageUrl).into(image);
        } else {
            image.setImageResource(R.drawable.placeholder_image);
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        renderWindowText(marker, window);
        return window;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}

