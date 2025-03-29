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
        View view = inflater.inflate(com.example.aplicatielicenta.R.layout.fragment_inbox, container, false);

        inboxRecyclerView = view.findViewById(com.example.aplicatielicenta.R.id.inboxRecyclerView);
        emptyInboxText = view.findViewById(R.id.emptyInboxText);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        transactionsList = new ArrayList<>();
        adapter = new TransactionsAdapter(requireContext(), transactionsList, transaction -> {
            // Deschide TransactionChatActivity
            startActivity(TransactionUtils.openChatIntent(requireContext(), transaction));
        });

        inboxRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        inboxRecyclerView.setAdapter(adapter);

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
                                        Log.d("InboxSwipe", "✅ Conversație ștearsă");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("InboxSwipe", "❌ Eroare la ștergere", e);
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

        loadInbox();
        return view;
    }

    private void loadInbox() {
        db.collection("transactions")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((querySnapshots, error) -> {
                    if (error != null) {
                        Log.e("FragmentInbox", "Eroare la încărcare conversații", error);
                        return;
                    }

                    transactionsList.clear();

                    if (querySnapshots != null) {
                        for (DocumentSnapshot doc : querySnapshots) {
                            TransactionModel transaction = doc.toObject(TransactionModel.class);
                            if (transaction != null) {
                                boolean isUserBuyer = currentUserId.equals(transaction.getBuyerId());
                                loadConversationDetails(transaction, isUserBuyer);
                            }
                        }
                    }
                });
    }

    private void loadConversationDetails(TransactionModel transaction, boolean isUserBuyer) {
        db.collection("products").document(transaction.getProductId()).get().addOnSuccessListener(productDoc -> {
            Product product = productDoc.toObject(Product.class);
            String otherUserId = isUserBuyer ? transaction.getSellerId() : transaction.getBuyerId();

            db.collection("users").document(otherUserId).get().addOnSuccessListener(userDoc -> {
                String otherUserName = userDoc.getString("name");

                db.collection("transactions").document(transaction.getTransactionId())
                        .collection("messages")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener(messages -> {
                            int unreadCount = 0;
                            String lastMessage = "";
                            long lastMessageTime = 0;

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

                            TransactionWithDetails transactionWithDetails = new TransactionWithDetails(
                                    transaction, product, otherUserName, lastMessage,
                                    lastMessageTime, isUserBuyer, unreadCount > 0, unreadCount
                            );

                            transactionsList.add(transactionWithDetails);
                            transactionsList.sort((t1, t2) -> Long.compare(t2.getLastMessageTimestamp(), t1.getLastMessageTimestamp()));
                            adapter.notifyDataSetChanged();
                        });
            });
        });
    }
}
