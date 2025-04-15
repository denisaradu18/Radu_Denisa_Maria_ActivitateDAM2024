package com.example.aplicatielicenta.models;

import java.util.List;

public class TransactionModel {
    private String transactionId;
    private String buyerId;
    private String sellerId;
    private String productId;
    private long timestamp;
    private List<String> participants;
    private String status;
    private long lastMessageTimestamp;

    private String initialMessage;
    private String requestedPickupTime;
    public TransactionModel() {}

    public TransactionModel(String transactionId, String buyerId, String sellerId, String productId, long timestamp, List<String> participants, String status, long lastMessageTimestamp) {
        this.transactionId = transactionId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.productId = productId;
        this.timestamp = timestamp;
        this.participants = participants;
        this.status = status;
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }
    public List<String> getParticipants() {
        return participants;
    }

    public String getInitialMessage() { return initialMessage; }
    public void setInitialMessage(String initialMessage) { this.initialMessage = initialMessage; }

    public String getRequestedPickupTime() { return requestedPickupTime; }
    public void setRequestedPickupTime(String requestedPickupTime) { this.requestedPickupTime = requestedPickupTime; }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionId() { return transactionId; }

    public String getBuyerId() { return buyerId; }
    public String getSellerId() { return sellerId; }

    public String getProductId() { return productId; }
    public long getTimestamp() { return timestamp; }
}
