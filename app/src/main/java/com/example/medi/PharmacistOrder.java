package com.example.medi;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

// This model class MUST match the document structure in your
// "ordersReceived" subcollection
public class PharmacistOrder {

    @DocumentId
    private String orderId; // Will be auto-filled with the document ID

    // User's Info
    private String userId;
    private String userName;
    private String userPhone;

    // Order Info
    private String status;
    private double totalAmount;
    private String paymentId;
    private String userOrderRefId;

    @ServerTimestamp
    private Date orderTimestamp;

    // List of items in this order
    private List<Map<String, Object>> items;

    // --- REQUIRED: Empty constructor for Firestore ---
    public PharmacistOrder() {}

    // --- Getters (Required by Firestore) ---
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserPhone() { return userPhone; }
    public String getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public String getPaymentId() { return paymentId; }
    public String getUserOrderRefId() { return userOrderRefId; }
    public Date getOrderTimestamp() { return orderTimestamp; }
    public List<Map<String, Object>> getItems() { return items; }
}