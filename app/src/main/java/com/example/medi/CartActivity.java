package com.example.medi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity; // Correct import
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Locale;

// FIXED: Removed "com.example.medi." prefix. Now extends the standard Android class.
public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCart;
    private TextView tvTotalAmount;
    private Button btnCheckout;
    private LinearLayout emptyCartLayout;
    private CartAdapter adapter;
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize the singleton
        cartManager = CartManager.getInstance();

        setupToolbar();
        initializeViews();
        setupRecyclerView();
        updateCartState();

        // Checkout Button Logic
        btnCheckout.setOnClickListener(v -> {
            if (cartManager.getItemCount() > 0) {
                Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
                // Tip: Ensure PaymentActivity handles the key "TOTAL_AMOUNT"
                intent.putExtra("TOTAL_AMOUNT", cartManager.calculateTotal());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Your cart is empty.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list in case items were changed in another activity or payment failed/succeeded
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateCartState();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar_cart);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Your Cart");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initializeViews() {
        recyclerViewCart = findViewById(R.id.recycler_view_cart);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        btnCheckout = findViewById(R.id.btn_checkout);
        emptyCartLayout = findViewById(R.id.empty_cart_layout);
    }

    private void setupRecyclerView() {
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        // We pass 'this::updateCartState' so the adapter can trigger a UI update when an item is removed
        adapter = new CartAdapter(cartManager.getCartItems(), this::updateCartState);
        recyclerViewCart.setAdapter(adapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateCartState() {
        double total = cartManager.calculateTotal();
        tvTotalAmount.setText(String.format(Locale.getDefault(), "INR %.2f", total));

        // Toggle visibility based on whether the cart is empty
        if (cartManager.getItemCount() == 0) {
            recyclerViewCart.setVisibility(View.GONE);
            emptyCartLayout.setVisibility(View.VISIBLE);
            btnCheckout.setEnabled(false);
        } else {
            recyclerViewCart.setVisibility(View.VISIBLE);
            emptyCartLayout.setVisibility(View.GONE);
            btnCheckout.setEnabled(true);
        }
    }
}