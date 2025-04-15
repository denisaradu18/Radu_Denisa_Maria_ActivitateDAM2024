package com.example.aplicatielicenta.main;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.aplicatielicenta.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ToolbarManager {

    private final Context context;
    private final View toolbarView;
    private final TextView greetingText;
    private final TextView locationText;
    private final ImageView notificationBell;

    public ToolbarManager(Context context, View toolbarView) {
        this.context = context;
        this.toolbarView = toolbarView;

        greetingText = toolbarView.findViewById(R.id.tv_greeting);
        locationText = toolbarView.findViewById(R.id.tv_location);
        notificationBell = toolbarView.findViewById(R.id.iv_notification);
    }

    public void initGreetingAndLocation(Fragment fragment) {
        if (greetingText != null && locationText != null) {
            loadUsername();
            fetchUserLocation(fragment);
        }
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
        String greeting = (hour < 12) ? "Good morning" :
                (hour < 18) ? "Good afternoon" : "Good evening";
        greetingText.setText(greeting + ", " + username);
    }

    private void fetchUserLocation(Fragment fragment) {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(fragment.requireActivity());

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationText.setText("Location not granted");
            return;
        }

        client.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (!addresses.isEmpty()) {
                        String loc = addresses.get(0).getSubLocality() + ", " + addresses.get(0).getLocality();
                        locationText.setText(loc);
                    }
                } catch (IOException e) {
                    locationText.setText("Location error");
                }
            }
        });
    }

    public void hideGreeting() {
        if (greetingText != null)
            greetingText.setVisibility(View.GONE);
    }

    public void hideLocation() {
        if (locationText != null)
            locationText.setVisibility(View.GONE);
    }

    public void setNotificationClick(View.OnClickListener listener) {
        if (notificationBell != null) {
            notificationBell.setOnClickListener(listener);
        }
    }
}
