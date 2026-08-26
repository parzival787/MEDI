package com.example.medi; // Defines the package where this class belongs

import com.google.android.material.bottomsheet.BottomSheetDialogFragment; // Allows creating a bottom sheet dialog UI
import android.os.Bundle; // Used for saving and restoring state
import android.util.Log; // For logging debug/error messages
import android.view.LayoutInflater; // Inflates XML layouts into View objects
import android.view.View; // Represents UI components
import android.view.ViewGroup; // Container for holding views
import android.widget.TextView; // For displaying text on screen
import android.widget.Toast; // For showing short pop-up messages

import androidx.annotation.NonNull; // Annotation for non-null parameters
import androidx.annotation.Nullable; // Annotation for nullable parameters

import com.google.android.material.button.MaterialButton; // Material Design button
import com.google.firebase.firestore.DocumentSnapshot; // Represents a Firestore document
import com.google.firebase.firestore.FirebaseFirestore; // Firestore database reference

import java.util.Locale; // For formatting numbers and currency properly
import java.util.Objects; // For safely handling null values

public class MedicineDetailsBottomSheet extends BottomSheetDialogFragment { // Defines a bottom sheet that shows medicine details

    private static final String TAG = "MedDetailsBottomSheet"; // Tag used for logging
    private static final String ARG_MEDICINE_DATA = "medicine_data"; // Key for passing medicine data between components

    private CartItem cartItem; // Stores the selected medicine item passed from UserActivity

    private MaterialButton btnAddToCart; // Button to add the medicine to the shopping cart

    // Factory method to create a new bottom sheet instance with data
    public static MedicineDetailsBottomSheet newInstance(CartItem item) { // Used to create a new instance of the fragment
        MedicineDetailsBottomSheet fragment = new MedicineDetailsBottomSheet(); // Create new fragment object
        Bundle args = new Bundle(); // Bundle used to pass arguments
        args.putParcelable(ARG_MEDICINE_DATA, item); // Put the CartItem (which implements Parcelable)
        fragment.setArguments(args); // Attach arguments to the fragment
        return fragment; // Return configured fragment
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) { // Called when fragment is created
        super.onCreate(savedInstanceState);
        if (getArguments() != null) { // Check if arguments were passed
            cartItem = getArguments().getParcelable(ARG_MEDICINE_DATA); // Retrieve CartItem from bundle
            if (cartItem == null) { // Handle null safety
                Log.e(TAG, "CartItem received in onCreate is null!"); // Log error if item missing
            }
        } else { // No arguments were passed
            Log.e(TAG, "Arguments are null in onCreate!"); // Log error
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // Called to create the layout for the bottom sheet
        return inflater.inflate(R.layout.activity_medicine_details_bottom_sheet, container, false); // Inflate XML layout for this bottom sheet
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { // Called once the view hierarchy is created
        super.onViewCreated(view, savedInstanceState);

        // --- Find UI components from the layout ---
        TextView tvName = view.findViewById(R.id.tv_medicine_name_detail); // Medicine name
        TextView tvPharmacist = view.findViewById(R.id.tv_pharmacist_name_detail); // Pharmacist name
        TextView tvDescription = view.findViewById(R.id.tv_description_detail); // Medicine description
        TextView tvManufacturer = view.findViewById(R.id.tv_manufacturer_detail); // Manufacturer info
        TextView tvStock = view.findViewById(R.id.tv_stock_detail); // Stock availability
        TextView tvPrice = view.findViewById(R.id.tv_price_detail); // Price
        btnAddToCart = view.findViewById(R.id.btn_add_to_cart_detail); // "Add to Cart" button

        if (cartItem != null) { // Ensure we have valid item data
            tvName.setText(cartItem.getName()); // Show medicine name
            tvPrice.setText(String.format(Locale.getDefault(), "INR %.2f", cartItem.getPrice())); // Show price in INR

            fetchFullDetails(tvPharmacist, tvDescription, tvManufacturer, tvStock); // Load more details from Firestore

            // Handle Add to Cart button click
            btnAddToCart.setOnClickListener(v -> {
                Log.d(TAG, "Add to Cart clicked for: " + cartItem.getName()); // Log the click
                boolean success = CartManager.getInstance().addToCart(cartItem); // Try adding to cart (returns boolean)

                if (success) { // If added successfully
                    Toast.makeText(getContext(), cartItem.getName() + " added to cart", Toast.LENGTH_SHORT).show(); // Show success message
                    dismiss(); // Close the bottom sheet
                } else { // If stock limit reached or already in cart
                    Toast.makeText(getContext(), "Stock limit reached for this item!", Toast.LENGTH_SHORT).show(); // Show error message
                    // Do not dismiss, so the user can see the warning
                }
            });

        } else { // cartItem is null
            Log.e(TAG, "cartItem is null in onViewCreated, cannot populate details."); // Log error
            Toast.makeText(getContext(), "Error: Could not load medicine details.", Toast.LENGTH_SHORT).show(); // Show error message
            dismiss(); // Close bottom sheet to avoid broken UI
        }
    }

    /**
     * Fetches pharmacy name and detailed medicine info from Firestore.
     */
    private void fetchFullDetails(TextView tvPharmacist, TextView tvDescription, TextView tvManufacturer, TextView tvStock) { // Fetches extra details from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance(); // Get Firestore database instance

        // --- 1. Fetch Pharmacist / Pharmacy Name ---
        db.collection("pharmacists").document(cartItem.getPharmacistId()) // Get pharmacist document using pharmacist ID
                .get()
                .addOnSuccessListener(documentSnapshot -> { // If Firestore call succeeds
                    if (documentSnapshot.exists()) { // Check if document actually exists

                        String pharmacyName = documentSnapshot.getString("pharmacyName"); // Try getting pharmacy name
                        if (pharmacyName == null || pharmacyName.isEmpty()) { // If missing, fallback to pharmacist name
                            pharmacyName = documentSnapshot.getString("name");
                        }

                        tvPharmacist.setText("Sold by: " + Objects.requireNonNullElse(pharmacyName, "Unknown Pharmacy")); // Display name or default
                        Log.d(TAG, "Fetched pharmacy name: " + pharmacyName); // Log pharmacy name

                    } else { // If pharmacist document not found
                        tvPharmacist.setText("Sold by: Unknown Pharmacy"); // Default text
                        Log.w(TAG, "Pharmacist document not found: " + cartItem.getPharmacistId()); // Log warning
                    }
                })
                .addOnFailureListener(e -> { // If error occurs during fetch
                    tvPharmacist.setText("Sold by: Error loading name"); // Show fallback text
                    Log.e(TAG, "Error fetching pharmacist name", e); // Log error
                });

        // --- 2. Fetch Full Medicine Details ---
        db.collection("pharmacists").document(cartItem.getPharmacistId()) // Access the pharmacist’s inventory
                .collection("my_inventory").document(cartItem.getMedicineId()) // Get medicine document
                .get()
                .addOnSuccessListener(documentSnapshot -> { // If fetch succeeds
                    if(documentSnapshot.exists()){ // If medicine data found
                        String description = documentSnapshot.getString("description"); // Get description
                        String manufacturer = documentSnapshot.getString("manufacturer"); // Get manufacturer
                        Long stockLong = documentSnapshot.getLong("stock"); // Get stock count as Long
                        long stock = (stockLong != null) ? stockLong : 0; // Convert safely to long

                        // Update the cart item with latest stock info
                        cartItem = new CartItem(
                                cartItem.getMedicineId(),
                                cartItem.getName(),
                                cartItem.getPrice(),
                                cartItem.getQuantity(), // Usually 1
                                cartItem.getPharmacistId(),
                                (int) stock // Convert long to int
                        );

                        // Disable Add to Cart button if out of stock
                        if (stock <= 0) {
                            btnAddToCart.setEnabled(false); // Disable button
                            btnAddToCart.setText("Out of Stock"); // Update text
                            tvStock.setText("0 units (Out of Stock)"); // Show stock message
                        } else {
                            btnAddToCart.setEnabled(true); // Enable button
                            btnAddToCart.setText("Add to Cart"); // Reset text
                            tvStock.setText(String.format(Locale.getDefault(), "%d units", stock)); // Display stock count
                        }

                        // Show description and manufacturer details
                        tvDescription.setText(Objects.requireNonNullElse(description, "No description available.")); // Default if missing
                        tvManufacturer.setText(Objects.requireNonNullElse(manufacturer, "N/A")); // Default if missing
                        Log.d(TAG, "Fetched medicine details - Desc: " + description + ", Manu: " + manufacturer + ", Stock: " + stock); // Log success

                    } else { // If no medicine data found
                        Log.w(TAG, "Medicine inventory document not found: " + cartItem.getPharmacistId() + "/" + cartItem.getMedicineId()); // Log warning
                        tvDescription.setText("Details not found."); // Show fallback text
                        tvManufacturer.setText("N/A");
                        tvStock.setText("N/A");
                        btnAddToCart.setEnabled(false); // Disable add to cart
                        btnAddToCart.setText("Unavailable"); // Indicate unavailability
                    }
                })
                .addOnFailureListener(e -> { // Handle any Firestore fetch errors
                    Log.e(TAG, "Error fetching medicine details", e); // Log error
                    tvDescription.setText("Error loading details."); // Show fallback text
                    tvManufacturer.setText("Error");
                    tvStock.setText("Error");
                    btnAddToCart.setEnabled(false); // Disable button
                    btnAddToCart.setText("Error"); // Indicate issue
                });
    }
}
