package com.example.medi; // Package name for this class

import android.os.Bundle; // For saving/restoring activity state
import android.util.Log; // For logging debug information
import android.view.View; // For controlling view visibility
import android.widget.TextView; // For displaying text messages
import android.widget.Toast; // For showing quick feedback messages

import androidx.appcompat.app.AppCompatActivity; // Base class for activities
import androidx.recyclerview.widget.LinearLayoutManager; // Layout manager for RecyclerView
import androidx.recyclerview.widget.RecyclerView; // Displays a scrollable list of items

import com.google.android.material.appbar.MaterialToolbar; // Material Design toolbar
import com.google.firebase.auth.FirebaseAuth; // Firebase authentication reference
import com.google.firebase.firestore.DocumentSnapshot; // Represents a Firestore document
import com.google.firebase.firestore.FirebaseFirestore; // Firestore database reference
import com.google.firebase.firestore.Query; // Used for sorting Firestore queries

import java.util.ArrayList; // For managing a list of orders

public class MyOrdersActivity extends AppCompatActivity { // Activity to show user’s order history

    private static final String TAG = "MyOrdersActivity"; // Tag for debugging/logging

    private RecyclerView recyclerViewOrders; // List to display orders
    private TextView tvEmptyOrders; // Text shown when there are no orders
    private FirebaseFirestore db; // Firestore database instance
    private FirebaseAuth mAuth; // Firebase authentication instance

    private MyOrdersAdapter adapter; // Adapter that binds order data to RecyclerView
    private ArrayList<Order> orderList; // List to hold user's orders

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Called when activity is created
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders); // Set UI layout for this screen

        db = FirebaseFirestore.getInstance(); // Initialize Firestore
        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Authentication
        orderList = new ArrayList<>(); // Create an empty list to hold orders

        setupToolbar(); // Configure top toolbar
        initializeViews(); // Link UI elements with XML
        setupRecyclerView(); // Prepare RecyclerView with adapter
        loadOrders(); // Fetch user's order history from Firestore
    }

    private void setupToolbar() { // Method to configure toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar_my_orders); // Get toolbar view
        toolbar.setTitle("My Orders"); // Set toolbar title
        setSupportActionBar(toolbar); // Attach toolbar to the activity

        if (getSupportActionBar() != null) { // Enable back arrow if ActionBar exists
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back button
            getSupportActionBar().setDisplayShowHomeEnabled(true); // Enable back icon
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed()); // Handle back arrow click
    }

    private void initializeViews() { // Method to find all UI views
        recyclerViewOrders = findViewById(R.id.recycler_view_orders); // RecyclerView for orders
        tvEmptyOrders = findViewById(R.id.tv_empty_orders); // TextView for empty state
    }

    private void setupRecyclerView() { // Method to configure RecyclerView
        adapter = new MyOrdersAdapter(this, orderList); // Create adapter with context + data
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this)); // Display items vertically
        recyclerViewOrders.setAdapter(adapter); // Attach adapter to RecyclerView
    }

    private void loadOrders() { // Fetch user's order data from Firestore
        if (mAuth.getCurrentUser() == null) { // Check if user is logged in
            Log.w(TAG, "User not logged in, cannot load orders."); // Log warning
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show(); // Notify user
            tvEmptyOrders.setVisibility(View.VISIBLE); // Show empty message
            recyclerViewOrders.setVisibility(View.GONE); // Hide list
            return; // Exit method
        }

        String userId = mAuth.getCurrentUser().getUid(); // Get current user's ID
        Log.d(TAG, "Loading orders for user: " + userId); // Debug log for tracking

        tvEmptyOrders.setVisibility(View.GONE); // Hide "no orders" text temporarily
        recyclerViewOrders.setVisibility(View.GONE); // Hide list until data loads

        // Query Firestore for user’s orders sorted by date (newest first)
        db.collection("users").document(userId).collection("orders")
                .orderBy("orderTimestamp", Query.Direction.DESCENDING) // Sort descending
                .get() // Fetch all documents
                .addOnCompleteListener(task -> { // Handle async result
                    if (task.isSuccessful() && task.getResult() != null) { // If query successful
                        orderList.clear(); // Clear existing data before adding new ones

                        if (task.getResult().isEmpty()) { // If user has no orders
                            Log.d(TAG, "No orders found for this user."); // Log info
                            tvEmptyOrders.setVisibility(View.VISIBLE); // Show empty state
                            recyclerViewOrders.setVisibility(View.GONE); // Hide list
                        } else { // If there are orders
                            Log.d(TAG, "Found " + task.getResult().size() + " orders."); // Log count
                            tvEmptyOrders.setVisibility(View.GONE); // Hide empty text
                            recyclerViewOrders.setVisibility(View.VISIBLE); // Show list

                            for (DocumentSnapshot doc : task.getResult()) { // Loop through orders
                                Order order = doc.toObject(Order.class); // Convert document to Order object
                                if (order != null) { // Check for null
                                    orderList.add(order); // Add to list
                                }
                            }

                            adapter.notifyDataSetChanged(); // Refresh RecyclerView with new data
                        }
                    } else { // If query failed
                        Log.e(TAG, "Failed to load orders.", task.getException()); // Log error
                        Toast.makeText(this, "Failed to load orders.", Toast.LENGTH_SHORT).show(); // Show error message
                        tvEmptyOrders.setVisibility(View.VISIBLE); // Show empty text
                        recyclerViewOrders.setVisibility(View.GONE); // Hide list
                    }
                });
    }
}
