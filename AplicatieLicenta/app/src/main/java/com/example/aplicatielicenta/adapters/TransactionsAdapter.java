package com.example.aplicatielicenta.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aplicatielicenta.R;
import com.example.aplicatielicenta.models.TransactionWithDetails;
import com.example.aplicatielicenta.notification.NotificationActivity;

import android.graphics.Typeface;
import android.widget.Toast;

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

        if (position < 0 || position >= transactionsList.size()) {
            return;
        }

        TransactionWithDetails transaction = transactionsList.get(position);

        if (transaction == null) {
            return;
        }

        if (holder.productNameTextView != null && transaction.getProduct() != null) {
            holder.productNameTextView.setText(transaction.getProduct().getTitle() != null
                    ? transaction.getProduct().getTitle()
                    : "Produs fără nume");
        }

        if (holder.userNameTextView != null) {
            holder.userNameTextView.setText(transaction.getOtherUserName() != null
                    ? transaction.getOtherUserName()
                    : "Utilizator necunoscut");
        }

        if (holder.roleTextView != null) {
            holder.roleTextView.setText(transaction.isUserBuyer() ? "Cumpărător" : "Vânzător");
        }

        if (holder.lastMessageTextView != null) {
            holder.lastMessageTextView.setText(transaction.getLastMessage() != null
                    ? transaction.getLastMessage()
                    : "Nici un mesaj");
        }

        if (holder.timestampTextView != null) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
                String formattedDate = dateFormat.format(new Date(transaction.getLastMessageTimestamp()));
                holder.timestampTextView.setText(formattedDate);
            } catch (Exception e) {
                holder.timestampTextView.setText("Data indisponibilă");
            }
        }

        if (holder.productImageView != null) {
            if (transaction.getProduct() != null
                    && transaction.getProduct().getImageUrls() != null
                    && !transaction.getProduct().getImageUrls().isEmpty()) {
                Glide.with(context)
                        .load(transaction.getProduct().getImageUrls().get(0))
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(holder.productImageView);
            } else {
                holder.productImageView.setImageResource(R.drawable.placeholder_image);
            }
        }

        if (listener != null) {
            holder.itemView.setOnClickListener(v -> {
                v.animate().alpha(0f).setDuration(150).withEndAction(() -> {
                    listener.onTransactionClick(transaction);
                    v.setAlpha(1f);
                }).start();
            });
        }

        // Unread messages handling
        if (holder.unreadBadge != null) {
            if (transaction.isHasUnread() || transaction.getUnreadCount() > 0) {
                holder.lastMessageTextView.setTypeface(null, Typeface.BOLD);
                holder.timestampTextView.setTypeface(null, Typeface.BOLD);
                holder.unreadBadge.setVisibility(View.VISIBLE);

                if (transaction.getUnreadCount() > 0) {
                    holder.unreadBadge.setText(String.valueOf(transaction.getUnreadCount()));
                }
            } else {
                holder.lastMessageTextView.setTypeface(null, Typeface.NORMAL);
                holder.timestampTextView.setTypeface(null, Typeface.NORMAL);
                holder.unreadBadge.setVisibility(View.GONE);
            }
        }
        holder.itemView.setOnClickListener(v -> {
            String status = transaction.getTransaction().getStatus();
            if ("pending".equals(status) && !transaction.isUserBuyer()) {
                // Seller viewing pending transaction - prompt to go to notifications
                new AlertDialog.Builder(context)
                        .setTitle("Pending Request")
                        .setMessage("This transaction is pending your approval. Check your notifications to accept or decline.")
                        .setPositiveButton("Go to Notifications", (dialog, which) -> {
                            Intent intent = new Intent(context, NotificationActivity.class);
                            context.startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            else if ("pending".equals(status) && transaction.isUserBuyer()) {
                // Buyer viewing their own pending transaction
                Toast.makeText(context, "Waiting for seller to accept your request", Toast.LENGTH_SHORT).show();
            }
            else if ("declined".equals(status)) {
                Toast.makeText(context, "This transaction has been declined", Toast.LENGTH_SHORT).show();
            }
            else {
                // For accepted or completed transactions, open the chat
                listener.onTransactionClick(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionsList != null ? transactionsList.size() : 0;
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productNameTextView, userNameTextView, roleTextView, lastMessageTextView, timestampTextView;
        TextView unreadBadge;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            // Add null checks during view finding
            productImageView = itemView.findViewById(R.id.productImageView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            roleTextView = itemView.findViewById(R.id.roleTextView);
            lastMessageTextView = itemView.findViewById(R.id.lastMessageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            unreadBadge = itemView.findViewById(R.id.unreadBadge);
        }
    }
}