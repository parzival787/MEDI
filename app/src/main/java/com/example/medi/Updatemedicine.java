package com.example.medi; // Declare the package this file belongs to

import android.os.Bundle; // Import Bundle class for activity state management
import android.text.TextUtils; // Import utility class for text validation
import android.view.View; // Import View class for UI elements
import android.widget.Button; // Import Button widget
import android.widget.ImageButton; // Import ImageButton widget
import android.widget.LinearLayout; // Import LinearLayout container
import android.widget.ProgressBar; // Import ProgressBar widget
import android.widget.Toast; // Import Toast class for short messages

import androidx.appcompat.app.AppCompatActivity; // Import base class for activities
import androidx.recyclerview.widget.LinearLayoutManager; // Import layout manager for RecyclerView
import androidx.recyclerview.widget.RecyclerView; // Import RecyclerView

import com.google.android.material.appbar.MaterialToolbar; // Import Material Toolbar
import com.google.android.material.card.MaterialCardView; // Import Material CardView
import com.google.android.material.switchmaterial.SwitchMaterial; // Import Material Switch
import com.google.android.material.textfield.TextInputEditText; // Import Material EditText
import com.google.android.material.textfield.TextInputLayout; // Import Material Input Layout
import com.google.firebase.auth.FirebaseAuth; // Import Firebase Authentication
import com.google.firebase.auth.FirebaseUser; // Import Firebase User
import com.google.firebase.firestore.FirebaseFirestore; // Import Firestore database
import com.google.firebase.firestore.QueryDocumentSnapshot; // Import Firestore query snapshot

import java.util.ArrayList; // Import ArrayList
import java.util.HashMap; // Import HashMap
import java.util.Map; // Import Map interface
import java.util.Objects; // Import Objects utility

public class Updatemedicine extends AppCompatActivity { // Define activity class

    // --- UI Views ---
    private TextInputEditText etSearchName; // Search input field
    private Button btnSearch, btnUpdateRecords; // Buttons for searching and updating
    private LinearLayout loadingContainer; // Container for loading indicator
    private MaterialCardView searchResultsCard; // CardView for search results
    private MaterialCardView detailsCard; // CardView for medicine details
    private SwitchMaterial switchRequiresPrescription; // Switch for prescription requirement
    private RecyclerView recyclerViewSearchResults; // RecyclerView to show search results

    // Editable Field Views
    private View viewName, viewDescription, viewManufacturer, viewPrice, viewStock; // Parent views for each field
    private TextInputEditText etMedicineName, etDescription, etManufacturer, etPrice, etStock; // Editable fields

    // --- Firebase ---
    private FirebaseFirestore db; // Firestore database instance
    private FirebaseAuth mAuth; // Firebase authentication instance
    private String pharmacistId; // Logged-in pharmacist ID
    private String foundMedicineId; // ID of selected medicine

    private MedicineSearchUserAdapter adapter; // RecyclerView adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Activity onCreate method
        super.onCreate(savedInstanceState); // Call superclass onCreate
        setContentView(R.layout.activity_updatemedicine); // Set activity layout

        db = FirebaseFirestore.getInstance(); // Initialize Firestore
        mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get current user
        if (currentUser == null) { // Check if user is not logged in
            Toast.makeText(this, "Authentication required.", Toast.LENGTH_SHORT).show(); // Show warning
            finish(); // Close activity
            return; // Exit method
        }
        pharmacistId = currentUser.getUid(); // Get pharmacist UID

        initializeViews(); // Initialize UI components
        setupToolbar(); // Set up toolbar behavior
        setupClickListeners(); // Set up button and field click listeners
    }

    private void initializeViews() { // Initialize all UI elements
        etSearchName = findViewById(R.id.et_search_medicine_name); // Search input
        btnSearch = findViewById(R.id.btn_search); // Search button
        loadingContainer = findViewById(R.id.loading_container); // Loading container
        searchResultsCard = findViewById(R.id.search_results_card); // Search results card
        detailsCard = findViewById(R.id.scroll_view_details); // Details card
        btnUpdateRecords = findViewById(R.id.btn_update_records); // Update button
        switchRequiresPrescription = findViewById(R.id.switch_requires_prescription); // Prescription switch

        recyclerViewSearchResults = findViewById(R.id.recycler_view_search_results); // RecyclerView for search
        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(this)); // Set vertical layout manager

        // Initialize included layout views for medicine details
        viewName = findViewById(R.id.et_medicine_name); // Name field container
        viewDescription = findViewById(R.id.et_description); // Description container
        viewManufacturer = findViewById(R.id.et_manufacturer); // Manufacturer container
        viewPrice = findViewById(R.id.et_price); // Price container
        viewStock = findViewById(R.id.et_stock); // Stock container

        etMedicineName = viewName.findViewById(R.id.edit_text); // Name edit text
        etDescription = viewDescription.findViewById(R.id.edit_text); // Description edit text
        etManufacturer = viewManufacturer.findViewById(R.id.edit_text); // Manufacturer edit text
        etPrice = viewPrice.findViewById(R.id.edit_text); // Price edit text
        etStock = viewStock.findViewById(R.id.edit_text); // Stock edit text

        ((TextInputLayout) viewName.findViewById(R.id.text_input_layout)).setHint("Medicine Name"); // Set hint
        ((TextInputLayout) viewDescription.findViewById(R.id.text_input_layout)).setHint("Description"); // Set hint
        ((TextInputLayout) viewManufacturer.findViewById(R.id.text_input_layout)).setHint("Manufacturer"); // Set hint
        ((TextInputLayout) viewPrice.findViewById(R.id.text_input_layout)).setHint("Price (₹)"); // Set hint
        ((TextInputLayout) viewStock.findViewById(R.id.text_input_layout)).setHint("Stock (Units)"); // Set hint

        // Set initial visibility for UI elements
        loadingContainer.setVisibility(View.GONE); // Hide loading
        searchResultsCard.setVisibility(View.GONE); // Hide search results
        detailsCard.setVisibility(View.GONE); // Hide details
    }

    private void setupToolbar() { // Toolbar setup
        MaterialToolbar toolbar = findViewById(R.id.toolbar); // Find toolbar
        setSupportActionBar(toolbar); // Set toolbar as action bar
        if (getSupportActionBar() != null) { // Check for action bar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back button
        }
        toolbar.setNavigationOnClickListener(v -> finish()); // Close activity on back button
    }

    private void setupClickListeners() { // Setup all click events
        btnSearch.setOnClickListener(v -> searchForMedicine()); // Search button click
        btnUpdateRecords.setOnClickListener(v -> validateAndUpdateData()); // Update button click

        // Setup edit buttons for each field
        setupEditButton(viewName.findViewById(R.id.btn_edit), etMedicineName); // Edit name
        setupEditButton(viewDescription.findViewById(R.id.btn_edit), etDescription); // Edit description
        setupEditButton(viewManufacturer.findViewById(R.id.btn_edit), etManufacturer); // Edit manufacturer
        setupEditButton(viewPrice.findViewById(R.id.btn_edit), etPrice); // Edit price
        setupEditButton(viewStock.findViewById(R.id.btn_edit), etStock); // Edit stock
    }

    private void setupEditButton(ImageButton button, TextInputEditText editText) { // Helper to enable editing
        button.setOnClickListener(v -> { // On click
            editText.setEnabled(true); // Enable editing
            editText.requestFocus(); // Focus field
            TextInputLayout parent = (TextInputLayout) editText.getParent().getParent(); // Get parent layout
            Toast.makeText(this, parent.getHint() + " can now be edited.", Toast.LENGTH_SHORT).show(); // Show hint
        });
    }

    private void searchForMedicine() { // Search logic
        String searchName = Objects.requireNonNull(etSearchName.getText()).toString().trim(); // Get search text
        if (TextUtils.isEmpty(searchName)) { // Check if empty
            Toast.makeText(this, "Please enter a medicine name.", Toast.LENGTH_SHORT).show(); // Show message
            return; // Exit
        }

        // Show loading, hide everything else
        loadingContainer.setVisibility(View.VISIBLE); // Show loading
        detailsCard.setVisibility(View.GONE); // Hide details
        searchResultsCard.setVisibility(View.GONE); // Hide results

        db.collection("pharmacists").document(pharmacistId) // Access Firestore collection
                .collection("my_inventory")
                .orderBy("name") // Order by name
                .startAt(searchName) // Start at search string
                .endAt(searchName + '\uf8ff') // End at search string
                .get() // Get documents
                .addOnCompleteListener(task -> { // On completion
                    loadingContainer.setVisibility(View.GONE); // Hide loading
                    if (task.isSuccessful()) { // If successful
                        if (task.getResult() == null || task.getResult().isEmpty()) { // No results
                            Toast.makeText(this, "No medicines found with that name.", Toast.LENGTH_SHORT).show(); // Show message
                        } else {
                            ArrayList<QueryDocumentSnapshot> results = new ArrayList<>(); // Create results list
                            for (QueryDocumentSnapshot document : task.getResult()) { // Iterate results
                                results.add(document); // Add document
                            }
                            displaySearchResults(results); // Display in RecyclerView
                        }
                    } else { // Error
                        Toast.makeText(this, "Error searching: " +
                                        Objects.requireNonNull(task.getException()).getMessage(),
                                Toast.LENGTH_SHORT).show(); // Show error
                    }
                });
    }

    private void displaySearchResults(ArrayList<QueryDocumentSnapshot> results) { // Display results
        adapter = new MedicineSearchUserAdapter(results, document -> { // Set adapter and click listener
            foundMedicineId = document.getId(); // Store selected medicine ID
            populateDetails(document); // Fill details
            searchResultsCard.setVisibility(View.GONE); // Hide search card
            detailsCard.setVisibility(View.VISIBLE); // Show details card
        });
        recyclerViewSearchResults.setAdapter(adapter); // Attach adapter
        searchResultsCard.setVisibility(View.VISIBLE); // Show search results
    }

    private void populateDetails(QueryDocumentSnapshot doc) { // Populate detail fields
        etMedicineName.setText(doc.getString("name")); // Set name
        etDescription.setText(doc.getString("description")); // Set description
        etManufacturer.setText(doc.getString("manufacturer")); // Set manufacturer

        // Handle null values properly
        Double priceValue = doc.getDouble("price"); // Get price
        etPrice.setText(priceValue != null ? String.valueOf(priceValue) : "0.0"); // Set or default

        Long stockValue = doc.getLong("stock"); // Get stock
        etStock.setText(stockValue != null ? String.valueOf(stockValue) : "0"); // Set or default

        switchRequiresPrescription.setChecked(Boolean.TRUE.equals(doc.getBoolean("requiresPrescription"))); // Set switch

        // Disable all fields by default after populating
        etMedicineName.setEnabled(false);
        etDescription.setEnabled(false);
        etManufacturer.setEnabled(false);
        etPrice.setEnabled(false);
        etStock.setEnabled(false);
    }

    private void validateAndUpdateData() { // Validate inputs and update Firestore
        if (TextUtils.isEmpty(foundMedicineId)) { // No selected medicine
            Toast.makeText(this, "Please search and select a medicine first.", Toast.LENGTH_SHORT).show(); // Warn user
            return; // Exit
        }

        String name = Objects.requireNonNull(etMedicineName.getText()).toString().trim(); // Get name
        String description = Objects.requireNonNull(etDescription.getText()).toString().trim(); // Get description
        String manufacturer = Objects.requireNonNull(etManufacturer.getText()).toString().trim(); // Get manufacturer
        String priceStr = Objects.requireNonNull(etPrice.getText()).toString().trim(); // Get price
        String stockStr = Objects.requireNonNull(etStock.getText()).toString().trim(); // Get stock
        boolean requiresPrescription = switchRequiresPrescription.isChecked(); // Get switch state

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) ||
                TextUtils.isEmpty(manufacturer) || TextUtils.isEmpty(priceStr) ||
                TextUtils.isEmpty(stockStr)) { // Check empty
            Toast.makeText(this, "Please ensure all fields are filled.", Toast.LENGTH_SHORT).show(); // Warn user
            return; // Exit
        }

        double price; // Initialize price
        int stock; // Initialize stock
        try {
            price = Double.parseDouble(priceStr); // Convert price
            stock = Integer.parseInt(stockStr); // Convert stock

            if (price < 0 || stock < 0) { // Check negative
                Toast.makeText(this, "Price and Stock cannot be negative.", Toast.LENGTH_SHORT).show(); // Warn
                return; // Exit
            }
        } catch (NumberFormatException e) { // Invalid numbers
            Toast.makeText(this, "Price and Stock must be valid numbers.", Toast.LENGTH_SHORT).show(); // Warn
            return; // Exit
        }

        loadingContainer.setVisibility(View.VISIBLE); // Show loading

        Map<String, Object> updatedMedicine = new HashMap<>(); // Create update map
        updatedMedicine.put("name", name); // Add name
        updatedMedicine.put("description", description); // Add description
        updatedMedicine.put("manufacturer", manufacturer); // Add manufacturer
        updatedMedicine.put("price", price); // Add price
        updatedMedicine.put("stock", stock); // Add stock
        updatedMedicine.put("requiresPrescription", requiresPrescription); // Add switch
        updatedMedicine.put("imageUrl", ""); // Placeholder image

        db.collection("pharmacists").document(pharmacistId) // Access Firestore path
                .collection("my_inventory").document(foundMedicineId) // Specific document
                .update(updatedMedicine) // Update fields
                .addOnSuccessListener(aVoid -> { // On success
                    loadingContainer.setVisibility(View.GONE); // Hide loading
                    Toast.makeText(this, "Medicine updated successfully!", Toast.LENGTH_SHORT).show(); // Show message

                    // Reset and go back
                    foundMedicineId = null; // Clear selected ID
                    detailsCard.setVisibility(View.GONE); // Hide details
                    etSearchName.setText(""); // Clear search
                    finish(); // Close activity
                })
                .addOnFailureListener(e -> { // On failure
                    loadingContainer.setVisibility(View.GONE); // Hide loading
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show(); // Show error
                });
    }
}
