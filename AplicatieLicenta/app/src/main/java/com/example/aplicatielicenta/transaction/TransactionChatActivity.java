package com.example.aplicatielicenta.transaction;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aplicatielicenta.R;
import com.example.aplicatielicenta.adapters.ChatAdapter;
import com.example.aplicatielicenta.models.MessageModel;
import com.example.aplicatielicenta.models.Product;
import com.example.aplicatielicenta.models.TransactionModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionChatActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private TextView productNameTextView, otherUserNameTextView;
    private ImageView productImageView;
    private ChatAdapter chatAdapter;
    private List<MessageModel> messageList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String senderId, transactionId, receiverId, currentUserId;
    private TransactionModel transaction;
    private Product product;
    private String otherUserName;
    private Button completeTransactionButton;
    private boolean isUserSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_chat);

        // Inițializare Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // Obține date din Intent
        transactionId = getIntent().getStringExtra("transactionId");
        receiverId = getIntent().getStringExtra("receiverId");

        // Debugging log
        Log.d("TransactionChatActivity", "Received transactionId: " + transactionId);
        Log.d("TransactionChatActivity", "Received receiverId: " + receiverId);

        if (transactionId == null) {
            Toast.makeText(this, "Error: Missing transaction information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        senderId = currentUserId;

        // Inițializare UI
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        productNameTextView = findViewById(R.id.productNameTextView);
        otherUserNameTextView = findViewById(R.id.otherUserNameTextView);
        productImageView = findViewById(R.id.productImageView);
        completeTransactionButton = findViewById(R.id.completeTransactionButton);


        setupRecyclerView();

        // Încarcă datele tranzacției
        loadTransactionData();
        completeTransactionButton.setOnClickListener(v -> completeTransaction());


        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
            } else {
                Toast.makeText(this, "Nu poți trimite un mesaj gol!", Toast.LENGTH_SHORT).show();
            }
        });
        markMessagesAsRead(transactionId);

    }


    private void markMessagesAsRead(String transactionId) {
        db.collection("transactions")
                .document(transactionId)
                .collection("messages")
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    for (DocumentSnapshot doc : querySnapshots) {
                        doc.getReference().update("read", true);
                    }
                });
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        chatRecyclerView.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter(this, messageList, currentUserId);
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void loadTransactionData() {
        db.collection("transactions").document(transactionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        transaction = documentSnapshot.toObject(TransactionModel.class);

                        if (transaction != null) {
                            isUserSeller = currentUserId.equals(transaction.getSellerId());

                            // Show completion button only for the seller and only if status is "accepted"
                            String status = transaction.getStatus();
                            if (isUserSeller && "accepted".equals(status)) {
                                completeTransactionButton.setVisibility(View.VISIBLE);
                            } else {
                                completeTransactionButton.setVisibility(View.GONE);
                            }

                            // Încarcă datele produsului
                            loadProductData();

                            // Încarcă datele utilizatorului
                            loadOtherUserData();

                            // Încarcă mesajele
                            loadTransactionMessages();
                        }
                    } else {
                        Toast.makeText(this, "Error: Transaction not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading transaction data", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadProductData() {
        db.collection("products").document(transaction.getProductId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        product = documentSnapshot.toObject(Product.class);

                        if (product != null) {
                            productNameTextView.setText(product.getTitle());

                            // Încarcă imaginea produsului
                            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                                Glide.with(this)
                                        .load(product.getImageUrls())
                                        .centerCrop()
                                        .into(productImageView);
                            } else {
                                // Setează o imagine placeholder dacă nu există imagine
                                productImageView.setImageResource(R.drawable.placeholder_image);
                            }
                        }
                    }
                });
    }

    private void loadOtherUserData() {
        db.collection("users").document(receiverId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        otherUserName = documentSnapshot.getString("name");
                        otherUserNameTextView.setText(otherUserName);
                    }
                });
    }

    private void loadTransactionMessages() {
        db.collection("transactions")
                .document(transactionId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("TransactionChatActivity", "❌ Eroare la încărcarea mesajelor!", e);
                        return;
                    }


                    messageList.clear();

                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            MessageModel message = doc.toObject(MessageModel.class);
                            if (message != null) {
                                messageList.add(message);

                                // Dacă mesajul este primit și necitit, marchează-l ca citit
                                if (message.getReceiverId().equals(currentUserId) && !message.isRead()) {
                                    doc.getReference().update("read", true);
                                }
                            }
                        }

                        // 🔄 actualizează UI
                        chatAdapter.notifyDataSetChanged();
                        chatRecyclerView.scrollToPosition(messageList.size() - 1);
                    }

                });
    }

    public void sendMessage(String messageText) {
        if (messageText.isEmpty()) {
            Log.e("TransactionChatActivity", "⚠️ Mesajul este gol, nu se poate trimite.");
            return;
        }

        if (transactionId == null || receiverId == null) {
            Log.e("TransactionChatActivity", "⚠️ transactionId sau receiverId este null!");
            return;
        }

        String messageId = db.collection("transactions")
                .document(transactionId)
                .collection("messages")
                .document()
                .getId();

        MessageModel message = new MessageModel(senderId, receiverId, messageText, System.currentTimeMillis());
        message.setRead(false); // ✅ AICI setezi că mesajul NU este citit


        db.collection("transactions")
                .document(transactionId)
                .collection("messages")
                .document(messageId)
                .set(message)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TransactionChatActivity", "✅ Mesaj trimis cu succes!");
                    messageInput.setText("");

                    updateTransactionTimestamp();

                    // 🔥 Verificăm dacă receiverId are token
                    db.collection("users").document(receiverId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists() && documentSnapshot.contains("fcmToken")) {
                                    String receiverToken = documentSnapshot.getString("fcmToken");

                                    Log.d("TransactionChatActivity", "🔔 Trimit notificare către token: " + receiverToken);

                                    Map<String, Object> notification = new HashMap<>();
                                    notification.put("token", receiverToken);
                                    notification.put("message", messageText);
                                    notification.put("senderId", senderId);
                                    notification.put("transactionId", transactionId);
                                    notification.put("timestamp", System.currentTimeMillis());
                                    notification.put("processed", false);

                                    db.collection("notifications")
                                            .add(notification)
                                            .addOnSuccessListener(docRef -> {
                                                Log.d("TransactionChatActivity", "✅ Notificare salvată în Firestore.");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("TransactionChatActivity", "❌ Eroare la salvarea notificării", e);
                                            });

                                } else {
                                    Log.e("TransactionChatActivity", "❌ Nu am găsit fcmToken pentru receiverId: " + receiverId);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("TransactionChatActivity", "❌ Eroare la obținerea token-ului", e);
                            });

                })
                .addOnFailureListener(e -> {
                    Log.e("TransactionChatActivity", "❌ Eroare la trimiterea mesajului", e);
                });
    }

    private void updateTransactionTimestamp() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessageTimestamp", System.currentTimeMillis());

        db.collection("transactions").document(transactionId)
                .update(updates)
                .addOnSuccessListener(aVoid ->
                        Log.d("TransactionChatActivity", "Timestamp actualizat cu succes"))
                .addOnFailureListener(e ->
                        Log.e("TransactionChatActivity", "Eroare la actualizarea timestamp", e));
    }

    private void sendNotificationToReceiver(String receiverId, String messageText) {
        db.collection("users").document(receiverId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("fcmToken")) {
                        String receiverToken = documentSnapshot.getString("fcmToken");
                        sendFCMNotification(receiverToken, messageText);
                    }
                });
    }

    private void completeTransaction() {
        new AlertDialog.Builder(this)
                .setTitle("Complete Transaction")
                .setMessage("Are you sure you want to mark this transaction as completed?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Update transaction status to completed
                    db.collection("transactions")
                            .document(transactionId)
                            .update("status", "completed",
                                    "completedAt", System.currentTimeMillis())
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Transaction completed!", Toast.LENGTH_SHORT).show();
                                completeTransactionButton.setVisibility(View.GONE);

                                // Send notification to buyer
                                Map<String, Object> notification = new HashMap<>();
                                notification.put("title", "Transaction Completed");
                                notification.put("message", "The seller has marked your transaction as completed");
                                notification.put("timestamp", System.currentTimeMillis());
                                notification.put("type", "info");
                                notification.put("fromUserId", currentUserId);
                                notification.put("transactionId", transactionId);

                                db.collection("users")
                                        .document(transaction.getBuyerId())
                                        .collection("notifications")
                                        .add(notification);

                                // You can add code here to update UI to reflect completed state
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error completing transaction", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void sendFCMNotification(String receiverToken, String messageText) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> notification = new HashMap<>();
        notification.put("token", receiverToken);
        notification.put("message", messageText);
        notification.put("senderId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        notification.put("transactionId", transactionId);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("processed", false);

        db.collection("notifications").add(notification)
                .addOnSuccessListener(documentReference ->
                        Log.d("TransactionChatActivity", "✅ Notificare adăugată pentru procesare"))
                .addOnFailureListener(e ->
                        Log.e("TransactionChatActivity", "❌ Eroare la salvarea notificării", e));
    }
}

