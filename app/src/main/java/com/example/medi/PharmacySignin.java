package com.example.medi;                                        // Defines the package for this class

import android.content.Context;                                    // Import Context to access system services
import android.content.Intent;                                     // Import Intent to navigate between screens
import android.net.ConnectivityManager;                            // Import to check network connectivity
import android.net.Network;                                        // *** ADDED for modern network check ***
import android.net.NetworkCapabilities;                            // *** ADDED for modern network check ***
import android.net.NetworkInfo;                                     // Import to get details of active network
import android.os.Build;                                            // *** ADDED for API version check ***
import android.os.Bundle;                                           // Import Bundle for Activity state management
import android.text.TextUtils;                                      // Import TextUtils for easy string checks
import android.util.Log;                                            // Import Log for debug messages
// import android.util.Patterns;                                   // No longer needed, using our own regex
import android.view.View;                                           // Import View for UI components
import android.widget.LinearLayout;                                 // Import LinearLayout container
import android.widget.ProgressBar;                                  // Import ProgressBar UI element
import android.widget.TextView;                                     // Import TextView UI element
import android.widget.Toast;                                        // Import Toast for short messages

import androidx.appcompat.app.AppCompatActivity;                   // Import AppCompatActivity for modern Activity features

import com.google.android.material.button.MaterialButton;           // Import MaterialButton from Material Design
import com.google.android.material.switchmaterial.SwitchMaterial;   // Import SwitchMaterial for toggle switch
import com.google.android.material.textfield.TextInputEditText;    // Import editable text fields
import com.google.android.material.textfield.TextInputLayout;      // Import TextInputLayout for Material Design input wrapper
import com.google.firebase.auth.FirebaseAuth;                       // Import FirebaseAuth to manage authentication
import com.google.firebase.auth.FirebaseUser;                       // Import FirebaseUser to get current user
import com.google.firebase.firestore.FirebaseFirestore;             // Import Firestore to store user data
import com.google.firebase.firestore.GeoPoint;                      // Import GeoPoint for storing latitude & longitude

import java.util.HashMap;                                           // Import HashMap to store key-value pairs
import java.util.Map;                                               // Import Map interface
import java.util.regex.Pattern;                                   // *** ADDED for Regex validation ***

public class PharmacySignin extends AppCompatActivity {             // Activity for pharmacist login and registration

    private static final String TAG = "PharmacyAuthActivity";       // Tag for logging

    // --- Regex Patterns for Validation ---
    // Requires at least 3 letters, allowing spaces, numbers, and common symbols
    private static final Pattern PHARMACY_NAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9\\s.&'-]{3,}$");

    // Requires at least 2 letters, allowing spaces, apostrophes, and dots
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z\\s.']{2,}$");

    // Allows 5-20 alphanumeric characters and dashes (common for license plates/IDs)
    private static final Pattern LICENSE_NUMBER_PATTERN =
            Pattern.compile("^[A-Za-z0-9-]{5,20}$");

    // Simple check for at least 5 alphanumeric/common address characters
    private static final Pattern LOCATION_PATTERN =
            Pattern.compile("^[A-Za-z0-9\\s.,#-]{5,}$");

    // Indian Mobile Number: Starts with 6, 7, 8, or 9, followed by 9 digits
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[6-9]\\d{9}$");

    // Standard Email Pattern
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    // Password: min 6 chars, no whitespace
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=\\S+$).{6,}$");


    // --- UI Views ---
    private TextInputLayout tilPharmacyName, tilPharmacistName, tilLicenseNumber, tilLocation, tilPhone, tilLatitude, tilLongitude; // TextInputLayouts for input fields
    private LinearLayout layoutGeopoint;                             // Layout container for latitude & longitude fields
    private TextInputEditText etPharmacyName, etPharmacistName, etLicenseNumber, etLocation, etPhone, etLatitude, etLongitude, etPharmacistEmail, etPharmacistPassword; // Editable input fields
    private SwitchMaterial switchIsOpen;                             // Switch to indicate if pharmacy is open
    private MaterialButton btnPharmacistAction;                      // Button to register or login
    private ProgressBar progressBar;                                  // Progress bar to show loading
    private TextView tvToggleMode, tvTitle;                           // TextView for switching modes and title

    // --- Firebase ---
    private FirebaseAuth mAuth;                                      // Firebase Authentication instance
    private FirebaseFirestore db;                                     // Firestore instance

    // --- State Tracking ---
    private boolean isRegisterMode = true;                           // Flag to check if user is in registration mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {             // Called when Activity is created
        super.onCreate(savedInstanceState);                           // Call parent constructor
        setContentView(R.layout.activity_pharmacy_signin);           // Set layout for this Activity

        mAuth = FirebaseAuth.getInstance();                           // Initialize Firebase Authentication
        db = FirebaseFirestore.getInstance();                         // Initialize Firestore

        // --- Initialize All UI Components ---
        tvTitle = findViewById(R.id.tv_title);                        // Link title TextView
        progressBar = findViewById(R.id.progressBar);                 // Link ProgressBar

        // Layouts for visibility toggling
        tilPharmacyName = findViewById(R.id.til_pharmacy_name);       // Link Pharmacy Name layout
        tilPharmacistName = findViewById(R.id.til_pharmacist_name);   // Link Pharmacist Name layout
        tilLicenseNumber = findViewById(R.id.til_license_number);     // Link License Number layout
        tilLocation = findViewById(R.id.til_location);                // Link Location layout
        tilPhone = findViewById(R.id.til_phone);                      // Link Phone layout
        layoutGeopoint = findViewById(R.id.layout_geopoint);          // Link GeoPoint layout (latitude/longitude)
        // *** ADDED: Link lat/lon text input layouts for setting errors ***
        tilLatitude = findViewById(R.id.til_latitude);
        tilLongitude = findViewById(R.id.til_longitude);

        // Input Fields
        etPharmacyName = findViewById(R.id.etPharmacyName);           // Link Pharmacy Name input
        etPharmacistName = findViewById(R.id.etPharmacistName);       // Link Pharmacist Name input
        etLicenseNumber = findViewById(R.id.etLicenseNumber);         // Link License Number input
        etLocation = findViewById(R.id.etLocation);                   // Link Location input
        etPhone = findViewById(R.id.etPhone);                         // Link Phone input
        etLatitude = findViewById(R.id.etLatitude);                   // Link Latitude input
        etLongitude = findViewById(R.id.etLongitude);                 // Link Longitude input
        etPharmacistEmail = findViewById(R.id.etPharmacistEmail);     // Link Email input
        etPharmacistPassword = findViewById(R.id.etPharmacistPassword); // Link Password input

        // Switch, Buttons and Links
        switchIsOpen = findViewById(R.id.switchIsOpen);               // Link switch to indicate open/closed
        btnPharmacistAction = findViewById(R.id.btnPharmacistRegister); // Link action button
        tvToggleMode = findViewById(R.id.tvPharmacistLoginLink);      // Link toggle text to switch login/register

        updateUI();                                                    // Update UI elements based on mode

        btnPharmacistAction.setOnClickListener(v -> handleAuthAction()); // Set click listener for register/login
        tvToggleMode.setOnClickListener(v -> {                         // Set click listener to toggle modes
            isRegisterMode = !isRegisterMode;                          // Toggle boolean flag
            updateUI();                                                // Update UI after toggle
        });
    }

    private void updateUI() {                                          // Updates visibility and text based on mode
        int visibility = isRegisterMode ? View.VISIBLE : View.GONE;    // Show fields in registration, hide in login

        tilPharmacyName.setVisibility(visibility);                     // Show/hide Pharmacy Name field
        tilPharmacistName.setVisibility(visibility);                   // Show/hide Pharmacist Name field
        tilLicenseNumber.setVisibility(visibility);                    // Show/hide License Number field
        tilLocation.setVisibility(visibility);                          // Show/hide Location field
        tilPhone.setVisibility(visibility);                             // Show/hide Phone field
        layoutGeopoint.setVisibility(visibility);                       // Show/hide Latitude & Longitude fields
        switchIsOpen.setVisibility(visibility);                          // Show/hide Open/Closed switch

        if (isRegisterMode) {                                           // Set UI text for registration
            tvTitle.setText("Pharmacist Registration");                 // Set title
            btnPharmacistAction.setText("Create Pharmacy Account");     // Set button text
            tvToggleMode.setText("Already registered? Login Here");     // Set toggle link
        } else {                                                        // Set UI text for login
            tvTitle.setText("Pharmacist Login");                        // Set title
            btnPharmacistAction.setText("Login");                       // Set button text
            tvToggleMode.setText("New here? Register your Pharmacy");   // Set toggle link
        }

        // *** ADDED: Clear errors when toggling ***
        clearAllErrors();
    }

    private void handleAuthAction() {                                   // Handles login or registration when button clicked
        // --- 1. Validate All Inputs ---
        // *** MODIFIED: Use new consolidated validation method ***
        if (!validateInputs()) {
            return; // Stop if validation fails
        }

        // --- 2. Check Network ---
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "An internet connection is required.", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String email = etPharmacistEmail.getText().toString().trim();   // Get email from input
        String password = etPharmacistPassword.getText().toString().trim(); // Get password from input

        if (isRegisterMode) {                                           // Registration mode
            // Get all string values
            String pharmacyName = etPharmacyName.getText().toString().trim();
            String pharmacistName = etPharmacistName.getText().toString().trim();
            String licenseNumber = etLicenseNumber.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String latStr = etLatitude.getText().toString().trim();
            String lonStr = etLongitude.getText().toString().trim();
            boolean isOpen = switchIsOpen.isChecked();

            // *** REMOVED: Old validation logic is now in validateInputs() ***

            // --- 3. Parse and Validate Latitude/Longitude ---
            double latitude, longitude;
            try {
                latitude = Double.parseDouble(latStr);
                longitude = Double.parseDouble(lonStr);

                // *** ADDED: Range checking for coordinates ***
                if (latitude < -90.0 || latitude > 90.0) {
                    tilLatitude.setError("Latitude must be between -90 and 90");
                    etLatitude.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (longitude < -180.0 || longitude > 180.0) {
                    tilLongitude.setError("Longitude must be between -180 and 180");
                    etLongitude.requestFocus();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

            } catch (NumberFormatException e) {
                // This check is still useful for empty strings or invalid characters
                tilLatitude.setError("Invalid number format");
                tilLongitude.setError("Invalid number format");
                progressBar.setVisibility(View.GONE);
                return;
            }

            // --- 4. Create User in Firebase Auth ---
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                GeoPoint geoPoint = new GeoPoint(latitude, longitude); // Create GeoPoint
                                savePharmacistProfile(user.getUid(), email, geoPoint, isOpen, licenseNumber, location, pharmacistName, pharmacyName, phone); // Save profile
                            }
                        } else {
                            handleAuthFailure(task.getException());
                        }
                    });

        } else {                                                             // Login mode
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(PharmacySignin.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            navigateToPharmacistDashboard();
                        } else {
                            handleAuthFailure(task.getException());
                        }
                    });
        }
    }

    private void savePharmacistProfile(String userId, String email, GeoPoint geopoint, boolean isopen, String licenseNumber, String location, String pharmacistName, String pharmacyName, String phone) { // Saves pharmacist data to Firestore
        Map<String, Object> profile = new HashMap<>();
        profile.put("email", email);
        profile.put("geopoint", geopoint);
        profile.put("isopen", isopen);
        profile.put("licenseNumber", licenseNumber);
        profile.put("location", location);
        profile.put("pharmacistName", pharmacistName);
        profile.put("pharmacyName", pharmacyName);
        profile.put("phone", phone);
        profile.put("userType", "pharmacist");

        db.collection("pharmacists").document(userId).set(profile)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PharmacySignin.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    navigateToPharmacistDashboard();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error saving profile to Firestore", e);
                    Toast.makeText(PharmacySignin.this, "Failed to save profile.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void handleAuthFailure(Exception exception) {
        Log.w(TAG, "Authentication Failed", exception);
        Toast.makeText(PharmacySignin.this, "Authentication failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.GONE);
    }

    private void navigateToPharmacistDashboard() {
        Intent intent = new Intent(PharmacySignin.this, PharmacistActivity.class);
        startActivity(intent);
        finish();
    }

    // *** REMOVED: Old validateEmailAndPassword method ***

    // *** ADDED: Helper method to clear all errors ***
    private void clearAllErrors() {
        // Use TextInputLayouts to clear errors, as it's the standard way
        tilPharmacyName.setError(null);
        tilPharmacistName.setError(null);
        tilLicenseNumber.setError(null);
        tilLocation.setError(null);
        tilPhone.setError(null);
        tilLatitude.setError(null);
        tilLongitude.setError(null);
        // Also clear errors from the EditTexts themselves
        etPharmacistEmail.setError(null);
        etPharmacistPassword.setError(null);
    }

    // *** ADDED: New consolidated validation method using Regex ***
    private boolean validateInputs() {
        clearAllErrors(); // Clear previous errors first

        // Get all text inputs
        String email = etPharmacistEmail.getText().toString().trim();
        String password = etPharmacistPassword.getText().toString().trim();

        // --- Validate Registration-Specific Fields ---
        if (isRegisterMode) {
            String pharmacyName = etPharmacyName.getText().toString().trim();
            String pharmacistName = etPharmacistName.getText().toString().trim();
            String licenseNumber = etLicenseNumber.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String latStr = etLatitude.getText().toString().trim();
            String lonStr = etLongitude.getText().toString().trim();

            if (TextUtils.isEmpty(pharmacyName) || !PHARMACY_NAME_PATTERN.matcher(pharmacyName).matches()) {
                tilPharmacyName.setError("Enter a valid pharmacy name (at least 3 chars)");
                etPharmacyName.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(pharmacistName) || !NAME_PATTERN.matcher(pharmacistName).matches()) {
                tilPharmacistName.setError("Enter a valid name (at least 2 letters)");
                etPharmacistName.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(licenseNumber) || !LICENSE_NUMBER_PATTERN.matcher(licenseNumber).matches()) {
                tilLicenseNumber.setError("Enter a valid license number (5-20 chars, A-Z, 0-9, -)");
                etLicenseNumber.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(location) || !LOCATION_PATTERN.matcher(location).matches()) {
                tilLocation.setError("Enter a valid address (at least 5 characters)");
                etLocation.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(phone) || !PHONE_PATTERN.matcher(phone).matches()) {
                tilPhone.setError("Enter a valid 10-digit Indian mobile number");
                etPhone.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(latStr)) {
                tilLatitude.setError("Latitude is required");
                etLatitude.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(lonStr)) {
                tilLongitude.setError("Longitude is required");
                etLongitude.requestFocus();
                return false;
            }
        }

        // --- Validate Common Fields (Email & Password) ---
        if (TextUtils.isEmpty(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            etPharmacistEmail.setError("Enter a valid email address");
            etPharmacistEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password) || !PASSWORD_PATTERN.matcher(password).matches()) {
            etPharmacistPassword.setError("Password must be at least 6 non-whitespace characters");
            etPharmacistPassword.requestFocus();
            return false;
        }

        return true; // All validations passed
    }

    // *** MODIFIED: Modernized network check ***
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Use NetworkCapabilities for API 23 (Marshmallow) and higher
            Network network = cm.getActiveNetwork();
            if (network == null) return false;
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        } else {
            // Use deprecated getActiveNetworkInfo for API 22 and lower
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
    }
}



