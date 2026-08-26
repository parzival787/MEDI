package com.example.medi; // Declares the package this class belongs to

// Import required Android and Java classes
import android.content.Context; // Context provides access to resources, layouts, etc.
import android.view.LayoutInflater; // Inflates XML layouts into View objects
import android.view.View; // Represents a UI element
import android.view.ViewGroup; // Container for Views
import android.widget.LinearLayout; // Layout to hold a list of views vertically or horizontally
import android.widget.TextView; // UI element to display text

import androidx.annotation.NonNull; // For null-safety annotations
import androidx.recyclerview.widget.RecyclerView; // RecyclerView for displaying lists

import java.text.SimpleDateFormat; // For formatting dates
import java.util.List; // Java List interface
import java.util.Locale; // For locale-specific formatting
import java.util.Map; // To represent items in an order as key-value pairs

// Adapter for showing a list of received orders in a RecyclerView
public class OrdersReceivedAdapter extends RecyclerView.Adapter<OrdersReceivedAdapter.OrderViewHolder> {

    private Context context; // Context from activity
    private List<PharmacistOrder> orderList; // List of pharmacist orders
    private SimpleDateFormat dateFormat; // Format for showing date and time
    private LayoutInflater inflater; // Inflates sub-item views for medicines

    // Constructor
    public OrdersReceivedAdapter(Context context, List<PharmacistOrder> orderList) {
        this.context = context; // Save context
        this.orderList = orderList; // Save order list
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()); // Format dates like "03 Nov 2025, 04:30 PM"
        this.inflater = LayoutInflater.from(context); // Initialize inflater to inflate item layouts later
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single order row
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_order_received, parent, false);
        return new OrderViewHolder(view); // Return new ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        PharmacistOrder order = orderList.get(position); // Get the order at this position

        // --- Set main text fields for user info and order details ---
        holder.tvUserName.setText(order.getUserName()); // Show user's name
        holder.tvUserPhone.setText(order.getUserPhone()); // Show user's phone number
        holder.tvTotal.setText(String.format(Locale.getDefault(), "₹%.2f", order.getTotalAmount())); // Show total price
        holder.tvStatus.setText(order.getStatus()); // Show order status

        // Show order date
        if (order.getOrderTimestamp() != null) {
            holder.tvDate.setText(dateFormat.format(order.getOrderTimestamp())); // Format timestamp to readable date
        } else {
            holder.tvDate.setText("Date pending..."); // Placeholder if date not available
        }

        // --- Show IDs for order and payment ---
        holder.tvOrderId.setText("Order ID: " + order.getUserOrderRefId()); // Show order reference ID
        holder.tvPaymentId.setText("Payment ID: " + order.getPaymentId()); // Show payment ID

        // --- Populate the list of items in this order ---
        populateOrderItems(holder.itemsContainer, order.getItems());

        // TODO: You can add click listener here to open order details activity
        // holder.itemView.setOnClickListener(v -> { ... });
    }

    /**
     * Dynamically populates the itemsContainer LinearLayout with medicine items.
     * Clears old views (important for RecyclerView recycling) and inflates a view for each medicine.
     */
    private void populateOrderItems(LinearLayout container, List<Map<String, Object>> items) {
        container.removeAllViews(); // Clear old views from recycled ViewHolder

        if (items == null || items.isEmpty()) {
            return; // No items to display
        }

        // Loop through each item in the order
        for (Map<String, Object> item : items) {
            // Inflate layout for a single medicine item
            View itemView = inflater.inflate(R.layout.list_item_order_received_medicine, container, false);

            // Find the TextViews in the sub-item layout
            TextView tvItemName = itemView.findViewById(R.id.tv_item_name);
            TextView tvItemQty = itemView.findViewById(R.id.tv_item_quantity);
            TextView tvItemPrice = itemView.findViewById(R.id.tv_item_price);

            // Extract data from the Map
            String name = (String) item.get("medicineName"); // Medicine name
            Double price = (Double) item.get("price"); // Price per unit
            Long quantityLong = (Long) item.get("quantity"); // Quantity (Firestore stores numbers as Long)

            // Convert quantity and price safely
            int quantity = (quantityLong != null) ? quantityLong.intValue() : 0;
            double itemPrice = (price != null) ? price : 0.0;
            double totalItemPrice = itemPrice * quantity; // Total price for this medicine

            // Set values in the sub-item views
            tvItemName.setText(name); // Medicine name
            tvItemQty.setText("x" + quantity); // Quantity
            tvItemPrice.setText(String.format(Locale.getDefault(), "₹%.2f", totalItemPrice)); // Total price

            // Add this medicine view to the container layout
            container.addView(itemView);
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size(); // Number of orders
    }

    // ViewHolder class for RecyclerView
    static class OrderViewHolder extends RecyclerView.ViewHolder {
        // Views for main order details
        TextView tvUserName, tvUserPhone, tvStatus, tvDate, tvTotal, tvOrderId, tvPaymentId;
        LinearLayout itemsContainer; // Container for all medicines in this order

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find all views by their IDs
            tvUserName = itemView.findViewById(R.id.tv_order_user_name);
            tvUserPhone = itemView.findViewById(R.id.tv_order_user_phone);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
            tvDate = itemView.findViewById(R.id.tv_order_date);
            tvTotal = itemView.findViewById(R.id.tv_order_total);

            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvPaymentId = itemView.findViewById(R.id.tv_payment_id);
            itemsContainer = itemView.findViewById(R.id.layout_order_items); // Container for list of medicines
        }
    }
}
