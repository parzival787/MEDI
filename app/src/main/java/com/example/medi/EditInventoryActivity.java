package com.example.medi; // Defines the package name where this activity belongs

import android.content.Intent; // Used to navigate between activities
import android.os.Bundle; // For saving and restoring activity state
import android.util.Log; // For logging messages (useful for debugging)
import android.view.View; // Base class for all UI components
import androidx.appcompat.app.AppCompatActivity; // Base class for all activities using AppCompat
import com.google.android.material.appbar.MaterialToolbar; // Toolbar widget from Material Design
import com.google.android.material.button.MaterialButton; // Material Design button widget

public class EditInventoryActivity extends AppCompatActivity { // Activity that allows pharmacist to add or update medicines

    private static final String TAG = "EditInventoryActivity"; // Tag used for logging/debugging

    // UI Elements
    private MaterialToolbar toolbar; // Toolbar at the top of the screen
    private MaterialButton btnAddMedicine; // Button to navigate to "Add Medicine" screen
    private MaterialButton btnUpdateMedicine; // Button to navigate to "Update Medicine" screen

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Called when the activity is first created
        super.onCreate(savedInstanceState); // Calls parent implementation
        setContentView(R.layout.activity_edit_inventory); // Sets the layout XML file for this activity

        // Find the UI elements
        toolbar = findViewById(R.id.toolbar_edit_inventory); // Connects the toolbar view from layout
        btnAddMedicine = findViewById(R.id.btnAddMedicine); // Connects the "Add Medicine" button
        btnUpdateMedicine = findViewById(R.id.btnUpdateMedicine); // Connects the "Update Medicine" button

        // Setup the toolbar with a back button
        setupToolbar(); // Calls helper method to configure toolbar

        // Setup click listeners for the buttons
        setupClickListeners(); // Calls helper method to handle button clicks
    }

    private void setupToolbar() { // Method to configure toolbar
        setSupportActionBar(toolbar); // Sets the toolbar as the app’s action bar
        if (getSupportActionBar() != null) { // Ensures the action bar exists
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Shows back arrow button
            getSupportActionBar().setDisplayShowHomeEnabled(true); // Makes home (back) button visible
        }
        // Handle the toolbar's navigation (back) button click
        toolbar.setNavigationOnClickListener(v -> { // Sets click listener for toolbar back arrow
            onBackPressed(); // Returns to the previous activity
        });
    }

    private void setupClickListeners() { // Method to set button click behaviors

        // 1. Add Medicine Button
        btnAddMedicine.setOnClickListener(v -> { // When Add Medicine button is clicked
            Log.d(TAG, "Add Medicine button clicked."); // Log the click event for debugging
            Intent intent = new Intent(EditInventoryActivity.this, addmedicine.class); // Create intent to open Add Medicine screen
            startActivity(intent); // Start the Add Medicine activity
        });

        // 2. Update Medicine Button
        btnUpdateMedicine.setOnClickListener(v -> { // When Update Medicine button is clicked
            Log.d(TAG, "Update Medicine button clicked."); // Log the click event for debugging
            Intent intent = new Intent(EditInventoryActivity.this, Updatemedicine.class); // Create intent to open Update Medicine screen
            startActivity(intent); // Start the Update Medicine activity
        });
    }
}
