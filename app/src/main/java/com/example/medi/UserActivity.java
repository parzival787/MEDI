package com.example.medi; // Define the package where this activity belongs

import android.content.Context; // Import Context class for system services like InputMethodManager
import android.content.Intent; // Import Intent to start new activities
import android.os.Bundle; // Import Bundle for activity state
import android.text.Editable; // Import Editable for text editing (not used here but typical for TextWatcher)
import android.text.TextWatcher; // Import TextWatcher interface (not directly used here)
import android.util.Log; // Import Log for logging debug/warning/error messages
import android.view.Menu; // Import Menu class to handle app bar menus
import android.view.MenuInflater; // Import MenuInflater to inflate menu XML
import android.view.MenuItem; // Import MenuItem to detect clicked menu items
import android.view.View; // Import View for UI elements
import android.view.inputmethod.EditorInfo; // Import EditorInfo to handle IME actions like search
import android.view.inputmethod.InputMethodManager; // Import InputMethodManager to hide/show keyboard
import android.widget.LinearLayout; // Import LinearLayout UI container
import android.widget.TextView; // Import TextView UI element
import android.widget.Toast; // Import Toast for short messages to user

import androidx.annotation.NonNull; // Import annotation to mark non-null parameters
import androidx.appcompat.app.AppCompatActivity; // Import base class for activities with AppCompat features
import androidx.recyclerview.widget.LinearLayoutManager; // Import layout manager for RecyclerView
import androidx.recyclerview.widget.RecyclerView; // Import RecyclerView for listing items

import com.google.android.material.appbar.MaterialToolbar; // Import MaterialToolbar for top app bar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton; // Import extended FAB
import com.google.android.material.textfield.TextInputEditText; // Import Material text input
import com.google.android.material.textfield.TextInputLayout; // Import wrapper for TextInputEditText
import com.google.firebase.auth.FirebaseAuth; // Import Firebase Auth
import com.google.firebase.auth.FirebaseUser; // Import Firebase User object
import com.google.firebase.firestore.FirebaseFirestore; // Import Firestore database
import com.google.firebase.firestore.Query; // Import Firestore query class
import com.google.firebase.firestore.QueryDocumentSnapshot; // Import Firestore query result snapshot

import java.util.ArrayList; // Import ArrayList for dynamic lists
import java.util.Locale; // Import Locale (not used here)
import java.util.Objects; // Import Objects utility for null-safe operations

public class UserActivity extends AppCompatActivity { // Define the main activity class

    private static final String TAG = "UserActivity_DEBUG"; // Tag for logging

    // --- UI Views ---
    private TextInputLayout searchInputLayout; // Layout wrapper for search EditText
    private TextInputEditText etSearch; // Search input field
    private RecyclerView recyclerViewMedicines; // RecyclerView to show search results
    private LinearLayout emptyStateLayout; // Layout to show when no results
    private TextView tvWelcomeUser; // TextView to show welcome message
    private ExtendedFloatingActionButton fabCart; // Floating action button for cart
    private View cardUploadPrescription; // Card view to upload prescription
    private View cardMyOrders; // Card view to navigate to orders

    // --- Firebase & Adapters ---
    private FirebaseFirestore db; // Firestore database instance
    private MedicineSearchUserAdapter adapter; // Adapter for RecyclerView
    private FirebaseAuth mAuth; // Firebase authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Called when activity is created
        super.onCreate(savedInstanceState); // Call parent constructor
        setContentView(R.layout.activity_user); // Inflate the activity layout

        Log.d(TAG, "onCreate: Activity starting."); // Debug log

        db = FirebaseFirestore.getInstance(); // Initialize Firestore
        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth

        initializeViews(); // Find all UI elements
        setupToolbar(); // Setup toolbar for menu actions
        setupUser(); // Display welcome message based on current user
        setupSearch(); // Setup search input and actions
        setupClickListeners(); // Setup clicks for FAB and cards

        recyclerViewMedicines.setVisibility(View.GONE); // Hide RecyclerView initially
        emptyStateLayout.setVisibility(View.VISIBLE); // Show empty state initially
        Log.d(TAG, "onCreate: Initial UI state set."); // Debug log
    }

    // --- Inflate the menu for the Toolbar ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // Called to inflate menu
        MenuInflater inflater = getMenuInflater(); // Get menu inflater
        inflater.inflate(R.menu.user_menu, menu); // Inflate menu XML into Toolbar
        Log.d(TAG, "onCreateOptionsMenu: Menu inflated."); // Debug log
        return true; // Return true to display menu
    }

    // --- Handle clicks on menu items ---
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { // Called when a menu item is clicked
        if (item.getItemId() == R.id.action_logout) { // Check if logout item clicked
            Log.d(TAG, "Logout menu item selected."); // Debug log
            logoutUser(); // Call logout function
            return true; // Indicate click was handled
        }
        return super.onOptionsItemSelected(item); // Default handling for other items
    }

    private void initializeViews() { // Find all UI elements by ID
        Log.d(TAG, "initializeViews: Finding UI elements."); // Debug log
        tvWelcomeUser = findViewById(R.id.tv_welcome_user); // TextView for welcome
        searchInputLayout = findViewById(R.id.search_input_layout); // Layout for search
        etSearch = findViewById(R.id.et_search); // Search EditText
        recyclerViewMedicines = findViewById(R.id.recycler_view_medicines); // RecyclerView
        emptyStateLayout = findViewById(R.id.empty_state_layout); // Empty state layout
        fabCart = findViewById(R.id.fab_cart); // Cart FAB
        cardUploadPrescription = findViewById(R.id.card_upload_prescription); // Prescription card
        cardMyOrders = findViewById(R.id.card_my_orders); // Orders card
        recyclerViewMedicines.setLayoutManager(new LinearLayoutManager(this)); // Set vertical list layout
        Log.d(TAG, "initializeViews: UI elements found."); // Debug log
    }

    private void setupToolbar() { // Setup toolbar as action bar
        MaterialToolbar toolbar = findViewById(R.id.toolbar); // Find toolbar view
        setSupportActionBar(toolbar); // Set as activity action bar
        Log.d(TAG, "setupToolbar: Toolbar setup complete."); // Debug log
    }

    private void setupUser() { // Setup welcome message for user
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get logged-in user
        if (currentUser != null) { // If user exists
            String displayName = currentUser.getDisplayName(); // Get display name
            String email = currentUser.getEmail(); // Get email
            String nameToDisplay = "User"; // Default name

            if (displayName != null && !displayName.isEmpty()) { // Use display name if available
                nameToDisplay = displayName;
            } else if (email != null && !email.isEmpty()) { // Otherwise use email prefix
                nameToDisplay = email.split("@")[0];
            }
            String welcomeText = "Hello, " + nameToDisplay + "!"; // Create welcome message
            tvWelcomeUser.setText(welcomeText); // Set TextView
            Log.d(TAG, "setupUser: Welcome message set for user: " + currentUser.getUid()); // Debug log
        } else { // If no user
            tvWelcomeUser.setText("Hello!"); // Generic greeting
            Log.w(TAG, "setupUser: Current user is NULL."); // Warning log
        }
    }

    private void setupClickListeners() { // Setup all button and card click actions
        fabCart.setOnClickListener(v -> { // FAB click listener
            Log.d(TAG, "Cart FAB clicked."); // Debug log
            startActivity(new Intent(UserActivity.this, CartActivity.class)); // Open CartActivity
        });

        cardUploadPrescription.setOnClickListener(v -> { // Prescription card click
            Log.d(TAG, "Upload Prescription card clicked."); // Debug log
            startActivity(new Intent(UserActivity.this, Uploadprescription.class)); // Open upload activity
        });

        cardMyOrders.setOnClickListener(v -> { // Orders card click
            Log.d(TAG, "My Orders card clicked."); // Debug log
            startActivity(new Intent(UserActivity.this, MyOrdersActivity.class)); // Open orders activity
        });

        Log.d(TAG, "setupClickListeners: General click listeners (FAB, Card) set."); // Debug log
    }

    private void setupSearch() { // Setup search input and button
        searchInputLayout.setEndIconOnClickListener(v -> { // End icon clicked
            Log.d(TAG, "Search end icon clicked."); // Debug log
            performSearch(); // Trigger search
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> { // Handle keyboard search action
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { // If search action pressed
                Log.d(TAG, "Keyboard search action detected."); // Debug log
                performSearch(); // Trigger search
                return true; // Consume action
            }
            return false; // Pass other actions
        });

        Log.d(TAG, "setupSearch: Search button/action listeners added."); // Debug log
    }

    private void performSearch() { // Perform search on Firestore
        String query = Objects.requireNonNull(etSearch.getText()).toString().trim(); // Get trimmed search text
        Log.d(TAG, "performSearch: Attempting search for query='" + query + "'"); // Debug log
        hideKeyboard(); // Hide soft keyboard

        if (query.isEmpty()) { // If query is empty
            Log.d(TAG, "performSearch: Query is empty, showing empty state."); // Debug log
            recyclerViewMedicines.setVisibility(View.GONE); // Hide list
            emptyStateLayout.setVisibility(View.VISIBLE); // Show empty state
            if (adapter != null) { // Clear previous results
                adapter.updateData(new ArrayList<>());
            }
        } else { // If query has text
            searchMedicines(query); // Call Firestore search
        }
    }

    private void hideKeyboard() { // Hide soft keyboard
        View view = this.getCurrentFocus(); // Get current focused view
        if (view != null) { // If view exists
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); // Get input manager
            if (imm != null) { // Hide keyboard
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                Log.d(TAG, "hideKeyboard: Keyboard hidden."); // Debug log
            } else {
                Log.w(TAG, "hideKeyboard: InputMethodManager is null."); // Warning log
            }
            view.clearFocus(); // Clear focus
        } else { // No focus found
            Log.d(TAG, "hideKeyboard: No current focus found, clearing focus from search EditText."); // Debug log
            etSearch.clearFocus(); // Clear search field focus
        }
    }

    private void searchMedicines(String query) { // Search medicines in Firestore
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get current user
        if (currentUser == null) { // If user is null
            Log.e(TAG, "searchMedicines: User is NULL. Aborting search."); // Error log
            Toast.makeText(this, "Error: Not signed in.", Toast.LENGTH_SHORT).show(); // Notify user
            emptyStateLayout.setVisibility(View.VISIBLE); // Show empty state
            recyclerViewMedicines.setVisibility(View.GONE); // Hide list
            if (adapter != null) adapter.updateData(new ArrayList<>()); // Clear adapter
            return; // Exit method
        }
        Log.d(TAG, "searchMedicines: Starting Firestore query for '" + query + "' (Case-Sensitive) by user " + currentUser.getUid()); // Debug log

        emptyStateLayout.setVisibility(View.GONE); // Hide empty state
        recyclerViewMedicines.setVisibility(View.VISIBLE); // Show RecyclerView

        Query firestoreQuery = db.collectionGroup("my_inventory") // Query all "my_inventory" subcollections
                .orderBy("name") // Order by "name" field
                .startAt(query) // Start at query string
                .endAt(query + '\uf8ff') // End at query string (prefix search)
                .limit(15); // Limit to 15 results

        firestoreQuery.get() // Execute query
                .addOnCompleteListener(task -> { // Handle result
                    if (task.isSuccessful() && task.getResult() != null) { // Success
                        ArrayList<QueryDocumentSnapshot> results = new ArrayList<>(); // Store results
                        for (QueryDocumentSnapshot doc : task.getResult()) { // Iterate results
                            results.add(doc); // Add document
                        }
                        Log.d(TAG, "searchMedicines: Firestore query successful. Found " + results.size() + " documents."); // Debug log
                        updateSearchResults(results); // Update UI
                    } else { // Failure
                        Log.e(TAG, "searchMedicines: Firestore query FAILED.", task.getException()); // Error log
                        Toast.makeText(UserActivity.this, "Search failed. Check logs.", Toast.LENGTH_SHORT).show(); // Notify user
                        emptyStateLayout.setVisibility(View.VISIBLE); // Show empty state
                        recyclerViewMedicines.setVisibility(View.GONE); // Hide list
                        if (adapter != null) adapter.updateData(new ArrayList<>()); // Clear adapter
                    }
                });
    }

    private void updateSearchResults(ArrayList<QueryDocumentSnapshot> documents) { // Update RecyclerView
        Log.d(TAG, "updateSearchResults: Updating adapter with " + documents.size() + " items."); // Debug log

        boolean hasResults = !documents.isEmpty(); // Check if there are results
        emptyStateLayout.setVisibility(hasResults ? View.GONE : View.VISIBLE); // Show/hide empty state
        recyclerViewMedicines.setVisibility(hasResults ? View.VISIBLE : View.GONE); // Show/hide list

        if (adapter == null) { // If adapter not initialized
            if (hasResults) { // Only create if results exist
                Log.d(TAG, "updateSearchResults: Creating new adapter."); // Debug log
                adapter = new MedicineSearchUserAdapter(documents, document -> { // Create adapter with click listener
                    Log.d(TAG, "Search item clicked: " + document.getId() + " - " + document.getString("name")); // Debug log
                    String medId = document.getId(); // Medicine document ID
                    String name = document.getString("name"); // Medicine name
                    Double price = document.getDouble("price"); // Medicine price
                    if (price == null) price = 0.0; // Default to 0 if null

                    Long stockLong = document.getLong("stock"); // Get stock from Firestore
                    int stock = (stockLong != null) ? stockLong.intValue() : 0; // Default 0 if null

                    if (stock <= 0) { // If out of stock
                        Toast.makeText(UserActivity.this, "This item is out of stock.", Toast.LENGTH_SHORT).show(); // Notify user
                        return; // Exit
                    }

                    String pharmacistId = document.getReference().getParent().getParent().getId(); // Get parent pharmacist ID

                    CartItem item = new CartItem(medId, name, price, 1, pharmacistId, stock); // Create CartItem

                    MedicineDetailsBottomSheet bottomSheet = MedicineDetailsBottomSheet.newInstance(item); // Create bottom sheet
                    try {
                        Log.d(TAG, "Showing bottom sheet for: " + name); // Debug log
                        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag()); // Show bottom sheet
                    } catch (IllegalStateException e) { // Catch error
                        Log.e(TAG, "Error showing bottom sheet:", e); // Log error
                        Toast.makeText(this, "Could not show details.", Toast.LENGTH_SHORT).show(); // Notify user
                    }
                });
                recyclerViewMedicines.setAdapter(adapter); // Attach adapter
            }
        } else { // If adapter exists
            Log.d(TAG, "updateSearchResults: Updating existing adapter data."); // Debug log
            adapter.updateData(documents); // Update adapter data
        }
    }

    // --- NEW Logout Function ---
    private void logoutUser() { // Log user out
        mAuth.signOut(); // Sign out from Firebase Auth
        Log.d(TAG, "User signed out."); // Debug log

        Intent intent = new Intent(UserActivity.this, MainActivity.class); // Prepare intent to MainActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent); // Start MainActivity
        finish(); // Finish current activity
        Log.d(TAG, "Navigated back to MainActivity."); // Debug log
    }
}
