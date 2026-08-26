package com.example.medi; // Define the package name for this activity

import android.os.Bundle; // Bundle used for passing data between activities
import android.util.Log; // Logging utility
import android.view.View; // For manipulating UI visibility
import android.widget.ProgressBar; // Loading spinner UI element
import android.widget.TextView; // TextView UI element
import android.widget.Toast; // Toast messages for user feedback

import androidx.appcompat.app.AppCompatActivity; // Base class for activities with action bar
import androidx.recyclerview.widget.LinearLayoutManager; // RecyclerView layout manager (vertical list)
import androidx.recyclerview.widget.RecyclerView; // RecyclerView for displaying list of items

import com.google.android.material.appbar.MaterialToolbar; // Material Design toolbar
import com.google.firebase.auth.FirebaseAuth; // Firebase Authentication
import com.google.firebase.auth.FirebaseUser; // Represents the currently signed-in user
import com.google.firebase.firestore.FirebaseFirestore; // Firestore database
import com.google.firebase.firestore.Query; // Firestore query object
import com.google.firebase.firestore.QueryDocumentSnapshot; // Represents a document in a Firestore query result

import java.util.ArrayList; // For creating dynamic lists
import java.util.List; // List interface

public class ViewInventoryActivity extends AppCompatActivity { // Activity for viewing the pharmacist's inventory

    private static final String TAG = "ViewInventoryActivity"; // Tag for logging

    // UI Components
    private RecyclerView recyclerView; // RecyclerView to display list of medicines
    private ProgressBar progressBar; // Loading spinner while fetching data
    private TextView tvEmpty; // TextView to show "no items" or error message

    // Firebase Components
    private FirebaseFirestore db; // Firestore database instance
    private FirebaseAuth mAuth; // Firebase Authentication instance

    // Data Components
    private InventoryAdapter adapter; // Adapter for RecyclerView
    private List<Medicine> medicineList; // List to hold medicine data

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Called when activity is created
        super.onCreate(savedInstanceState); // Call superclass method
        setContentView(R.layout.activity_view_inventory); // Set the layout XML for this activity

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance(); // Get Firestore instance
        mAuth = FirebaseAuth.getInstance(); // Get FirebaseAuth instance

        setupToolbar(); // Setup toolbar with back button
        initializeViews(); // Bind UI elements to variables
        setupRecyclerView(); // Initialize RecyclerView and adapter

        loadInventory(); // Load inventory data from Firestore
    }

    private void setupToolbar() { // Setup toolbar UI
        MaterialToolbar toolbar = findViewById(R.id.toolbar_inventory); // Find toolbar by ID
        setSupportActionBar(toolbar); // Set as support action bar
        // Add back button if action bar is not null
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back arrow
            getSupportActionBar().setDisplayShowHomeEnabled(true); // Enable home button
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed()); // Handle back arrow click
    }

    private void initializeViews() { // Bind UI components
        recyclerView = findViewById(R.id.recycler_view_inventory); // RecyclerView for inventory
        progressBar = findViewById(R.id.progressBar_inventory); // ProgressBar while loading
        tvEmpty = findViewById(R.id.tv_empty_inventory); // TextView for empty/error state
    }

    private void setupRecyclerView() { // Setup RecyclerView and its adapter
        medicineList = new ArrayList<>(); // Initialize empty list
        adapter = new InventoryAdapter(this, medicineList); // Create adapter with context and list
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Vertical list
        recyclerView.setAdapter(adapter); // Attach adapter to RecyclerView
    }

    private void loadInventory() { // Load inventory data from Firestore
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get currently logged-in user
        if (currentUser == null) { // If no user logged in
            Toast.makeText(this, "Not logged in!", Toast.LENGTH_SHORT).show(); // Show message
            Log.w(TAG, "User is not logged in."); // Log warning
            progressBar.setVisibility(View.GONE); // Hide progress bar
            finish(); // Close activity
            return; // Stop execution
        }

        String pharmacistId = currentUser.getUid(); // Get user ID of pharmacist

        Log.d(TAG, "Loading inventory for pharmacist: " + pharmacistId); // Log pharmacist ID
        progressBar.setVisibility(View.VISIBLE); // Show loading spinner
        tvEmpty.setVisibility(View.GONE); // Hide empty text
        recyclerView.setVisibility(View.GONE); // Hide RecyclerView initially

        // Query Firestore for this pharmacist's inventory
        db.collection("pharmacists").document(pharmacistId) // Go to specific pharmacist document
                .collection("my_inventory") // Access "my_inventory" subcollection
                .orderBy("name", Query.Direction.ASCENDING) // Sort medicines alphabetically by name
                .get() // Fetch documents
                .addOnCompleteListener(task -> { // Callback when query completes
                    progressBar.setVisibility(View.GONE); // Hide progress bar

                    if (task.isSuccessful()) { // If query succeeded
                        medicineList.clear(); // Clear old list

                        if (task.getResult().isEmpty()) { // If no documents returned
                            Log.d(TAG, "Inventory is empty."); // Log info
                            tvEmpty.setVisibility(View.VISIBLE); // Show "empty" text
                        } else { // If documents exist
                            for (QueryDocumentSnapshot doc : task.getResult()) { // Loop through each document
                                Medicine medicine = doc.toObject(Medicine.class); // Convert document to Medicine object
                                medicineList.add(medicine); // Add to list
                            }
                            adapter.notifyDataSetChanged(); // Notify adapter of changes
                            recyclerView.setVisibility(View.VISIBLE); // Show RecyclerView
                            Log.d(TAG, "Successfully loaded " + medicineList.size() + " items."); // Log count
                        }
                    } else { // If query failed
                        Log.e(TAG, "Error loading inventory: ", task.getException()); // Log error
                        Toast.makeText(ViewInventoryActivity.this, "Error loading inventory.", Toast.LENGTH_SHORT).show(); // Show toast
                        tvEmpty.setText("Error loading data."); // Show error text
                        tvEmpty.setVisibility(View.VISIBLE); // Make TextView visible
                    }
                });
    }
}
