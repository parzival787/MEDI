package com.example.medi; // Defines the package name where this class belongs — must match the app's structure.

// --- Importing necessary Android and Firebase classes ---
import android.os.Bundle; // Used to pass data between activities or save activity state.
import android.text.TextUtils; // Provides utility methods for working with text (like checking if empty).
import android.view.View; // Used for controlling visibility and handling UI elements.
import android.widget.ProgressBar; // UI component that shows a loading spinner.
import android.widget.Toast; // Used to display small pop-up messages to the user.

// Unused imports for Uri, Intent, and Storage were removed since image upload was deleted.

import androidx.appcompat.app.AppCompatActivity; // Base class for all activities using modern Android design.

import com.google.android.material.appbar.MaterialToolbar; // Toolbar widget from Material Design.
import com.google.android.material.button.MaterialButton; // Button widget with Material styling.
import com.google.android.material.switchmaterial.SwitchMaterial; // On/Off switch from Material Design.
import com.google.android.material.textfield.TextInputEditText; // Input field with Material Design look.
import com.google.firebase.auth.FirebaseAuth; // Used for Firebase user authentication.
import com.google.firebase.auth.FirebaseUser; // Represents the currently logged-in Firebase user.
import com.google.firebase.firestore.FirebaseFirestore; // Firebase Firestore database for storing data.

import java.util.HashMap; // Provides a map-based data structure for key-value pairs.
import java.util.Map; // Interface implemented by HashMap.

// --- The main Activity class ---
public class addmedicine extends AppCompatActivity {

    // --- UI Components ---
    private TextInputEditText etMedicineName, etDescription, etManufacturer, etPrice, etStock; // Input fields for medicine details.
    private SwitchMaterial switchRequiresPrescription; // Switch to indicate if a prescription is needed.
    private MaterialButton btnSaveMedicine; // Button to save medicine data.
    private ProgressBar progressBar; // Loading indicator shown while saving data.

    // --- Firebase Components ---
    private FirebaseFirestore db; // Firestore database reference.
    private FirebaseAuth mAuth; // Firebase Authentication instance.

    // Activity lifecycle method — runs when the screen is first created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Calls the parent class’s onCreate method.
        setContentView(R.layout.activity_addmedicine); // Loads the layout XML file for this activity.

        // --- Initialize Firebase instances ---
        db = FirebaseFirestore.getInstance(); // Connect to Firestore database.
        mAuth = FirebaseAuth.getInstance(); // Get Firebase authentication instance.

        // --- Setup Toolbar ---
        MaterialToolbar toolbar = findViewById(R.id.toolbar); // Get the toolbar from the layout.
        setSupportActionBar(toolbar); // Set it as the app's top bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the back arrow.
        toolbar.setNavigationOnClickListener(v -> finish()); // When the arrow is pressed, close this screen.

        // Initialize all input fields and buttons.
        initializeViews();

        // Set up button click actions.
        setupClickListeners();
    }

    // --- Finds and links all the views from XML layout ---
    private void initializeViews() {
        etMedicineName = findViewById(R.id.et_medicine_name); // Get reference to medicine name field.
        etDescription = findViewById(R.id.et_description); // Get reference to description field.
        etManufacturer = findViewById(R.id.et_manufacturer); // Get reference to manufacturer field.
        etPrice = findViewById(R.id.et_price); // Get reference to price field.
        etStock = findViewById(R.id.et_stock); // Get reference to stock field.
        switchRequiresPrescription = findViewById(R.id.switch_requires_prescription); // Get reference to prescription switch.
        btnSaveMedicine = findViewById(R.id.btn_save_medicine); // Get reference to save button.
        progressBar = findViewById(R.id.progressBar); // Get reference to progress bar.
    }

    // --- Assigns what happens when buttons are clicked ---
    private void setupClickListeners() {
        // The image upload feature was removed, so we only handle the Save button.
        btnSaveMedicine.setOnClickListener(v -> validateAndSaveData()); // When clicked, validate inputs and save data.
    }

    // --- Validates user input and sends data to Firestore if all fields are correct ---
    private void validateAndSaveData() {
        // Get text from all input fields and remove any extra spaces.
        String name = etMedicineName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String manufacturer = etManufacturer.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        boolean requiresPrescription = switchRequiresPrescription.isChecked(); // True if switch is ON.

        // Check if any field is empty.
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) ||
                TextUtils.isEmpty(manufacturer) || TextUtils.isEmpty(priceStr) ||
                TextUtils.isEmpty(stockStr)) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show(); // Show message to fill all fields.
            return; // Stop execution until user fills all fields.
        }

        // Try to convert price and stock to numbers.
        double price;
        int stock;
        try {
            price = Double.parseDouble(priceStr); // Convert price text to a decimal number.
            stock = Integer.parseInt(stockStr); // Convert stock text to an integer.
        } catch (NumberFormatException e) { // If conversion fails (invalid number format).
            Toast.makeText(this, "Please enter valid numbers for price and stock", Toast.LENGTH_SHORT).show();
            return; // Stop execution if invalid numbers entered.
        }

        // Show loading spinner while saving data.
        progressBar.setVisibility(View.VISIBLE);

        // Save data to Firestore database.
        saveDataToFirestore(name, description, manufacturer, price, stock, requiresPrescription);
    }

    // --- Actually saves the validated data to Firestore database ---
    private void saveDataToFirestore(String name, String description, String manufacturer,
                                     double price, int stock, boolean requiresPrescription) {
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get currently logged-in Firebase user.
        if (currentUser == null) { // If user is not logged in.
            Toast.makeText(this, "Error: You must be logged in.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE); // Hide loading spinner.
            return;
        }

        // Get the pharmacist’s unique ID from Firebase Authentication.
        String pharmacistId = currentUser.getUid();

        // Create a map (key-value pairs) for storing medicine data.
        Map<String, Object> medicine = new HashMap<>();
        medicine.put("name", name);
        medicine.put("description", description);
        medicine.put("manufacturer", manufacturer);
        medicine.put("price", price);
        medicine.put("stock", stock);
        medicine.put("requiresPrescription", requiresPrescription);
        medicine.put("imageUrl", ""); // Image feature removed, so we save an empty string for image URL.

        // Save the data into Firestore under pharmacist’s inventory.
        db.collection("pharmacists").document(pharmacistId) // Access the pharmacist’s document.
                .collection("my_inventory").document() // Create a new document inside “my_inventory”.
                .set(medicine) // Upload the medicine data.
                .addOnSuccessListener(aVoid -> { // Runs if upload is successful.
                    progressBar.setVisibility(View.GONE); // Hide progress bar.
                    Toast.makeText(addmedicine.this, "Medicine added successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the current screen and go back.
                })
                .addOnFailureListener(e -> { // Runs if there’s an error while saving.
                    progressBar.setVisibility(View.GONE); // Hide progress bar.
                    Toast.makeText(addmedicine.this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
