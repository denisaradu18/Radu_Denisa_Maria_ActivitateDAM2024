package com.example.aplicatielicenta;

import android.content.Context;
import android.content.Intent;

public class TransactionUtils {

    public static Intent openChatIntent(Context context, TransactionWithDetails transaction) {
        String receiverId = transaction.isUserBuyer()
                ? transaction.getTransaction().getSellerId()
                : transaction.getTransaction().getBuyerId();

        Intent intent = new Intent(context, TransactionChatActivity.class);
        intent.putExtra("transactionId", transaction.getTransaction().getTransactionId());
        intent.putExtra("receiverId", receiverId);
        return intent;
    }
}
