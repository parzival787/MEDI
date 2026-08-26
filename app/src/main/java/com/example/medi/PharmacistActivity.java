package com.example.medi;                                        // Defines the package for this class

import android.content.Intent;                                     // Import Intent for navigating between screens
import android.os.Bundle;                                          // Import Bundle for passing data between Activities
import android.util.Log;                                           // Import Log to write debug messages
import android.view.View;                                          // Import View, base class for UI components
import android.widget.Toast;                                       // Import Toast to show short messages

import androidx.appcompat.app.AppCompatActivity;                  // Import AppCompatActivity for modern Activity features

import com.google.android.material.button.MaterialButton;          // Import MaterialButton from Material Design
import com.google.android.material.card.MaterialCardView;          // Import MaterialCardView from Material Design
import com.google.firebase.auth.FirebaseAuth;                      // Import FirebaseAuth to manage authentication

public class PharmacistActivity extends AppCompatActivity {        // Main Activity class for the pharmacist screen

    private static final String TAG = "PharmacistActivity";       // Tag for Log messages

    private MaterialCardView cardViewInventory, cardViewOrders, cardViewEditInventory; // Card views for screen options
    private MaterialButton btnLogout;                              // Logout button

    private FirebaseAuth mAuth;                                    // Firebase authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {           // Called when Activity is created
        super.onCreate(savedInstanceState);                        // Call parent constructor
        setContentView(R.layout.activity_pharmacist);              // Set the layout for this Activity

        mAuth = FirebaseAuth.getInstance();                        // Initialize Firebase authentication

        cardViewInventory = findViewById(R.id.cardViewInventory);  // Link Java variable to "View Inventory" card
        cardViewOrders = findViewById(R.id.cardViewOrders);        // Link Java variable to "Orders Received" card
        cardViewEditInventory = findViewById(R.id.cardViewEditInventory); // Link Java variable to "Edit Inventory" card

        btnLogout = findViewById(R.id.btnLogoutPharmacist);        // Link Java variable to logout button

        setupClickListeners();                                     // Setup click listeners for cards and button
    }

    private void setupClickListeners() {                           // Method to define click actions

        cardViewInventory.setOnClickListener(v -> {                // Click listener for "View Inventory" card
            Log.d(TAG, "View Inventory card clicked.");            // Log click for debugging
            Intent intent = new Intent(PharmacistActivity.this, ViewInventoryActivity.class); // Create intent for inventory screen
            startActivity(intent);                                  // Open inventory screen
        });

        cardViewOrders.setOnClickListener(v -> {                   // Click listener for "Orders Received" card
            Log.d(TAG, "Orders Received card clicked.");           // Log click
            Intent intent = new Intent(PharmacistActivity.this, OrdersReceivedActivity.class); // Create intent for orders screen
            startActivity(intent);                                  // Open orders screen
        });

        cardViewEditInventory.setOnClickListener(v -> {            // Click listener for "Edit Inventory" card
            Log.d(TAG, "Edit Inventory card clicked.");            // Log click
            Intent intent = new Intent(PharmacistActivity.this, EditInventoryActivity.class); // Create intent for edit inventory
            startActivity(intent);                                  // Open edit inventory screen
        });

        btnLogout.setOnClickListener(v -> {                        // Click listener for logout button
            Log.d(TAG, "Logout button clicked.");                  // Log click
            mAuth.signOut();                                        // Sign out user from Firebase
            Intent intent = new Intent(PharmacistActivity.this, MainActivity.class); // Create intent to go back to main screen
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);                                  // Open MainActivity
            finish();                                               // Close current Activity
            Log.d(TAG, "User signed out and navigated to MainActivity."); // Log successful logout
        });
    }
}
