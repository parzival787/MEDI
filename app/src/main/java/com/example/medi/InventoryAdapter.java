package com.example.medi; // Defines the package for this class

import android.content.Context; // Provides access to application-specific resources
import android.graphics.Color; // For setting text color directly using RGB or predefined colors
import android.view.LayoutInflater; // Used to convert XML layouts into View objects
import android.view.View; // Base class for UI components
import android.view.ViewGroup; // A container that holds multiple child views
import android.widget.TextView; // For displaying text in the layout

import androidx.annotation.NonNull; // Annotation to mark non-nullable parameters
import androidx.core.content.ContextCompat; // Helps access colors and resources safely
import androidx.recyclerview.widget.RecyclerView; // Used to efficiently display a list of scrollable items

import java.util.List; // Used for storing collections of objects
import java.util.Locale; // For locale-specific formatting like currency and numbers

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> { // Adapter for showing medicines in a RecyclerView

    private Context context; // Holds reference to the current context (e.g., Activity)
    private List<Medicine> medicineList; // List of medicines to display in the inventory

    public InventoryAdapter(Context context, List<Medicine> medicineList) { // Constructor for adapter
        this.context = context; // Initializes context
        this.medicineList = medicineList; // Initializes list of medicines
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // Called when creating a new view for a medicine item
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_inventory_medicine, parent, false); // Inflate the custom XML layout for each item
        return new InventoryViewHolder(view); // Return a new ViewHolder containing that view
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) { // Called to bind data to an item at a specific position
        Medicine medicine = medicineList.get(position); // Get the current medicine from the list

        holder.tvName.setText(medicine.getName()); // Set medicine name
        holder.tvManufacturer.setText("by " + medicine.getManufacturer()); // Show manufacturer name
        holder.tvPrice.setText(String.format(Locale.getDefault(), "₹%.2f", medicine.getPrice())); // Display price in INR format

        // --- Set stock text and adjust color based on stock quantity ---
        holder.tvStock.setText(String.format(Locale.getDefault(), "%d units", medicine.getStock())); // Show number of available units

        if (medicine.getStock() <= 0) { // If stock is zero or negative
            holder.tvStock.setText("Out of Stock"); // Display "Out of Stock" message
            holder.tvStock.setTextColor(Color.RED); // Set text color to red for emphasis
        } else if (medicine.getStock() < 10) { // If stock is low (less than 10)
            holder.tvStock.setTextColor(ContextCompat.getColor(context, R.color.low_stock_warning)); // Use a warning color (like orange)
        } else { // Normal stock level
            holder.tvStock.setTextColor(ContextCompat.getColor(context, android.R.color.tab_indicator_text)); // Use default text color
        }

        // TODO: Add OnClickListener to open a details/edit screen for this medicine item
        // holder.itemView.setOnClickListener(v -> { ... }); // Example for future implementation
    }

    @Override
    public int getItemCount() { // Returns total number of items to show in RecyclerView
        return medicineList.size(); // Return the number of medicines
    }

    static class InventoryViewHolder extends RecyclerView.ViewHolder { // ViewHolder holds references to each view inside a list item
        TextView tvName, tvManufacturer, tvPrice, tvStock; // TextViews for displaying medicine details

        public InventoryViewHolder(@NonNull View itemView) { // Constructor for ViewHolder
            super(itemView); // Call parent constructor
            tvName = itemView.findViewById(R.id.tv_med_name); // Connects to medicine name TextView in layout
            tvManufacturer = itemView.findViewById(R.id.tv_med_manufacturer); // Connects to manufacturer TextView
            tvPrice = itemView.findViewById(R.id.tv_med_price); // Connects to price TextView
            tvStock = itemView.findViewById(R.id.tv_med_stock); // Connects to stock TextView
        }
    }
}
