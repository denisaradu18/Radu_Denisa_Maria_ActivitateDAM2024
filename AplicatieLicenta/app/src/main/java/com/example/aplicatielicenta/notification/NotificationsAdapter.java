package com.example.aplicatielicenta.notification;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicatielicenta.R;
import com.example.aplicatielicenta.notification.NotificationModel;
import com.example.aplicatielicenta.transaction.TransactionChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private final List<NotificationModel> list;
    private final Context context;

    public NotificationsAdapter(Context context, List<NotificationModel> list) {
        this.context = context;
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, body;
        Button acceptBtn, declineBtn;
        LinearLayout buttonsLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_notification_title);
            body = itemView.findViewById(R.id.tv_notification_body);
            acceptBtn = itemView.findViewById(R.id.btn_accept);
            declineBtn = itemView.findViewById(R.id.btn_decline);
            buttonsLayout = itemView.findViewById(R.id.layout_buttons);
        }
    }


    @NonNull
    @Override
    public NotificationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsAdapter.ViewHolder holder, int position) {
        NotificationModel item = list.get(position);
        holder.title.setText(item.getTitle());
        holder.body.setText("Pickup time: " + item.getPickupTime() + "\nMessage: " + item.getMessage());

        FirebaseFirestore.getInstance().collection("users")
                .document(item.getFromUserId())
                .get()
                .addOnSuccessListener(doc -> {
                    String senderUsername = doc.getString("username");
                    if (senderUsername != null) {
                        holder.title.setText(item.getTitle() + " from @" + senderUsername);
                    }
                });

        if ("request".equals(item.getType())) {
            holder.buttonsLayout.setVisibility(View.VISIBLE);

            holder.acceptBtn.setOnClickListener(v -> {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("transactions")
                        .document(item.getTransactionId())
                        .update("status", "accepted")
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(context, "Request accepted!", Toast.LENGTH_SHORT).show();


                            deleteNotification(item.getToUserId(), item.getNotificationId(), position);


                            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            Intent intent = new Intent(context, TransactionChatActivity.class);
                            intent.putExtra("transactionId", item.getTransactionId());
                            intent.putExtra("receiverId", item.getFromUserId());
                            context.startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Error accepting request", Toast.LENGTH_SHORT).show();
                        });
            });

            holder.declineBtn.setOnClickListener(v -> {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("transactions")
                        .document(item.getTransactionId())
                        .update("status", "declined")
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(context, "Request declined!", Toast.LENGTH_SHORT).show();

                            // Delete the notification
                            deleteNotification(item.getToUserId(), item.getNotificationId(), position);

                            Map<String, Object> declineNotification = new HashMap<>();
                            declineNotification.put("title", "Request Declined");
                            declineNotification.put("message", "Your request has been declined by the seller");
                            declineNotification.put("timestamp", System.currentTimeMillis());
                            declineNotification.put("type", "info");
                            declineNotification.put("fromUserId", item.getToUserId());

                            db.collection("users")
                                    .document(item.getFromUserId())
                                    .collection("notifications")
                                    .add(declineNotification);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Error declining request", Toast.LENGTH_SHORT).show();
                        });
            });
        } else {
            holder.buttonsLayout.setVisibility(View.GONE);
        }
    }
    private void deleteNotification(String toUserId, String notificationId, int position) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(toUserId)
                .collection("notifications")
                .document(notificationId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    list.remove(position);
                    notifyItemRemoved(position);
                    Log.d("Notification", "🔔 Notification deleted: " + notificationId);
                })
                .addOnFailureListener(e -> Log.e("Notification", "❌ Failed to delete", e));
    }
    private void updateTransactionStatus(String transactionId, String newStatus) {
        FirebaseFirestore.getInstance()
                .collection("transactions")
                .document(transactionId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "✅ Status updated to: " + newStatus, Toast.LENGTH_SHORT).show();
                    Log.d("Transaction", "Status set to " + newStatus);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "❌ Failed to update status", Toast.LENGTH_SHORT).show();
                    Log.e("Transaction", "Error updating status", e);
                });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }
}
