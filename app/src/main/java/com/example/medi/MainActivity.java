
package com.example.medi; // Defines the package this class belongs to

import android.os.Bundle; // Used to handle saved instance states in Android activities
import androidx.appcompat.app.AppCompatActivity; // Base class for modern Android activities
import android.content.Intent; // Used for navigation between activities
import android.util.Log; // Used for logging debug information
import android.view.View; // Base class for all UI components
import com.google.android.material.card.MaterialCardView; // For using Material Design card views

import com.google.firebase.auth.FirebaseAuth; // Firebase authentication library
import com.google.firebase.auth.FirebaseUser; // Represents the currently logged-in Firebase user
import com.google.firebase.firestore.FirebaseFirestore; // Firestore database instance

public class MainActivity extends AppCompatActivity { // Main entry activity where user selects their role

    private static final String TAG = "MainActivity"; // Tag for logging debug information


    private FirebaseAuth mAuth; // Firebase Authentication instance
    private FirebaseFirestore db; // Firestore database reference

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Called when activity is first created
        super.onCreate(savedInstanceState); // Call parent method to handle default setup
        setContentView(R.layout.activity_main); // Set the layout for this activity

        // --- ADD THESE ---
        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Authentication
        db = FirebaseFirestore.getInstance(); // Initialize Firestore database
        // --- END ADD ---

        MaterialCardView userCard = findViewById(R.id.card_user); // Reference to User card from XML
        MaterialCardView pharmacistCard = findViewById(R.id.card_pharmacist); // Reference to Pharmacist card

        userCard.setOnClickListener(new View.OnClickListener() { // When the user taps the "User" card
            @Override
            public void onClick(View v) { // Handle click event
                Intent intent = new Intent(MainActivity.this, UserSignin.class); // Create intent to go to UserSignin screen
                startActivity(intent); // Start UserSignin activity
                // Do not call finish() here so the user can press Back to return
            }
        });

        pharmacistCard.setOnClickListener(new View.OnClickListener() { // When the user taps the "Pharmacist" card
            @Override
            public void onClick(View v) { // Handle click event
                Intent intent = new Intent(MainActivity.this, PharmacySignin.class); // Create intent to go to PharmacySignin screen
                startActivity(intent); // Start PharmacySignin activity
                // Do not call finish() here for the same reason
            }
        });
    }

    // --- ADD THIS ENTIRE onStart() METHOD ---
    @Override
    protected void onStart() { // Called every time the activity becomes visible
        super.onStart(); // Call the superclass method
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get currently logged-in Firebase user

        if (currentUser != null) { // If a user is already logged in
            Log.d(TAG, "User " + currentUser.getUid() + " is already logged in. Checking type..."); // Log info
            checkUserType(currentUser.getUid()); // Determine if they are a pharmacist or a regular user
        } else { // If no user is logged in
            Log.d(TAG, "No user logged in. Showing role selection screen."); // Show the role selection UI
        }
    }

    // --- ADD THIS METHOD to check Firestore ---
    private void checkUserType(String uid) { // Check which type of user is logged in
        db.collection("pharmacists").document(uid).get() // Look in the "pharmacists" collection
                .addOnSuccessListener(doc -> { // If data is successfully retrieved
                    if (doc.exists()) { // If a document exists, the user is a pharmacist
                        Log.d(TAG, "User is a Pharmacist. Redirecting to PharmacistActivity..."); // Log success
                        redirectToDashboard(PharmacistActivity.class); // Open Pharmacist dashboard
                    } else { // If not found in pharmacists
                        checkIfRegularUser(uid); // Then check the "users" collection
                    }
                })
                .addOnFailureListener(e -> { // Handle any errors from Firestore
                    Log.e(TAG, "Failed to check pharmacists collection", e); // Log the error
                    mAuth.signOut(); // Log the user out for safety
                });
    }


    private void checkIfRegularUser(String uid) { // Check if the user is a regular app user
        db.collection("users").document(uid).get() // Look in the "users" collection
                .addOnSuccessListener(userDoc -> { // On successful read
                    if (userDoc.exists()) { // If the document exists
                        Log.d(TAG, "User is a regular User. Redirecting to UserActivity..."); // Log result
                        redirectToDashboard(UserActivity.class); // Open User dashboard
                    } else { // If document doesn't exist
                        Log.w(TAG, "User logged in but has no DB record in /users or /pharmacists. Logging out."); // Log warning
                        mAuth.signOut(); // Log them out since Firestore entry is missing
                    }
                })
                .addOnFailureListener(e -> { // Handle Firestore failure
                    Log.e(TAG, "Failed to check users collection", e); // Log the error
                    mAuth.signOut(); // Log the user out for safety
                });
    }


    private void redirectToDashboard(Class<?> activityClass) { // Used to open the correct dashboard
        Intent intent = new Intent(MainActivity.this, activityClass); // Create intent for the target activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack and start fresh
        startActivity(intent); // Launch the new dashboard activity
        finish(); // Close MainActivity so user can’t go back to it
    }
}
