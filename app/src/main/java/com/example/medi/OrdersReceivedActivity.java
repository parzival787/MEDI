package com.example.medi; // Declares the package this class belongs to

// Import required Android, Firebase, and UI classes
import android.os.Bundle; // Bundle for passing data between activities
import android.util.Log; // Logging utility
import android.view.View; // Represents UI elements
import android.widget.ProgressBar; // UI element to show loading
import android.widget.TextView; // UI element to show text
import android.widget.Toast; // Small popup messages

import androidx.appcompat.app.AppCompatActivity; // Base activity class
import androidx.recyclerview.widget.LinearLayoutManager; // Layout manager for RecyclerView
import androidx.recyclerview.widget.RecyclerView; // RecyclerView for lists

import com.google.android.material.appbar.MaterialToolbar; // Material Design toolbar
import com.google.firebase.auth.FirebaseAuth; // Firebase authentication
import com.google.firebase.auth.FirebaseUser; // Represents currently logged-in user
import com.google.firebase.firestore.FirebaseFirestore; // Firebase Firestore database
import com.google.firebase.firestore.Query; // Firestore query
import com.google.firebase.firestore.QueryDocumentSnapshot; // Firestore document returned in query

import java.util.ArrayList; // List implementation
import java.util.List; // List interface

// Activity to show orders received by the pharmacist
public class OrdersReceivedActivity extends AppCompatActivity {

    private static final String TAG = "OrdersReceivedActivity"; // Tag for logging

    // UI components
    private RecyclerView recyclerView; // RecyclerView to show list of orders
    private ProgressBar progressBar; // Progress bar while loading
    private TextView tvEmpty; // TextView to show "No orders" or error messages

    // Firebase
    private FirebaseFirestore db; // Firestore database instance
    private FirebaseAuth mAuth; // Firebase authentication instance

    // Data
    private OrdersReceivedAdapter adapter; // RecyclerView adapter
    private List<PharmacistOrder> orderList; // List of orders

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_received); // Set the activity layout

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupToolbar(); // Configure the toolbar
        initializeViews(); // Find UI elements by ID
        setupRecyclerView(); // Prepare RecyclerView with adapter

        loadReceivedOrders(); // Load orders from Firestore
    }

    // Setup the top toolbar
    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar_orders_received); // Get toolbar from layout
        setSupportActionBar(toolbar); // Set as support action bar

        // Add back button functionality
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back arrow
            getSupportActionBar().setDisplayShowHomeEnabled(true); // Enable back navigation
        }

        // Handle back arrow click
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    // Find and initialize UI components
    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_view_orders_received); // RecyclerView for orders
        progressBar = findViewById(R.id.progressBar_orders); // Loading indicator
        tvEmpty = findViewById(R.id.tv_empty_orders); // Message when list is empty
    }

    // Setup RecyclerView with adapter and layout manager
    private void setupRecyclerView() {
        orderList = new ArrayList<>(); // Initialize empty list
        adapter = new OrdersReceivedAdapter(this, orderList); // Initialize adapter with context and list
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Use vertical list
        recyclerView.setAdapter(adapter); // Attach adapter to RecyclerView
    }

    // Load received orders from Firestore
    private void loadReceivedOrders() {
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get currently logged-in user
        if (currentUser == null) { // Check if user is logged in
            Toast.makeText(this, "Not logged in!", Toast.LENGTH_SHORT).show(); // Show message
            Log.w(TAG, "User is not logged in."); // Log warning
            progressBar.setVisibility(View.GONE); // Hide progress bar
            finish(); // Close activity if no user
            return;
        }

        String pharmacistId = currentUser.getUid(); // Get UID of logged-in pharmacist

        Log.d(TAG, "Loading received orders for pharmacist: " + pharmacistId); // Log debug message
        progressBar.setVisibility(View.VISIBLE); // Show loading indicator
        tvEmpty.setVisibility(View.GONE); // Hide empty message
        recyclerView.setVisibility(View.GONE); // Hide list until data loads

        // Query the "ordersReceived" subcollection for this pharmacist
        db.collection("pharmacists").document(pharmacistId)
                .collection("ordersReceived")
                .orderBy("orderTimestamp", Query.Direction.DESCENDING) // Sort by newest orders first
                .get() // Get all documents once
                .addOnCompleteListener(task -> { // Callback when query finishes
                    progressBar.setVisibility(View.GONE); // Hide progress bar

                    if (task.isSuccessful()) { // If query succeeded
                        orderList.clear(); // Clear old data

                        if (task.getResult().isEmpty()) { // No orders found
                            Log.d(TAG, "No orders found.");
                            tvEmpty.setVisibility(View.VISIBLE); // Show "No orders" message
                        } else {
                            // Loop through all documents
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                PharmacistOrder order = doc.toObject(PharmacistOrder.class); // Convert document to object
                                orderList.add(order); // Add to list
                            }
                            adapter.notifyDataSetChanged(); // Refresh RecyclerView
                            recyclerView.setVisibility(View.VISIBLE); // Show the list
                            Log.d(TAG, "Successfully loaded " + orderList.size() + " orders."); // Log number of orders
                        }
                    } else { // Query failed
                        Log.e(TAG, "Error loading orders: ", task.getException()); // Log error
                        Toast.makeText(OrdersReceivedActivity.this, "Error loading orders.", Toast.LENGTH_SHORT).show(); // Show message
                        tvEmpty.setText("Error loading data."); // Update empty message
                        tvEmpty.setVisibility(View.VISIBLE); // Show empty message
                    }
                });
    }
}
