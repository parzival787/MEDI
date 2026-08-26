package com.example.medi; // Declares the package this class belongs to

// Import statements bring in required Android and Firebase classes
import android.view.LayoutInflater; // Used to inflate XML layouts into View objects
import android.view.View; // Represents a single UI element
import android.view.ViewGroup; // Represents a group of Views (like RecyclerView)
import android.widget.Button; // UI button widget
import android.widget.TextView; // UI text widget
import android.widget.Toast; // Small popup messages

import androidx.annotation.NonNull; // Annotation to prevent null values
import androidx.recyclerview.widget.RecyclerView; // For RecyclerView list functionality

import com.google.firebase.firestore.DocumentSnapshot; // Single Firestore document
import com.google.firebase.firestore.FirebaseFirestore; // Firebase database instance
import com.google.firebase.firestore.QueryDocumentSnapshot; // Document returned in a query

import java.util.HashMap; // Key-value mapping
import java.util.List; // List interface
import java.util.Map; // Map interface
import java.util.Locale; // Locale for formatting numbers/dates

// This class is a RecyclerView adapter to show OCR-scanned medicine results
public class OcrResultsAdapter extends RecyclerView.Adapter<OcrResultsAdapter.ViewHolder> {

    private final List<QueryDocumentSnapshot> medicineDocs; // List of Firestore documents representing medicines
    private final CartManager cartManager; // Singleton object to manage the shopping cart
    private final Map<String, String> pharmacistNameCache = new HashMap<>(); // Cache pharmacist names to avoid repeated Firestore calls

    // Constructor: takes the list of medicines
    public OcrResultsAdapter(List<QueryDocumentSnapshot> medicineDocs) {
        this.medicineDocs = medicineDocs; // Save the list locally
        this.cartManager = CartManager.getInstance(); // Get the singleton instance of CartManager
    }

    // Called when RecyclerView needs to create a new row
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the XML layout for a single row
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_ocr_result, parent, false);
        return new ViewHolder(view); // Wrap it in our ViewHolder and return
    }

    // Called to display data at a given position
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QueryDocumentSnapshot doc = medicineDocs.get(position); // Get the medicine document at this position

        String medName = doc.getString("name"); // Get medicine name from Firestore
        Double price = doc.getDouble("price"); // Get medicine price
        String medId = doc.getId(); // Get Firestore document ID
        // Get pharmacist ID from the parent path of this document
        String pharmacistId = doc.getReference().getParent().getParent().getId();

        // Get stock quantity from Firestore
        Long stockLong = doc.getLong("stock");
        int stock = (stockLong != null) ? stockLong.intValue() : 0; // Default to 0 if null

        // Set medicine name and price in the UI
        holder.tvMedicineName.setText(medName);
        holder.tvMedicinePrice.setText(String.format(Locale.getDefault(), "₹%.2f", price != null ? price : 0.0));

        // Initially show placeholder text for pharmacist
        holder.tvPharmacistName.setText("Sold by: Loading...");

        // Show stock info and disable button if out of stock
        if (stock <= 0) {
            holder.tvStock.setText("Out of Stock"); // Show "Out of Stock" in UI
            holder.btnAddCart.setEnabled(false); // Disable the add to cart button
            holder.btnAddCart.setText("Out of Stock"); // Update button text
        } else {
            holder.tvStock.setText(String.format(Locale.getDefault(), "%d units available", stock)); // Show available units
            holder.btnAddCart.setEnabled(true); // Enable button
            holder.btnAddCart.setText("Add to Cart"); // Button text
        }

        // If pharmacist name is cached, use it; otherwise fetch from Firestore
        if (pharmacistNameCache.containsKey(pharmacistId)) {
            holder.tvPharmacistName.setText("Sold by: " + pharmacistNameCache.get(pharmacistId));
        } else {
            fetchPharmacistName(pharmacistId, holder.tvPharmacistName); // Fetch asynchronously
        }

        // Handle Add to Cart button click
        holder.btnAddCart.setOnClickListener(v -> {
            if (medName != null && price != null) { // Ensure valid data
                // Create a CartItem object including stock
                CartItem item = new CartItem(medId, medName, price, 1, pharmacistId, stock);

                // Add item to cart; returns true if successful
                boolean success = cartManager.addToCart(item);

                if (success) {
                    // Show confirmation toast
                    Toast.makeText(holder.itemView.getContext(), medName + " added to cart", Toast.LENGTH_SHORT).show();
                } else {
                    // Show stock limit reached toast
                    Toast.makeText(holder.itemView.getContext(), "Stock limit reached!", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Show error if data is missing
                Toast.makeText(holder.itemView.getContext(), "Error adding item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch pharmacist name from Firestore
    private void fetchPharmacistName(String pharmacistId, TextView textView) {
        FirebaseFirestore.getInstance().collection("pharmacists").document(pharmacistId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) { // Check if document exists
                        String name = documentSnapshot.getString("name"); // Get pharmacist name
                        if (name != null) {
                            pharmacistNameCache.put(pharmacistId, name); // Cache the name
                            textView.setText("Sold by: " + name); // Update UI
                        } else {
                            textView.setText("Sold by: Unknown Pharmacy"); // Default text if name missing
                        }
                    } else {
                        textView.setText("Sold by: Unknown Pharmacy"); // Default if document missing
                    }
                })
                .addOnFailureListener(e -> textView.setText("Sold by: Error loading name")); // Error case
    }

    @Override
    public int getItemCount() {
        return medicineDocs.size(); // Return number of items in the list
    }

    // ViewHolder holds references to UI elements in a single row
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicineName, tvPharmacistName, tvMedicinePrice, tvStock; // Text fields
        Button btnAddCart; // Add to cart button

        ViewHolder(View view) {
            super(view);
            tvMedicineName = view.findViewById(R.id.tv_ocr_medicine_name); // Medicine name TextView
            tvPharmacistName = view.findViewById(R.id.tv_ocr_pharmacist_name); // Pharmacist name TextView
            tvMedicinePrice = view.findViewById(R.id.tv_ocr_medicine_price); // Medicine price TextView
            btnAddCart = view.findViewById(R.id.btn_ocr_add_to_cart); // Add to cart button
            tvStock = view.findViewById(R.id.tv_ocr_stock); // Stock quantity TextView
        }
    }
}
