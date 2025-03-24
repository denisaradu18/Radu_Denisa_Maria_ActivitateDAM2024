package com.example.aplicatielicenta;

public class TransactionModel {
    private String transactionId;
    private String buyerId;    // În loc de senderId
    private String sellerId;
    private String productId;
    private long timestamp;

    public TransactionModel() {} // Firestore needs an empty constructor

    public TransactionModel(String transactionId, String buyerId, String sellerId, String productId, long timestamp) {
        this.transactionId = transactionId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.productId = productId;
        this.timestamp = timestamp;
    }

    public String getTransactionId() { return transactionId; }

    public String getBuyerId() { return buyerId; }
    public String getSellerId() { return sellerId; }

    public String getProductId() { return productId; }
    public long getTimestamp() { return timestamp; }
}
