package com.example.aplicatielicenta.main;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicatielicenta.R;
import com.example.aplicatielicenta.transaction.TransactionUtils;
import com.example.aplicatielicenta.adapters.TransactionsAdapter;
import com.example.aplicatielicenta.models.MessageModel;
import com.example.aplicatielicenta.models.Product;
import com.example.aplicatielicenta.models.TransactionModel;
import com.example.aplicatielicenta.models.TransactionWithDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class FragmentInbox extends Fragment {

    private static final String TAG = "FragmentInbox"; // Tag pentru logging
    private RecyclerView inboxRecyclerView;
    private TextView emptyInboxText;
    private TransactionsAdapter adapter;
    private List<TransactionWithDetails> transactionsList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    public FragmentInbox() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);

        // Inițializare UI components
        inboxRecyclerView = view.findViewById(R.id.inboxRecyclerView);
        emptyInboxText = view.findViewById(R.id.emptyInboxText);

        // Inițializare Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "Utilizator neautentificat!");
            emptyInboxText.setText("Trebuie să fii autentificat pentru a vedea conversațiile");
            emptyInboxText.setVisibility(View.VISIBLE);
            inboxRecyclerView.setVisibility(View.GONE);
            return view;
        }

        currentUserId = auth.getCurrentUser().getUid();
        Log.d(TAG, "User curent ID: " + currentUserId);

        // Inițializare RecyclerView
        transactionsList = new ArrayList<>();
        adapter = new TransactionsAdapter(requireContext(), transactionsList, transaction -> {
            startActivity(TransactionUtils.openChatIntent(requireContext(), transaction));
        });

        inboxRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        inboxRecyclerView.setAdapter(adapter);

        emptyInboxText.setVisibility(View.VISIBLE);
        inboxRecyclerView.setVisibility(View.GONE);


        setupItemTouchHelper();

        loadInbox();

        return view;
    }

    private void setupItemTouchHelper() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION || position >= transactionsList.size()) {
                    adapter.notifyDataSetChanged();
                    return;
                }

                TransactionWithDetails selectedTransaction = transactionsList.get(position);
                String transactionId = selectedTransaction.getTransaction().getTransactionId();

                new AlertDialog.Builder(requireContext())
                        .setTitle("Șterge conversația?")
                        .setMessage("Ești sigur că vrei să ștergi această conversație? Mesajele nu vor mai fi vizibile.")
                        .setPositiveButton("Șterge", (dialog, which) -> {
                            db.collection("transactions").document(transactionId)
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        transactionsList.remove(position);
                                        adapter.notifyItemRemoved(position);
                                        updateEmptyState();
                                        Log.d(TAG, "✅ Conversație ștearsă");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "❌ Eroare la ștergere", e);
                                        adapter.notifyItemChanged(position); // readaugă în UI dacă eșuează
                                    });
                        })
                        .setNegativeButton("Anulează", (dialog, which) -> {
                            adapter.notifyItemChanged(position); // readaugă în UI dacă utilizatorul renunță
                        })
                        .setCancelable(false)
                        .show();
            }
        }).attachToRecyclerView(inboxRecyclerView);
    }

    private void loadInbox() {
        Log.d(TAG, "Începe încărcarea conversațiilor pentru utilizatorul: " + currentUserId);

        db.collection("transactions")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((querySnapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Eroare la încărcare conversații", error);
                        emptyInboxText.setText("Eroare la încărcarea conversațiilor");
                        emptyInboxText.setVisibility(View.VISIBLE);
                        inboxRecyclerView.setVisibility(View.GONE);
                        return;
                    }

                    transactionsList.clear();

                    if (querySnapshots != null) {
                        Log.d(TAG, "Număr de documente primite: " + querySnapshots.size());

                        if (!querySnapshots.isEmpty()) {
                            for (DocumentSnapshot doc : querySnapshots) {
                                TransactionModel transaction = doc.toObject(TransactionModel.class);
                                if (transaction != null) {
                                    Log.d(TAG, "Transaction găsită: " + transaction.getTransactionId());
                                    // elimină temporar filtrarea cu hiddenFor sau status
                                    boolean isUserBuyer = currentUserId.equals(transaction.getBuyerId());
                                    loadConversationDetails(transaction, isUserBuyer);
                                } else {
                                    Log.w(TAG, "Document null de la Firestore: " + doc.getId());
                                }
                            }
                        } else {
                            Log.d(TAG, "Nu există conversații pentru acest utilizator");
                            updateEmptyState();
                        }
                    } else {
                        Log.w(TAG, "QuerySnapshot este null");
                        updateEmptyState();
                    }
                });
    }

    private void loadConversationDetails(TransactionModel transaction, boolean isUserBuyer) {
        Log.d(TAG, "Încărcare detalii pentru tranzacția: " + transaction.getTransactionId());

        // Produs
        db.collection("products").document(transaction.getProductId()).get()
                .addOnSuccessListener(productDoc -> {
                    Product product = productDoc.toObject(Product.class);
                    if (product == null) {
                        Log.w(TAG, "Produsul nu a fost găsit: " + transaction.getProductId());
                        product = new Product(); // Creează un obiect gol pentru a evita NullPointerException
                        product.setTitle("Produs indisponibil");
                    }

                    final Product finalProduct = product;
                    String otherUserId = isUserBuyer ? transaction.getSellerId() : transaction.getBuyerId();

                    // Utilizator
                    db.collection("users").document(otherUserId).get()
                            .addOnSuccessListener(userDoc -> {
                                String otherUserName = userDoc.getString("username");
                                if (otherUserName == null || otherUserName.isEmpty()) {
                                    otherUserName = "Utilizator necunoscut";
                                    Log.w(TAG, "Nume utilizator negăsit pentru: " + otherUserId);
                                }

                                final String finalOtherUserName = otherUserName;

                                // Mesaje
                                db.collection("transactions").document(transaction.getTransactionId())
                                        .collection("messages")
                                        .orderBy("timestamp", Query.Direction.DESCENDING)
                                        .limit(20)
                                        .get()
                                        .addOnSuccessListener(messages -> {
                                            int unreadCount = 0;
                                            String lastMessage = "";
                                            long lastMessageTime = 0;

                                            if (!messages.isEmpty()) {
                                                Log.d(TAG, "S-au găsit " + messages.size() + " mesaje pentru tranzacția " + transaction.getTransactionId());

                                                for (DocumentSnapshot msgDoc : messages) {
                                                    MessageModel msg = msgDoc.toObject(MessageModel.class);
                                                    if (msg != null) {
                                                        if (lastMessage.isEmpty()) {
                                                            lastMessage = msg.getMessage();
                                                            lastMessageTime = msg.getTimestamp();
                                                        }
                                                        if (msg.getReceiverId().equals(currentUserId) && !msg.isRead()) {
                                                            unreadCount++;
                                                        }
                                                    }
                                                }
                                            } else {
                                                Log.d(TAG, "Nu există mesaje pentru tranzacția " + transaction.getTransactionId());
                                                lastMessage = "Conversație nouă";
                                                lastMessageTime = transaction.getTimestamp();
                                            }

                                            TransactionWithDetails transactionWithDetails = new TransactionWithDetails(
                                                    transaction, finalProduct, finalOtherUserName, lastMessage,
                                                    lastMessageTime, isUserBuyer, unreadCount > 0, unreadCount
                                            );

                                            transactionsList.add(transactionWithDetails);
                                            transactionsList.sort((t1, t2) ->
                                                    Long.compare(t2.getLastMessageTimestamp(), t1.getLastMessageTimestamp()));

                                            // Notifică adaptorul pe UI thread
                                            if (getActivity() != null) {
                                                getActivity().runOnUiThread(() -> {
                                                    adapter.notifyDataSetChanged();
                                                    updateEmptyState();
                                                });
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Eroare la încărcarea mesajelor", e);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Eroare la încărcarea utilizatorului: " + otherUserId, e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Eroare la încărcarea produsului: " + transaction.getProductId(), e);
                });
    }

    private void updateEmptyState() {
        if (transactionsList.isEmpty()) {
            Log.d(TAG, "Lista de tranzacții este goală, afișez mesajul empty state");
            emptyInboxText.setVisibility(View.VISIBLE);
            inboxRecyclerView.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "Lista de tranzacții conține " + transactionsList.size() + " elemente");
            emptyInboxText.setVisibility(View.GONE);
            inboxRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reîncarcă datele când fragmentul devine vizibil din nou
        if (!transactionsList.isEmpty()) {
            transactionsList.clear();
            adapter.notifyDataSetChanged();
            loadInbox();
        }
    }
}