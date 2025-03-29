package com.example.aplicatielicenta.services;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            Log.d("FCM", "📬 Notificare primită: " + remoteMessage.getNotification().getBody());
        }
    }


    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        updateTokenToFirestore(token);
    }

    private void updateTokenToFirestore(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> tokenMap = new HashMap<>();
            tokenMap.put("fcmToken", token);

            db.collection("users").document(user.getUid())
                    .update(tokenMap)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM Token updated successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update FCM Token", e));
        }
    }
}
