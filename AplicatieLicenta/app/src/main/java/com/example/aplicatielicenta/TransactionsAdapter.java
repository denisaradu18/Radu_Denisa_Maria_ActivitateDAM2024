package com.example.aplicatielicenta;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {
    private Context context;
    private List<TransactionWithDetails> transactionsList;
    private OnTransactionClickListener listener;

    public interface OnTransactionClickListener {
        void onTransactionClick(TransactionWithDetails transaction);
    }

    public TransactionsAdapter(Context context, List<TransactionWithDetails> transactionsList, OnTransactionClickListener listener) {
        this.context = context;
        this.transactionsList = transactionsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionWithDetails transaction = transactionsList.get(position);

        // Setează titlul produsului
        holder.productNameTextView.setText(transaction.getProduct().getTitle());

        // Setează numele celuilalt utilizator
        holder.userNameTextView.setText(transaction.getOtherUserName());

        // Setează rolul utilizatorului
        if (transaction.isUserBuyer()) {
            holder.roleTextView.setText("Cumpărător");
        } else {
            holder.roleTextView.setText("Vânzător");
        }

        // Setează ultimul mesaj
        holder.lastMessageTextView.setText(transaction.getLastMessage());

        // Setează data ultimului mesaj
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(transaction.getLastMessageTimestamp()));
        holder.timestampTextView.setText(formattedDate);

        // Încarcă imaginea produsului
        if (transaction.getProduct().getImageUrls() != null && !transaction.getProduct().getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(transaction.getProduct().getImageUrls())
                    .centerCrop()
                    .into(holder.productImageView);
        } else {
            // Setează o imagine placeholder dacă nu există imagine
            holder.productImageView.setImageResource(R.drawable.placeholder_image);
        }

        // Setează listener pentru click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTransactionClick(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionsList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productNameTextView, userNameTextView, roleTextView, lastMessageTextView, timestampTextView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImageView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            roleTextView = itemView.findViewById(R.id.roleTextView);
            lastMessageTextView = itemView.findViewById(R.id.lastMessageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}