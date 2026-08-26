package com.example.medi;

import com.google.firebase.firestore.DocumentId;

// Model class for a Medicine item in inventory
public class Medicine {

    @DocumentId
    private String medicineId; // Auto-filled with Firestore document ID

    private String name;
    private String manufacturer;
    private String description;
    private double price;
    private long stock; // Use 'long' for Firestore numbers

    // --- REQUIRED: Empty constructor for Firestore's toObject() method ---
    public Medicine() {}

    // --- Getters (Firestore needs these) ---
    public String getMedicineId() {
        return medicineId;
    }

    public String getName() {
        return name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public long getStock() {
        return stock;
    }
}