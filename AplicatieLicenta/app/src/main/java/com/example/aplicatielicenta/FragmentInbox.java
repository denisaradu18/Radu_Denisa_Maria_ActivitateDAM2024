package com.example.aplicatielicenta;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FragmentInbox extends Fragment {
    private RecyclerView inboxRecyclerView;
    private TextView emptyMessage;
    private InboxAdapter inboxAdapter;
    private List<TransactionModel> transactionsList;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        inboxRecyclerView = view.findViewById(R.id.inboxRecyclerView);
        emptyMessage = view.findViewById(R.id.empty_message);

        transactionsList = new ArrayList<>();
        inboxAdapter = new InboxAdapter(transactionsList, getContext());

        inboxRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        inboxRecyclerView.setAdapter(inboxAdapter);

        loadTransactions();
        return view;
    }

    private void loadTransactions() {
        db.collection("transactions")
                .whereEqualTo("senderId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        transactionsList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            TransactionModel transaction = doc.toObject(TransactionModel.class);
                            transactionsList.add(transaction);
                        }
                        if (transactionsList.isEmpty()) {
                            emptyMessage.setVisibility(View.VISIBLE);
                        } else {
                            emptyMessage.setVisibility(View.GONE);
                        }
                        inboxAdapter.notifyDataSetChanged();
                    }
                });
    }
}
