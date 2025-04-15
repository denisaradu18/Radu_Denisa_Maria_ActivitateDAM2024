package com.example.aplicatielicenta.notification;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicatielicenta.R;
import com.example.aplicatielicenta.notification.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private List<NotificationModel> notificationList;
    private ImageView backBtn;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.recycler_notifications);
        backBtn = findViewById(R.id.back_btn);
        emptyText = findViewById(R.id.tv_empty_notifications);

        notificationList = new ArrayList<>();
        adapter = new NotificationsAdapter(this, notificationList); // ✅ adaugă contextul

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        backBtn.setOnClickListener(v -> finish());

        loadNotificationsFromFirestore();
    }

    private void loadNotificationsFromFirestore() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.d("NotificationActivity", "🔍 Loading notifications for user: " + currentUserId);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Log.d("NotificationActivity", "📥 Fetched " + snapshot.size() + " notifications");
                    notificationList.clear();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        NotificationModel notif = doc.toObject(NotificationModel.class);
                        if (notif != null) {
                            notif.setNotificationId(doc.getId());
                            notif.setToUserId(currentUserId);
                            notificationList.add(notif);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    emptyText.setVisibility(notificationList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading notifications", Toast.LENGTH_SHORT).show();
                    Log.e("NotificationActivity", "❌ Error: " + e.getMessage());
                });
    }
}
