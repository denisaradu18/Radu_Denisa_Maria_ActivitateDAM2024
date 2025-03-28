package com.example.aplicatielicenta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionsActivity extends AppCompatActivity {
    private RecyclerView transactionsRecyclerView;
    private TextView emptyStateTextView;
    private TransactionsAdapter adapter;
    private List<TransactionWithDetails> transactionsList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        // Inițializare Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // Inițializare UI
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);

        // Configurare RecyclerView
        transactionsList = new ArrayList<>();
        adapter = new TransactionsAdapter(this, transactionsList, new TransactionsAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(TransactionWithDetails transaction) {
                openTransactionChat(transaction);
            }
        });

        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionsRecyclerView.setAdapter(adapter);

        // Încarcă tranzacțiile
        loadTransactions();
    }

    private void loadTransactions() {
        db.collection("transactions")
                .whereArrayContains("participants", currentUserId) // 🔥 caută tranzacțiile unde userul e implicat
                .addSnapshotListener((querySnapshots, e) -> {
                    if (e != null) {
                        Log.e("TransactionsActivity", "Eroare la ascultarea tranzacțiilor", e);
                        return;
                    }

                    transactionsList.clear(); // 🧹 curățăm lista

                    if (querySnapshots != null) {
                        for (DocumentSnapshot doc : querySnapshots) {
                            TransactionModel transaction = doc.toObject(TransactionModel.class);
                            if (transaction != null) {
                                boolean isUserBuyer = currentUserId.equals(transaction.getBuyerId());
                                loadTransactionDetails(transaction, isUserBuyer);
                            }
                        }
                    }

                    if (transactionsList.isEmpty()) {
                        emptyStateTextView.setVisibility(View.VISIBLE);
                        transactionsRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyStateTextView.setVisibility(View.GONE);
                        transactionsRecyclerView.setVisibility(View.VISIBLE);
                    }
                });
    }

    // Adauga in metoda loadTransactionDetails din TransactionsActivity
    private void loadTransactionDetails(TransactionModel transaction, boolean isUserBuyer) {
        db.collection("products").document(transaction.getProductId())
                .get()
                .addOnSuccessListener(productDoc -> {
                    if (productDoc.exists()) {
                        Product product = productDoc.toObject(Product.class);
                        String otherUserId = isUserBuyer ? transaction.getSellerId() : transaction.getBuyerId();

                        db.collection("users").document(otherUserId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String otherUserName = userDoc.getString("name");

                                        db.collection("transactions")
                                                .document(transaction.getTransactionId())
                                                .collection("messages")
                                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                                .get()
                                                .addOnSuccessListener(messageQueryDocumentSnapshots -> {
                                                    String lastMessage = "";
                                                    long lastMessageTimestamp = transaction.getTimestamp();

                                                    AtomicInteger unreadCount = new AtomicInteger(0);

                                                    if (!messageQueryDocumentSnapshots.isEmpty()) {
                                                        for (DocumentSnapshot messageDoc : messageQueryDocumentSnapshots) {
                                                            MessageModel message = messageDoc.toObject(MessageModel.class);

                                                            if (message != null) {
                                                                if (lastMessage.isEmpty()) {
                                                                    lastMessage = message.getMessage();
                                                                    lastMessageTimestamp = message.getTimestamp();
                                                                }

                                                                if (!message.getSenderId().equals(currentUserId)
                                                                        && !Boolean.TRUE.equals(messageDoc.getBoolean("read"))) {
                                                                    unreadCount.incrementAndGet();
                                                                }
                                                            }
                                                        }
                                                    }

                                                    TransactionWithDetails transactionWithDetails = new TransactionWithDetails(
                                                            transaction,
                                                            product,
                                                            otherUserName,
                                                            lastMessage,
                                                            lastMessageTimestamp,
                                                            isUserBuyer,
                                                            unreadCount.get() > 0,
                                                            unreadCount.get()
                                                    );

                                                    transactionsList.add(transactionWithDetails);
                                                    transactionsList.sort((t1, t2) ->
                                                            Long.compare(t2.getLastMessageTimestamp(), t1.getLastMessageTimestamp()));
                                                    adapter.notifyDataSetChanged();

                                                    if (transactionsList.isEmpty()) {
                                                        emptyStateTextView.setVisibility(View.VISIBLE);
                                                        transactionsRecyclerView.setVisibility(View.GONE);
                                                    } else {
                                                        emptyStateTextView.setVisibility(View.GONE);
                                                        transactionsRecyclerView.setVisibility(View.VISIBLE);
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void openTransactionChat(TransactionWithDetails transaction) {
        // Determină cine este destinatarul (vânzătorul sau cumpărătorul)
        String receiverId = transaction.isUserBuyer() ?
                transaction.getTransaction().getSellerId() :
                transaction.getTransaction().getBuyerId();

        // Deschide activitatea de chat
        Intent intent = new Intent(this, TransactionChatActivity.class);
        intent.putExtra("transactionId", transaction.getTransaction().getTransactionId());
        intent.putExtra("receiverId", receiverId);
        startActivity(intent);
    }
}