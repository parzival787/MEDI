package com.example.medi; // Package name

import android.content.Context; // For accessing app resources and colors
import android.view.LayoutInflater; // To inflate XML layouts
import android.view.View; // Represents UI elements
import android.view.ViewGroup; // Parent view for RecyclerView items
import android.widget.TextView; // To display text

import androidx.annotation.NonNull; // For null safety annotations
import androidx.recyclerview.widget.RecyclerView; // RecyclerView base class

import java.text.SimpleDateFormat; // For formatting timestamps into readable strings
import java.util.ArrayList; // For managing a list of orders
import java.util.Locale; // Locale for formatting currency and date

/**
 * Adapter class to bind a list of orders to a RecyclerView.
 */
public class MyOrdersAdapter extends RecyclerView.Adapter<MyOrdersAdapter.OrderViewHolder> {

    private Context context; // Application context for layout inflation & resource access
    private ArrayList<Order> orderList; // List containing orders
    private SimpleDateFormat dateFormat; // To format order timestamps

    public MyOrdersAdapter(Context context, ArrayList<Order> orderList) { // Constructor
        this.context = context; // Assign context
        this.orderList = orderList; // Assign order list
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()); // e.g., "01 Nov 2025"
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout for a single order item
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_order, parent, false);
        return new OrderViewHolder(view); // Return a new ViewHolder instance
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position); // Get the order at this position

        // Display total amount, formatted as currency
        holder.tvTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", order.getTotalAmount()));

        // Display order status
        holder.tvStatus.setText(order.getStatus());

        // Display order ID
        holder.tvOrderId.setText("Order ID: " + order.getOrderId());

        // Display order date if available
        if (order.getOrderTimestamp() != null) {
            holder.tvOrderDate.setText("Ordered on: " + dateFormat.format(order.getOrderTimestamp()));
        } else {
            holder.tvOrderDate.setText("Order date pending...");
        }

        // --- Optional: Status color logic ---
        // You can uncomment and customize these lines to visually distinguish order status
        /*
        if (order.getStatus().equals("Cancelled")) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_background_cancelled);
            holder.tvStatus.setTextColor(context.getColor(R.color.red));
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.status_background_placed);
            holder.tvStatus.setTextColor(context.getColor(R.color.green));
        }
        */
    }

    @Override
    public int getItemCount() {
        return orderList.size(); // Return the total number of orders
    }

    /**
     * ViewHolder class to cache references to item views for performance.
     */
    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTotalAmount, tvStatus, tvOrderDate, tvOrderId; // UI elements in the item layout

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView); // Pass view to parent constructor
            tvTotalAmount = itemView.findViewById(R.id.tv_order_total_amount); // Total amount
            tvStatus = itemView.findViewById(R.id.tv_order_status); // Order status
            tvOrderDate = itemView.findViewById(R.id.tv_order_date); // Date of order
            tvOrderId = itemView.findViewById(R.id.tv_order_id); // Order ID
        }
    }
}
