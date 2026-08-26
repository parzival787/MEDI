package com.example.medi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // For formatting price

public class MedicineSearchUserAdapter extends RecyclerView.Adapter<MedicineSearchUserAdapter.ViewHolder> {

    private List<QueryDocumentSnapshot> medicineList;
    private final OnItemClickListener listener;

    // Interface for click handling in the Activity
    public interface OnItemClickListener {
        void onItemClick(QueryDocumentSnapshot document);
    }

    public MedicineSearchUserAdapter(List<QueryDocumentSnapshot> medicineList, OnItemClickListener listener) {
        this.medicineList = medicineList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_medicine_search, parent, false); // Ensure this layout exists
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QueryDocumentSnapshot document = medicineList.get(position);
        holder.bind(document, listener);
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    // Method to update the data in the adapter
    public void updateData(List<QueryDocumentSnapshot> newList) {
        this.medicineList = newList;
        notifyDataSetChanged(); // Simple way to refresh the list
    }

    // ViewHolder class
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicineName, tvMedicinePrice, tvPharmacistInfo; // Added pharmacist info TextView

        ViewHolder(View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tv_search_medicine_name);
            tvMedicinePrice = itemView.findViewById(R.id.tv_search_medicine_price);
            tvPharmacistInfo = itemView.findViewById(R.id.tv_search_pharmacist_info); // Find the new TextView
        }

        void bind(final QueryDocumentSnapshot document, final OnItemClickListener listener) {
            String name = document.getString("name");
            Double price = document.getDouble("price");
            // Get pharmacist ID from the document reference path
            String pharmacistId = document.getReference().getParent().getParent().getId();

            tvMedicineName.setText(name != null ? name : "N/A");
            tvMedicinePrice.setText(String.format(Locale.getDefault(), "INR %.2f", price != null ? price : 0.0));

            // Set placeholder, actual name loading might be done elsewhere or added here if needed
            tvPharmacistInfo.setText("ID: ..." + pharmacistId.substring(Math.max(0, pharmacistId.length() - 4))); // Show partial ID

            itemView.setOnClickListener(v -> listener.onItemClick(document));
        }
    }
}

