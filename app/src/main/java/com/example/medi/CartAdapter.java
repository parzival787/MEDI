package com.example.medi; // Defines the package this class belongs to

import android.annotation.SuppressLint; // Used to suppress specific Android lint warnings
import android.content.Context; // Provides access to app-specific resources and classes
import android.view.LayoutInflater; // Used to convert XML layout files into View objects
import android.view.View; // Base class for all UI elements
import android.view.ViewGroup; // A container that holds multiple views
import android.widget.ImageButton; // Button with an image instead of text
import android.widget.TextView; // Widget to display text
import android.widget.Toast; // For displaying small popup messages to the user

import androidx.annotation.NonNull; // Indicates a parameter or return value should not be null
import androidx.recyclerview.widget.RecyclerView; // Widget to efficiently display scrollable lists

import java.util.List; // Used to store a collection of objects (cart items)
import java.util.Locale; // For locale-specific number and currency formatting

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> { // Adapter for displaying cart items in a RecyclerView

    private List<CartItem> cartItemList; // List holding all items in the cart
    private Runnable updateCallback; // Callback to update total price and UI in the Activity
    private CartManager cartManager; // Singleton that manages cart logic (add, remove, etc.)
    private Context context; // Reference to the current context (used for toasts and inflation)

    public CartAdapter(List<CartItem> cartItemList, Runnable updateCallback) { // Constructor for adapter
        this.cartItemList = cartItemList; // Initialize list of cart items
        this.updateCallback = updateCallback; // Save callback for later use
        this.cartManager = CartManager.getInstance(); // Get singleton instance of CartManager
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // Called when creating a new item view
        this.context = parent.getContext(); // Get context from parent (RecyclerView)
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_cart, parent, false); // Inflate (create) view from XML layout
        return new CartViewHolder(view); // Return a new ViewHolder instance
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) { // Called to bind data to an existing item view
        CartItem cartItem = cartItemList.get(position); // Get the current cart item from list

        holder.tvMedicineName.setText(cartItem.getName()); // Display medicine name
        holder.tvQuantity.setText(String.valueOf(cartItem.getQuantity())); // Show item quantity
        holder.tvPrice.setText(String.format(Locale.getDefault(), "₹%.2f each", cartItem.getPrice())); // Display unit price

        double itemTotal = cartItem.getPrice() * cartItem.getQuantity(); // Calculate total price for this line
        holder.tvLineTotal.setText(String.format(Locale.getDefault(), "Total: ₹%.2f", itemTotal)); // Display line total

        // --- INCREMENT LOGIC ---
        holder.btnIncrease.setOnClickListener(v -> { // When "+" button is clicked
            int currentPosition = holder.getAdapterPosition(); // Get current position safely
            if (currentPosition == RecyclerView.NO_POSITION) return; // Exit if invalid position

            CartItem item = cartItemList.get(currentPosition); // Get item at current position
            boolean success = cartManager.incrementItemQuantity(item); // Try to increase item quantity

            if (success) { // If increment successful
                notifyItemChanged(currentPosition); // Refresh this item view
                updateCallback.run(); // Recalculate total and update Activity
            } else { // If stock limit reached
                Toast.makeText(context,
                        "Stock limit reached (" + item.getAvailableQuantity() + ")", // Show error message
                        Toast.LENGTH_SHORT).show(); // Display toast
            }
        });

        // --- DECREMENT LOGIC ---
        holder.btnDecrease.setOnClickListener(v -> { // When "-" button is clicked
            int currentPosition = holder.getAdapterPosition(); // Get item’s adapter position
            if (currentPosition == RecyclerView.NO_POSITION) return; // Safety check

            CartItem item = cartItemList.get(currentPosition); // Get the item
            int newQuantity = cartManager.decrementItemQuantity(item); // Decrease quantity

            if (newQuantity <= 0) { // If quantity becomes 0
                // CartManager already removed the item, no need to modify list here
            } else { // If item still exists with lower quantity
                notifyItemChanged(currentPosition); // Update that item view
            }
            updateCallback.run(); // Update total and refresh UI
        });

        // --- REMOVE LOGIC ---
        holder.btnRemove.setOnClickListener(v -> { // When "remove" button is clicked
            int currentPosition = holder.getAdapterPosition(); // Get current adapter position
            if (currentPosition == RecyclerView.NO_POSITION) return; // Safety check

            CartItem item = cartItemList.get(currentPosition); // Get item to remove
            cartManager.removeItem(item); // Remove item using CartManager
            updateCallback.run(); // Update Activity UI and total
        });
    }

    @Override
    public int getItemCount() { // Returns how many items are in the cart
        return cartItemList.size(); // Return the size of the cart item list
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder { // ViewHolder class to hold item views
        TextView tvMedicineName, tvPrice, tvQuantity, tvLineTotal; // Text fields in each cart item
        ImageButton btnIncrease, btnDecrease, btnRemove; // Buttons for +, -, and remove actions

        public CartViewHolder(@NonNull View itemView) { // Constructor for ViewHolder
            super(itemView); // Pass itemView to parent class

            tvMedicineName = itemView.findViewById(R.id.tv_cart_medicine_name); // Connect name TextView
            tvPrice = itemView.findViewById(R.id.tv_cart_medicine_price); // Connect price TextView
            tvQuantity = itemView.findViewById(R.id.tv_cart_quantity); // Connect quantity TextView
            btnIncrease = itemView.findViewById(R.id.btn_increase_quantity); // Connect "+" button
            btnDecrease = itemView.findViewById(R.id.btn_decrease_quantity); // Connect "-" button
            tvLineTotal = itemView.findViewById(R.id.tv_cart_line_total); // Connect line total TextView
            btnRemove = itemView.findViewById(R.id.btn_remove_item); // Connect "remove" button
        }
    }
}
