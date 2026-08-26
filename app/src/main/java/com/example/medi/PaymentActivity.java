package com.example.medi;
// Defines the package of the app where this class resides.

import android.app.Activity; // Import Activity class (needed to reference the current screen/activity)
import android.content.Intent; // Import Intent class (used for navigating between activities)
import android.os.Bundle; // Import Bundle (used to pass data when activity is created)
import android.util.Log; // Import Log (used for printing debug/error messages)
import android.widget.Button; // Import Button widget
import android.widget.TextView; // Import TextView widget
import android.widget.Toast; // Import Toast (small pop-up messages)

import androidx.annotation.NonNull; // Import annotation for non-null values
import androidx.appcompat.app.AppCompatActivity; // Import base activity class with AppCompat support

import com.google.android.material.appbar.MaterialToolbar; // Import Material Toolbar (top app bar)
import com.google.firebase.auth.FirebaseAuth; // Firebase Authentication
import com.google.firebase.auth.FirebaseUser; // Firebase user object
import com.google.firebase.firestore.DocumentReference; // Reference to a Firestore document
import com.google.firebase.firestore.DocumentSnapshot; // Snapshot of Firestore document
import com.google.firebase.firestore.FieldValue; // For special Firestore fields like server timestamp
import com.google.firebase.firestore.FirebaseFirestore; // Firestore database instance
import com.google.firebase.firestore.WriteBatch; // For performing multiple Firestore writes atomically
import com.razorpay.Checkout; // Razorpay checkout integration
import com.razorpay.PaymentResultListener; // Interface to listen to payment results

import org.json.JSONObject; // For creating JSON objects for Razorpay

import java.util.ArrayList; // ArrayList utility
import java.util.HashMap; // HashMap utility
import java.util.List; // List interface
import java.util.Locale; // For formatting currency properly
import java.util.Map; // Map interface
import java.util.Objects; // Utility for null-safe operations

// The activity implements PaymentResultListener to handle Razorpay payment callbacks
public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {

    private static final String TAG = "PaymentActivity"; // Tag for logging, useful in Logcat

    private TextView tvPaymentTotal; // Displays total payment amount
    private Button btnPayNow; // Button to initiate payment

    private double totalAmount = 0.0; // Variable to store the total amount to pay
    private String razorpayApiKey ="rzp_test_RYZljXLqktBse6"; // Razorpay test API key (replace with real key in production)

    private FirebaseFirestore db; // Firestore database instance
    private FirebaseAuth mAuth; // Firebase authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Call parent constructor
        setContentView(R.layout.activity_payment); // Set the XML layout for this screen

        db = FirebaseFirestore.getInstance(); // Initialize Firestore
        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth

        Checkout.preload(getApplicationContext()); // Preload Razorpay checkout to make it faster
        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0.0);
        // Get total amount from previous activity, default to 0.0 if not sent

        setupToolbar(); // Setup toolbar with back button
        initializeViews(); // Initialize TextView and Button references
        setupClickListeners(); // Set the onClick listener for payment button

        tvPaymentTotal.setText(String.format(Locale.getDefault(), "₹%.2f", totalAmount));
        // Display total amount in TextView formatted as Indian Rupees
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar_payment); // Get toolbar from XML
        setSupportActionBar(toolbar); // Set it as the app bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Complete Payment"); // Set toolbar title
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back button
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        // Handle back button click, go back to previous screen
    }

    private void initializeViews() {
        tvPaymentTotal = findViewById(R.id.tv_payment_total); // Connect TextView to XML
        btnPayNow = findViewById(R.id.btn_pay_now); // Connect Button to XML
    }

    private void setupClickListeners() {
        btnPayNow.setOnClickListener(v -> startPayment());
        // When Pay Now button is clicked, call startPayment()
    }

    private void startPayment() {
        if (totalAmount <= 0) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            // Show error if amount is zero or negative
            return;
        }

        final Activity activity = this; // Current activity
        final Checkout checkout = new Checkout(); // Create Razorpay checkout object
        checkout.setKeyID(razorpayApiKey); // Set Razorpay API key

        int amountInPaise = (int) (totalAmount * 100);
        // Convert rupees to paise as Razorpay expects amount in smallest currency unit

        try {
            JSONObject options = new JSONObject(); // Create JSON object with payment options
            options.put("name", "Medi App"); // Merchant name
            options.put("description", "Medicine Order Payment"); // Payment description
            options.put("theme.color", "#01579B"); // Toolbar color
            options.put("currency", "INR"); // Currency
            options.put("amount", String.valueOf(amountInPaise)); // Amount to pay

            JSONObject prefill = new JSONObject(); // Prefill customer info
            if(mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
                prefill.put("email", mAuth.getCurrentUser().getEmail());
                // Prefill email
                // prefill.put("contact", "USER_PHONE_NUMBER"); // Optional phone prefill
                options.put("prefill", prefill); // Add prefill to options
            }

            checkout.open(activity, options); // Open Razorpay checkout screen

        } catch (Exception e) {
            Log.e(TAG, "Error in starting Razorpay Checkout", e);
            // Log error in case payment initiation fails
            Toast.makeText(activity, "Error initiating payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Show toast to user
        }
    }

    // --- Razorpay payment success callback ---
    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        Log.d(TAG, "Payment Successful: " + razorpayPaymentID); // Log success
        Toast.makeText(this, "Payment Successful! Saving order...", Toast.LENGTH_LONG).show();
        // Notify user

        fetchUserDetailsAndSaveOrders(razorpayPaymentID);
        // Start saving order details to Firestore
    }

    // --- Razorpay payment failure callback ---
    @Override
    public void onPaymentError(int code, String description) {
        Log.e(TAG, "Payment failed: " + code + " " + description); // Log failure
        Toast.makeText(this, "Payment Failed: " + description, Toast.LENGTH_LONG).show();
        // Notify user
    }

    /**
     * Fetch user's name and phone from Firestore and proceed to save orders.
     */
    private void fetchUserDetailsAndSaveOrders(String razorpayPaymentID) {
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get currently logged in user
        if (currentUser == null) {
            Log.e(TAG, "Cannot save order, user is not logged in."); // Log error
            Toast.makeText(this, "Error: You are not logged in.", Toast.LENGTH_LONG).show();
            return; // Stop if no user is logged in
        }
        String userId = currentUser.getUid(); // Get user ID

        // Get user's profile document from "users" collection
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot userDoc = task.getResult();

                        String userName = userDoc.getString("firstName"); // Get first name
                        String userPhone = userDoc.getString("phone"); // Get phone

                        if (userName == null || userPhone == null || userName.isEmpty() || userPhone.isEmpty()) {
                            // If profile is incomplete, show error and log
                            Log.e(TAG, "User profile is incomplete. Missing name or phone.");
                            Log.e(TAG, "Found name: " + userName);
                            Log.e(TAG, "Found phone: " + userPhone);
                            Toast.makeText(this, "Error: Your profile is incomplete (missing name or phone). Cannot place order.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // If profile is complete, save orders
                        saveOrdersWithBatch(razorpayPaymentID, userId, userName, userPhone);

                    } else {
                        // Failed to fetch user document
                        Log.e(TAG, "CRITICAL: Failed to fetch user profile after payment.", task.getException());
                        Toast.makeText(this, "Payment Succeeded, but failed to fetch your profile! Contact support.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Save order details for both user and pharmacists using a batch (all-or-nothing write)
     */
    private void saveOrdersWithBatch(String razorpayPaymentID, String userId, String userName, String userPhone) {
        List<CartItem> cartItems = CartManager.getInstance().getCartItems(); // Get current cart items
        if (cartItems == null || cartItems.isEmpty()) {
            Log.e(TAG, "Cannot save order, cart is empty.");
            return; // Stop if cart is empty
        }

        // --- Group items by pharmacist ---
        Map<String, List<Map<String, Object>>> pharmacistOrders = new HashMap<>();
        List<Map<String, Object>> userOrderItems = new ArrayList<>();

        for (CartItem item : cartItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("medicineId", item.getMedicineId());
            itemMap.put("medicineName", item.getName());
            itemMap.put("price", item.getPrice());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("pharmacistId", item.getPharmacistId());

            userOrderItems.add(itemMap); // Add to user's order

            String pharmId = item.getPharmacistId();
            if (!pharmacistOrders.containsKey(pharmId)) {
                pharmacistOrders.put(pharmId, new ArrayList<>()); // Create list if first item for this pharmacist
            }
            Objects.requireNonNull(pharmacistOrders.get(pharmId)).add(itemMap); // Add item to pharmacist's list
        }

        // --- Create a Firestore batch write ---
        WriteBatch batch = db.batch();

        // --- Create user order document ---
        Map<String, Object> userOrderData = new HashMap<>();
        userOrderData.put("userId", userId);
        userOrderData.put("paymentId", razorpayPaymentID);
        userOrderData.put("totalAmount", this.totalAmount);
        userOrderData.put("status", "Placed"); // Initial status
        userOrderData.put("orderTimestamp", FieldValue.serverTimestamp()); // Server time
        userOrderData.put("items", userOrderItems); // Items list

        DocumentReference userOrderRef = db.collection("users").document(userId)
                .collection("orders").document(); // Create a new document reference
        batch.set(userOrderRef, userOrderData); // Add to batch

        // --- Create pharmacist orders ---
        for (Map.Entry<String, List<Map<String, Object>>> entry : pharmacistOrders.entrySet()) {
            String pharmacistId = entry.getKey();
            List<Map<String, Object>> pharmacistItems = entry.getValue();

            double pharmacistTotal = 0;
            for (Map<String, Object> item : pharmacistItems) {
                int quantity = 1;
                Object qtyObj = item.get("quantity");
                if (qtyObj instanceof Long) quantity = ((Long) qtyObj).intValue();
                else if (qtyObj instanceof Integer) quantity = (Integer) qtyObj;

                pharmacistTotal += (Double) item.get("price") * quantity;
            }

            Map<String, Object> pharmOrderData = new HashMap<>();
            pharmOrderData.put("userId", userId);
            pharmOrderData.put("userName", userName);
            pharmOrderData.put("userPhone", userPhone);
            pharmOrderData.put("paymentId", razorpayPaymentID);
            pharmOrderData.put("status", "Placed");
            pharmOrderData.put("orderTimestamp", FieldValue.serverTimestamp());
            pharmOrderData.put("items", pharmacistItems);
            pharmOrderData.put("totalAmount", pharmacistTotal);
            pharmOrderData.put("userOrderRefId", userOrderRef.getId());
            // Link to user's order document

            DocumentReference pharmOrderRef = db.collection("pharmacists").document(pharmacistId)
                    .collection("ordersReceived").document(); // New document for pharmacist
            batch.set(pharmOrderRef, pharmOrderData); // Add to batch
        }

        // --- Commit batch write ---
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Batch write successful! All orders saved.");
                    CartManager.getInstance().clearCart(); // Clear cart after order
                    Intent intent = new Intent(PaymentActivity.this, UserActivity.class);
                    // Navigate to UserActivity
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    // Clear previous activities
                    startActivity(intent);
                    finish(); // Finish this activity
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "CRITICAL ERROR: Payment Succeeded but FAILED to save batch orders!", e);
                    Toast.makeText(this, "Payment Succeeded, but failed to save order! Please contact support.", Toast.LENGTH_LONG).show();
                });
    }
}
