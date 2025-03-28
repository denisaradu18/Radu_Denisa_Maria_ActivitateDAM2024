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

import android.graphics.Typeface;

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

        holder.productNameTextView.setText(transaction.getProduct().getTitle());
        holder.userNameTextView.setText(transaction.getOtherUserName());

        holder.roleTextView.setText(transaction.isUserBuyer() ? "Cumpărător" : "Vânzător");
        holder.lastMessageTextView.setText(transaction.getLastMessage());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(transaction.getLastMessageTimestamp()));
        holder.timestampTextView.setText(formattedDate);

        if (transaction.getProduct().getImageUrls() != null && !transaction.getProduct().getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(transaction.getProduct().getImageUrls().get(0)) // ✅ asigură-te că iei primul URL
                    .centerCrop()
                    .into(holder.productImageView);
        } else {
            holder.productImageView.setImageResource(R.drawable.placeholder_image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                v.animate().alpha(0f).setDuration(150).withEndAction(() -> {
                    listener.onTransactionClick(transaction);
                    v.setAlpha(1f);
                }).start();
            }
        });


        // 🔴 Afișează badge pentru mesaje necitite
        if (transaction.isHasUnread()) {
            holder.lastMessageTextView.setTypeface(null, Typeface.BOLD);
            holder.timestampTextView.setTypeface(null, Typeface.BOLD);
            holder.unreadBadge.setVisibility(View.VISIBLE);
        } else {
            holder.lastMessageTextView.setTypeface(null, Typeface.NORMAL);
            holder.timestampTextView.setTypeface(null, Typeface.NORMAL);
            holder.unreadBadge.setVisibility(View.GONE);
        }

        if (transaction.getUnreadCount() > 0) {
            holder.unreadBadge.setVisibility(View.VISIBLE);
            holder.unreadBadge.setText(String.valueOf(transaction.getUnreadCount()));
            holder.lastMessageTextView.setTypeface(null, Typeface.BOLD);
            holder.timestampTextView.setTypeface(null, Typeface.BOLD);
        } else {
            holder.unreadBadge.setVisibility(View.GONE);
            holder.lastMessageTextView.setTypeface(null, Typeface.NORMAL);
            holder.timestampTextView.setTypeface(null, Typeface.NORMAL);
        }

    }

    @Override
    public int getItemCount() {
        return transactionsList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productNameTextView, userNameTextView, roleTextView, lastMessageTextView, timestampTextView;
        TextView unreadBadge; // ✅ aici trebuie să fie

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImageView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            roleTextView = itemView.findViewById(R.id.roleTextView);
            lastMessageTextView = itemView.findViewById(R.id.lastMessageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            unreadBadge = itemView.findViewById(R.id.unreadBadge); // ✅ corect aici
        }
    }
}
