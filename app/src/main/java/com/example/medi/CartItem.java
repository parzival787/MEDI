package com.example.medi; // Defines the package where this class belongs

import android.os.Parcel; // Used for reading and writing data to a Parcel (for Parcelable)
import android.os.Parcelable; // Interface that allows objects to be passed between Android components

public class CartItem implements Parcelable { // Model class representing one item in the cart and implements Parcelable
    private String medicineId; // Unique ID for the medicine
    private String name; // Name of the medicine
    private double price; // Price of a single unit of the medicine
    private int quantity; // Quantity of this item added to the cart
    private String pharmacistId; // ID of the pharmacist selling the medicine
    private int availableQuantity; // The total stock available for this medicine

    // Public constructor - initializes all fields
    public CartItem(String medicineId, String name, double price, int quantity, String pharmacistId, int availableQuantity) {
        this.medicineId = medicineId; // Assigns medicine ID
        this.name = name; // Assigns medicine name
        this.price = price; // Assigns price
        this.quantity = quantity; // Assigns quantity added to cart
        this.pharmacistId = pharmacistId; // Assigns pharmacist ID
        this.availableQuantity = availableQuantity; // Assigns available stock
    }

    // --- Parcelable Implementation ---

    protected CartItem(Parcel in) { // Constructor that recreates a CartItem object from a Parcel
        medicineId = in.readString(); // Reads medicine ID from Parcel
        name = in.readString(); // Reads medicine name from Parcel
        price = in.readDouble(); // Reads price from Parcel
        quantity = in.readInt(); // Reads quantity from Parcel
        pharmacistId = in.readString(); // Reads pharmacist ID from Parcel
        availableQuantity = in.readInt(); // Reads available quantity from Parcel
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() { // Parcelable CREATOR required for recreating objects
        @Override
        public CartItem createFromParcel(Parcel in) { // Creates a new CartItem from Parcel data
            return new CartItem(in); // Calls the Parcel constructor
        }

        @Override
        public CartItem[] newArray(int size) { // Creates an array of CartItem objects
            return new CartItem[size]; // Returns array with specified size
        }
    };

    @Override
    public int describeContents() { // Usually returns 0, unless object has special contents (like file descriptors)
        return 0; // No special content here
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) { // Writes object data to a Parcel for transfer
        dest.writeString(medicineId); // Writes medicine ID
        dest.writeString(name); // Writes medicine name
        dest.writeDouble(price); // Writes price
        dest.writeInt(quantity); // Writes quantity
        dest.writeString(pharmacistId); // Writes pharmacist ID
        dest.writeInt(availableQuantity); // Writes available stock
    }

    // --- Getters and Setters (for accessing and modifying fields) ---

    public String getMedicineId() { return medicineId; } // Returns medicine ID
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; } // Sets medicine ID

    public String getName() { return name; } // Returns medicine name
    public void setName(String name) { this.name = name; } // Sets medicine name

    public double getPrice() { return price; } // Returns price of the medicine
    public void setPrice(double price) { this.price = price; } // Sets price of the medicine

    public int getQuantity() { return quantity; } // Returns current quantity in cart
    public void setQuantity(int quantity) { this.quantity = quantity; } // Sets quantity in cart

    public String getPharmacistId() { return pharmacistId; } // Returns pharmacist ID
    public void setPharmacistId(String pharmacistId) { this.pharmacistId = pharmacistId; } // Sets pharmacist ID

    // --- NEW GETTER ---
    public int getAvailableQuantity() { return availableQuantity; } // Returns total available stock for this medicine
    // public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; } // (Optional) setter for stock
}
