package com.example.aplicatielicenta;

import com.example.aplicatielicenta.Product;
import com.example.aplicatielicenta.TransactionModel;

public class TransactionWithDetails {
    private TransactionModel transaction;
    private Product product;
    private String otherUserName;
    private String lastMessage;
    private long lastMessageTimestamp;
    private boolean isUserBuyer;
    private boolean hasUnread;
    private int unreadCount; // ✅ NOU

    public TransactionWithDetails(TransactionModel transaction, Product product,
                                  String otherUserName, String lastMessage,
                                  long lastMessageTimestamp, boolean isUserBuyer,
                                  boolean hasUnread, int unreadCount) { // ✅ include și numărul
        this.transaction = transaction;
        this.product = product;
        this.otherUserName = otherUserName;
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.isUserBuyer = isUserBuyer;
        this.hasUnread = hasUnread;
        this.unreadCount = unreadCount;
    }

    public TransactionModel getTransaction() {
        return transaction;
    }

    public Product getProduct() {
        return product;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public boolean isUserBuyer() {
        return isUserBuyer;
    }

    public boolean isHasUnread() {
        return hasUnread;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
