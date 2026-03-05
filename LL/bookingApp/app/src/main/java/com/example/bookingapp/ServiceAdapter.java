package com.example.bookingapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private Context context;
    private List<StyleItem> serviceList;
    private int buttonBackgroundId;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onBookButtonClick(int position, StyleItem serviceItem);
        void onItemClick(int position, StyleItem serviceItem);
    }

    public ServiceAdapter(Context context, List<StyleItem> serviceList, int buttonBackgroundId) {
        this.context = context;
        this.serviceList = serviceList;
        this.buttonBackgroundId = buttonBackgroundId;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_style, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        StyleItem serviceItem = serviceList.get(position);

        // Set the service name
        if (serviceItem.getName() != null) {
            holder.tvServiceName.setText(serviceItem.getName());
        } else {
            holder.tvServiceName.setText("Service Name");
        }

        // Set price and duration
        String priceDuration = "R" + serviceItem.getPrice() + " • " + serviceItem.getDuration() + " min";
        holder.tvPriceDuration.setText(priceDuration);

        // Load image using Glide
        if (serviceItem.getImageUrl() != null && !serviceItem.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(serviceItem.getImageUrl())
                    .into(holder.ivServiceImage);
        }

        // Set button background
        if (buttonBackgroundId != 0) {
            holder.btnBook.setBackgroundResource(buttonBackgroundId);
        }

        // Set click listeners
        holder.btnBook.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onBookButtonClick(position, serviceItem);
            } else {
                // Default booking flow if no listener is set
                startBookingFlow(serviceItem);
            }
        });

        holder.cardView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position, serviceItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return serviceList != null ? serviceList.size() : 0;
    }

    /**
     * Default booking flow when user clicks "Book Now"
     */
    private void startBookingFlow(StyleItem serviceItem) {
        Intent intent = new Intent(context, BookingSelectActivity.class);

        // Pass all necessary service data for booking
        intent.putExtra("serviceName", serviceItem.getName());
        intent.putExtra("servicePrice", "R" + serviceItem.getPrice());
        intent.putExtra("serviceDuration", serviceItem.getDuration() + " min");
        intent.putExtra("serviceImageUrl", serviceItem.getImageUrl());
        intent.putExtra("serviceCategory", serviceItem.getCategory()); // Important for Firestore

        // Pass raw numeric values for Firestore
        intent.putExtra("rawPrice", serviceItem.getPrice());
        intent.putExtra("rawDuration", serviceItem.getDuration());

        context.startActivity(intent);
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivServiceImage;
        TextView tvServiceName;
        TextView tvPriceDuration;
        TextView btnBook;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            ivServiceImage = itemView.findViewById(R.id.iv_service_image);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvPriceDuration = itemView.findViewById(R.id.tv_price_duration);
            btnBook = itemView.findViewById(R.id.btn_book);
        }
    }

    // Helper method to update data
    public void updateData(List<StyleItem> newServiceList) {
        this.serviceList = newServiceList;
        notifyDataSetChanged();
    }
}