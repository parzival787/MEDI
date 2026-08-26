package com.example.medi; // Define the package for the class, must match your app structure

import android.content.Context; // Needed for connectivity and system services
import android.content.Intent; // Needed for navigating between activities
import android.net.ConnectivityManager; // To check network connectivity
import android.net.Network; // *** ADDED for modern network check ***
import android.net.NetworkCapabilities; // *** ADDED for modern network check ***
import android.net.NetworkInfo; // To get active network info
import android.os.Build; // *** ADDED for API version check ***
import android.os.Bundle; // For activity state saving/restoring
import android.text.TextUtils; // For text validation
import android.util.Log; // For logging
// import android.util.Patterns; // No longer needed, using our own regex
import android.view.View; // For UI view handling
import android.widget.ProgressBar; // For showing progress/loading
import android.widget.TextView; // For displaying text
import android.widget.Toast; // For showing short messages

import androidx.annotation.Nullable; // For nullable annotation
import androidx.appcompat.app.AppCompatActivity; // Base activity class with ActionBar support

import com.google.android.gms.auth.api.signin.GoogleSignIn; // For Google sign-in
import com.google.android.gms.auth.api.signin.GoogleSignInAccount; // Google account representation
import com.google.android.gms.auth.api.signin.GoogleSignInClient; // Google sign-in client
import com.google.android.gms.auth.api.signin.GoogleSignInOptions; // Options for Google sign-in
import com.google.android.gms.common.api.ApiException; // Exception thrown by Google API
import com.google.android.gms.tasks.Task; // For async tasks
import com.google.android.material.button.MaterialButton; // Material design button
import com.google.android.material.textfield.TextInputEditText; // Material EditText
import com.google.android.material.textfield.TextInputLayout; // Wrapper for EditText with label/error

import com.google.firebase.auth.AuthCredential; // Firebase Auth credential
import com.google.firebase.auth.FirebaseAuth; // Firebase authentication
import com.google.firebase.auth.FirebaseUser; // Firebase user object
import com.google.firebase.auth.GoogleAuthProvider; // Firebase Google auth provider
import com.google.firebase.firestore.FieldValue; // Firestore server timestamp
import com.google.firebase.firestore.FirebaseFirestore; // Firestore database

import java.util.HashMap; // Map implementation
import java.util.Map; // Map interface
import java.util.regex.Pattern; // *** ADDED for Regex validation ***

// --- Activity for user sign-in and registration ---
public class UserSignin extends AppCompatActivity {

    private static final String TAG = "FirebaseAuthActivity"; // Tag for logging
    private static final int RC_SIGN_IN = 9001; // Request code for Google sign-in

    // --- Regex Patterns for Validation ---
    // Requires at least 2 letters, allowing spaces, apostrophes, and dots
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z\\s.']{2,}$");

    // Simple check for at least 5 alphanumeric/common address characters
    private static final Pattern ADDRESS_PATTERN =
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

    // --- Optional: More complex password pattern (min 8 chars, 1 upper, 1 lower, 1 num, 1 special) ---
    // private static final Pattern PASSWORD_PATTERN =
    //        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");


    // --- UI Views ---
    private TextInputLayout tilFirstName, tilAddress, tilPhone; // Layout wrappers for first name, address, phone
    private TextInputEditText etFirstName, etAddress, etPhone, etEmail, etPassword; // EditTexts for input
    private MaterialButton btnAction, btnGoogleSignIn; // Buttons for login/register and Google sign-in
    private ProgressBar progressBar; // Progress indicator
    private TextView tvToggleMode, tvTitle; // Text views for switching between login/register and title

    // --- Firebase & Google ---
    private FirebaseAuth mAuth; // Firebase authentication instance
    private FirebaseFirestore db; // Firestore database instance
    private GoogleSignInClient mGoogleSignInClient; // Google sign-in client

    // --- State Tracking ---
    private boolean isRegisterMode = true; // Track whether the user is in registration mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Call superclass implementation
        setContentView(R.layout.activity_user_signin); // Set layout XML for the activity

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth
        if (mAuth.getCurrentUser() != null) { // If user is already logged in
            navigateToDashboard(); // Go directly to dashboard
            return; // Skip the rest of onCreate
        }

        db = FirebaseFirestore.getInstance(); // Initialize Firestore database

        // --- Initialize UI Components ---
        tilFirstName = findViewById(R.id.til_first_name); // Find first name TextInputLayout
        tilAddress = findViewById(R.id.til_address); // Find address TextInputLayout
        tilPhone = findViewById(R.id.til_phone); // Find phone TextInputLayout

        etFirstName = findViewById(R.id.etFirstName); // Find first name EditText
        etAddress = findViewById(R.id.etAddress); // Find address EditText
        etPhone = findViewById(R.id.etPhone); // Find phone EditText
        etEmail = findViewById(R.id.etEmail); // Find email EditText
        etPassword = findViewById(R.id.etPassword); // Find password EditText

        btnAction = findViewById(R.id.btnAction); // Find login/register button
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn); // Find Google sign-in button
        progressBar = findViewById(R.id.progressBar); // Find progress bar
        tvToggleMode = findViewById(R.id.tvLoginLink); // Find toggle text
        tvTitle = findViewById(R.id.tv_title); // Find title text

        // --- Configure Google Sign-In ---
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Request ID token for Firebase
                .requestEmail() // Request user email
                .build(); // Build the options
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso); // Initialize Google sign-in client

        updateUI(); // Update UI based on register/login mode

        // --- Set Click Listeners ---
        btnAction.setOnClickListener(v -> handleAuthAction()); // Call auth handler when main button clicked
        tvToggleMode.setOnClickListener(v -> { // Switch between login and register modes
            isRegisterMode = !isRegisterMode; // Toggle mode
            updateUI(); // Update UI elements accordingly
        });
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle()); // Trigger Google sign-in flow
    }

    // --- Update UI according to mode ---
    private void updateUI() {
        int visibility = isRegisterMode ? View.VISIBLE : View.GONE; // Show fields only in register mode
        tilFirstName.setVisibility(visibility); // Show/hide first name
        tilAddress.setVisibility(visibility); // Show/hide address
        tilPhone.setVisibility(visibility); // Show/hide phone

        if (isRegisterMode) { // Registration mode
            tvTitle.setText("Create Account"); // Set title
            btnAction.setText("Register"); // Set button text
            tvToggleMode.setText("Already have an account? Login"); // Set toggle text
        } else { // Login mode
            tvTitle.setText("Login"); // Set title
            btnAction.setText("Login"); // Set button text
            tvToggleMode.setText("Don't have an account? Register"); // Set toggle text
        }

        // Clear errors when toggling
        etFirstName.setError(null);
        etAddress.setError(null);
        etPhone.setError(null);
        etEmail.setError(null);
        etPassword.setError(null);
    }

    // --- Handle login or registration button click ---
    private void handleAuthAction() {
        if (!isNetworkAvailable()) { // Check if device is online
            Toast.makeText(this, "An internet connection is required.", Toast.LENGTH_LONG).show();
            return; // Abort if no internet
        }

        // *** MODIFIED: Use new consolidated validation ***
        if (!validateInputs()) {
            progressBar.setVisibility(View.GONE); // Hide progress bar if validation fails
            return; // Stop if validation fails
        }

        progressBar.setVisibility(View.VISIBLE); // Show loading indicator

        // Get validated inputs
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (isRegisterMode) { // Registration logic
            String firstName = etFirstName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            // *** REMOVED: Old validation logic is now in validateInputs() ***

            // Create Firebase user with email and password
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> { // Handle async result
                        if (task.isSuccessful()) { // If successful
                            FirebaseUser user = mAuth.getCurrentUser(); // Get current user
                            if (user != null) {
                                saveUserProfile(user.getUid(), firstName, address, phone, email); // Save profile in Firestore
                            }
                        } else { // If failure
                            handleAuthFailure(task.getException()); // Handle error
                        }
                    });
        } else { // Login logic
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> { // Handle async result
                        if (task.isSuccessful()) { // If login successful
                            Toast.makeText(UserSignin.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            navigateToDashboard(); // Navigate to main dashboard
                        } else { // If failure
                            handleAuthFailure(task.getException()); // Handle error
                        }
                    });
        }
    }

    // --- Save user profile to Firestore ---
    private void saveUserProfile(String userId, String firstName, String address, String phone, String email) {
        Map<String, Object> userProfile = new HashMap<>(); // Create map for user profile
        userProfile.put("firstName", firstName); // Add first name
        userProfile.put("address", address); // Add address
        userProfile.put("phone", phone); // Add phone
        userProfile.put("email", email); // Add email
        userProfile.put("createdAt", FieldValue.serverTimestamp()); // Add server timestamp
        userProfile.put("userType", "customer"); // Add user type

        db.collection("users").document(userId).set(userProfile) // Save to Firestore
                .addOnSuccessListener(aVoid -> { // On success
                    Toast.makeText(UserSignin.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    navigateToDashboard(); // Navigate to dashboard
                })
                .addOnFailureListener(e -> { // On failure
                    Log.w(TAG, "Error saving profile", e); // Log error
                    Toast.makeText(UserSignin.this, "Failed to save profile.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE); // Hide progress bar
                });
    }

    // --- GOOGLE SIGN-IN METHODS ---
    private void signInWithGoogle() {
        if (!isNetworkAvailable()) { // Check network
            Toast.makeText(this, "An internet connection is required.", Toast.LENGTH_SHORT).show();
            return; // Abort if offline
        }
        progressBar.setVisibility(View.VISIBLE); // Show progress bar
        Intent signInIntent = mGoogleSignInClient.getSignInIntent(); // Get Google sign-in intent
        startActivityForResult(signInIntent, RC_SIGN_IN); // Start Google sign-in activity
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { // Handle activity result
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) { // If Google sign-in
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data); // Get account
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class); // Get account or throw exception
                firebaseAuthWithGoogle(account.getIdToken()); // Authenticate with Firebase
            } catch (ApiException e) { // Handle exception
                handleAuthFailure(e); // Show error
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) { // Firebase Google auth
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null); // Get credential
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> { // Handle async result
                    if (task.isSuccessful()) { // If successful
                        FirebaseUser user = mAuth.getCurrentUser(); // Get Firebase user
                        boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser(); // Check if new
                        if (isNewUser && user != null) { // If first-time Google login
                            // For Google Sign-in, address and phone are not provided, so save with empty strings
                            saveUserProfile(user.getUid(), user.getDisplayName(), "", "", user.getEmail()); // Save profile
                        } else {
                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show(); // Existing user
                            navigateToDashboard();
                        }
                    } else { // If failure
                        handleAuthFailure(task.getException());
                    }
                });
    }

    // --- UTILITY AND NAVIGATION ---
    private void handleAuthFailure(Exception exception) { // Display error
        Log.w(TAG, "Authentication Failed", exception); // Log warning
        Toast.makeText(UserSignin.this, "Authentication failed: " + exception.getMessage(), Toast.LENGTH_LONG).show(); // Show message
        progressBar.setVisibility(View.GONE); // Hide progress bar
    }

    private void navigateToDashboard() { // Open main dashboard activity
        Intent intent = new Intent(UserSignin.this, UserActivity.class);
        startActivity(intent); // Start activity
        finish(); // Close current activity
    }

    // *** REMOVED: Old validateEmailAndPassword method ***

    // *** ADDED: New consolidated validation method using Regex ***
    private boolean validateInputs() {
        // Clear previous errors
        etEmail.setError(null);
        etPassword.setError(null);
        if (isRegisterMode) {
            etFirstName.setError(null);
            etAddress.setError(null);
            etPhone.setError(null);
        }

        // Get all inputs
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // --- Validate Registration-Specific Fields ---
        if (isRegisterMode) {
            if (TextUtils.isEmpty(firstName) || !NAME_PATTERN.matcher(firstName).matches()) {
                etFirstName.setError("Enter a valid name (at least 2 letters)");
                etFirstName.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(address) || !ADDRESS_PATTERN.matcher(address).matches()) {
                etAddress.setError("Enter a valid address (at least 5 characters)");
                etAddress.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(phone) || !PHONE_PATTERN.matcher(phone).matches()) {
                etPhone.setError("Enter a valid 10-digit Indian mobile number");
                etPhone.requestFocus();
                return false;
            }
        }

        // --- Validate Common Fields (Email & Password) ---
        if (TextUtils.isEmpty(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password) || !PASSWORD_PATTERN.matcher(password).matches()) {
            etPassword.setError("Password must be at least 6 non-whitespace characters");
            // If using the complex pattern, change the error message:
            // etPassword.setError("Min 8 chars, 1 upper, 1 lower, 1 num, 1 special char");
            etPassword.requestFocus();
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