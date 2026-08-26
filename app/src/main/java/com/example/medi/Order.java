package com.example.medi;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

// This class MUST have an empty constructor for Firestore
public class Order {

    @DocumentId
    private String orderId; // Will be auto-filled with the document ID

    private String userId;
    private String paymentId;
    private String status;
    private double totalAmount;

    @ServerTimestamp
    private Date orderTimestamp; // Firestore will fill this

    // We save items as a List<Map<String, Object>>
    // so we can retrieve it the same way.
    private List<Map<String, Object>> items;

    // --- Empty constructor is REQUIRED for Firestore ---
    public Order() {}

    // --- Getters ---
    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getStatus() {
        return status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public Date getOrderTimestamp() {
        return orderTimestamp;
    }

    public List<Map<String, Object>> getItems() {
        return items;
    }
}