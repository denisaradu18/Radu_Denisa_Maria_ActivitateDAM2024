package com.example.aplicatielicenta;

public class TransactionWithDetails {
    private TransactionModel transaction;
    private Product product;
    private String otherUserName;
    private String lastMessage;
    private long lastMessageTimestamp;
    private boolean isUserBuyer;

    public TransactionWithDetails(TransactionModel transaction, Product product,
                                  String otherUserName, String lastMessage,
                                  long lastMessageTimestamp, boolean isUserBuyer) {
        this.transaction = transaction;
        this.product = product;
        this.otherUserName = otherUserName;
        this.lastMessage = lastMessage;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.isUserBuyer = isUserBuyer;
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
}