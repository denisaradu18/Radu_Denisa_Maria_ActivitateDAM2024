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
        // Obține toate tranzacțiile în care utilizatorul curent este implicat (ca buyer sau seller)
        db.collection("transactions")
                .whereEqualTo("buyerId", currentUserId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(buyerQueryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : buyerQueryDocumentSnapshots) {
                        TransactionModel transaction = doc.toObject(TransactionModel.class);
                        if (transaction != null) {
                            loadTransactionDetails(transaction, true);
                        }
                    }

                    // Continuă cu tranzacțiile în care utilizatorul este vânzător
                    db.collection("transactions")
                            .whereEqualTo("sellerId", currentUserId)
                            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                            .get()
                            .addOnSuccessListener(sellerQueryDocumentSnapshots -> {
                                for (DocumentSnapshot doc : sellerQueryDocumentSnapshots) {
                                    TransactionModel transaction = doc.toObject(TransactionModel.class);
                                    if (transaction != null) {
                                        loadTransactionDetails(transaction, false);
                                    }
                                }

                                // Verifică dacă lista este goală
                                if (transactionsList.isEmpty()) {
                                    emptyStateTextView.setVisibility(View.VISIBLE);
                                    transactionsRecyclerView.setVisibility(View.GONE);
                                } else {
                                    emptyStateTextView.setVisibility(View.GONE);
                                    transactionsRecyclerView.setVisibility(View.VISIBLE);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("TransactionsActivity", "Error loading transactions", e);
                });
    }

    private void loadTransactionDetails(TransactionModel transaction, boolean isUserBuyer) {
        // Obține detaliile produsului
        db.collection("products").document(transaction.getProductId())
                .get()
                .addOnSuccessListener(productDoc -> {
                    if (productDoc.exists()) {
                        Product product = productDoc.toObject(Product.class);

                        // Determină ID-ul celuilalt utilizator (vânzător sau cumpărător)
                        String otherUserId = isUserBuyer ? transaction.getSellerId() : transaction.getBuyerId();

                        // Obține detaliile celuilalt utilizator
                        db.collection("users").document(otherUserId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String otherUserName = userDoc.getString("name");

                                        // Obține ultimul mesaj din tranzacție
                                        db.collection("transactions")
                                                .document(transaction.getTransactionId())
                                                .collection("messages")
                                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                                .limit(1)
                                                .get()
                                                .addOnSuccessListener(messageQueryDocumentSnapshots -> {
                                                    String lastMessage = "";
                                                    long lastMessageTimestamp = transaction.getTimestamp();

                                                    if (!messageQueryDocumentSnapshots.isEmpty()) {
                                                        MessageModel message = messageQueryDocumentSnapshots.getDocuments()
                                                                .get(0).toObject(MessageModel.class);
                                                        if (message != null) {
                                                            lastMessage = message.getMessage();
                                                            lastMessageTimestamp = message.getTimestamp();
                                                        }
                                                    }

                                                    // Creează un obiect TransactionWithDetails
                                                    TransactionWithDetails transactionWithDetails = new TransactionWithDetails(
                                                            transaction,
                                                            product,
                                                            otherUserName,
                                                            lastMessage,
                                                            lastMessageTimestamp,
                                                            isUserBuyer
                                                    );

                                                    // Adaugă la listă și actualizează adapter-ul
                                                    transactionsList.add(transactionWithDetails);
                                                    // Sortează lista după timestamp-ul ultimului mesaj
                                                    transactionsList.sort((t1, t2) ->
                                                            Long.compare(t2.getLastMessageTimestamp(), t1.getLastMessageTimestamp()));
                                                    adapter.notifyDataSetChanged();

                                                    // Verifică dacă lista este goală
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