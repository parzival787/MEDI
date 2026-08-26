package com.example.medi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CartManager {

    private static CartManager instance;
    private final List<CartItem> cartItems;

    // Private constructor for Singleton pattern
    private CartManager() {
        cartItems = new ArrayList<>();
    }

    // Get the single instance of the CartManager
    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    // Get the current list of items in the cart
    public List<CartItem> getCartItems() {
        return cartItems;
    }

    /**
     * Add an item to the cart or increase quantity if it already exists.
     * Checks against available stock.
     *
     * @param newItem The item to add.
     * @return true if added/incremented successfully, false if stock limit would be exceeded.
     */
    public boolean addToCart(CartItem newItem) {
        for (CartItem item : cartItems) {
            // Check if the same medicine from the same pharmacist is already in the cart
            if (item.getMedicineId().equals(newItem.getMedicineId()) &&
                    item.getPharmacistId().equals(newItem.getPharmacistId())) {

                int newQuantity = item.getQuantity() + newItem.getQuantity();

                // Check stock limit
                if (newQuantity > item.getAvailableQuantity()) {
                    return false; // Failed to add, stock limit
                } else {
                    item.setQuantity(newQuantity);
                    return true; // Item found and updated
                }
            }
        }

        // If the loop finishes, the item wasn't found. Add it if stock is sufficient.
        if (newItem.getQuantity() > newItem.getAvailableQuantity()) {
            return false; // Failed to add, not enough stock
        } else {
            cartItems.add(newItem);
            return true; // Added new item
        }
    }

    /**
     * Tries to increment the quantity of an item by 1.
     *
     * @param itemToInc The item to increment.
     * @return true if successful, false if stock limit is reached.
     */
    public boolean incrementItemQuantity(CartItem itemToInc) {
        if (itemToInc.getQuantity() < itemToInc.getAvailableQuantity()) {
            itemToInc.setQuantity(itemToInc.getQuantity() + 1);
            return true; // Success
        }
        return false; // At stock limit
    }

    /**
     * Decreases the quantity of an item by 1.
     * Removes the item if the new quantity is 0 or less.
     *
     * @param itemToDec The item to decrement.
     * @return The new quantity (or 0 if the item was removed).
     */
    public int decrementItemQuantity(CartItem itemToDec) {
        int newQuantity = itemToDec.getQuantity() - 1;

        if (newQuantity <= 0) {
            removeItem(itemToDec); // Remove it from the list
            return 0;
        } else {
            itemToDec.setQuantity(newQuantity);
            return newQuantity;
        }
    }

    // Remove an item completely from the cart
    public void removeItem(CartItem itemToRemove) {
        // Use removeIf for safer removal
        cartItems.removeIf(item -> item.getMedicineId().equals(itemToRemove.getMedicineId()) &&
                Objects.equals(item.getPharmacistId(), itemToRemove.getPharmacistId()));
    }

    // Calculate the total price of all items in the cart
    public double calculateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    // Clear all items from the cart
    public void clearCart() {
        cartItems.clear();
    }

    // Get the number of unique items (lines) in the cart
    public int getItemCount() {
        return cartItems.size();
    }
}