package com.example.bookingapp;

import android.content.Context;
import android.util.Log;
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

public class StyleAdapter extends RecyclerView.Adapter<StyleAdapter.StyleViewHolder> {

    private Context context;
    private List<StyleItem> styleList;
    private int buttonBackgroundId;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onBookButtonClick(int position, StyleItem styleItem);
        void onItemClick(int position, StyleItem styleItem);
    }

    public StyleAdapter(Context context, List<StyleItem> styleList, int buttonBackgroundId) {
        this.context = context;
        this.styleList = styleList;
        this.buttonBackgroundId = buttonBackgroundId;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public StyleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new StyleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StyleViewHolder holder, int position) {
        StyleItem styleItem = styleList.get(position);

        // Debug logging - check what data we actually have
        Log.d("StyleAdapter", "Binding item at position " + position +
                ": " + styleItem.getName() +
                ", Price: " + styleItem.getPrice() +
                ", Duration: " + styleItem.getDuration());

        // Set the service name/title
        if (styleItem.getName() != null && !styleItem.getName().isEmpty()) {
            holder.serviceNameTextView.setText(styleItem.getName());
        } else {
            holder.serviceNameTextView.setText("Service Name");
            Log.w("StyleAdapter", "Service name is null or empty at position " + position);
        }

        // Set price and duration with proper formatting and fallbacks
        String priceDuration = formatPriceDuration(styleItem.getPrice(), styleItem.getDuration());
        holder.priceDurationTextView.setText(priceDuration);

        // Log what we're displaying
        Log.d("StyleAdapter", "Displaying price/duration: " + priceDuration);

        // Load image using Glide with fallback
        if (styleItem.getImageUrl() != null && !styleItem.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(styleItem.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.serviceImageView);
        } else {
            holder.serviceImageView.setImageResource(android.R.drawable.ic_menu_gallery);
            Log.d("StyleAdapter", "No image URL for service: " + styleItem.getName());
        }

        // Set the background for the book button
        if (buttonBackgroundId != 0) {
            holder.bookButton.setBackgroundResource(buttonBackgroundId);
        }

        // DEBUG: Test if button is clickable - ADDED DEBUG LOGGING
        holder.bookButton.setOnClickListener(v -> {
            Log.d("StyleAdapter", "🎯 BOOK BUTTON CLICKED for: " + styleItem.getName() + " at position " + position);
            if (onItemClickListener != null) {
                Log.d("StyleAdapter", "🎯 Notifying listener about book button click");
                onItemClickListener.onBookButtonClick(position, styleItem);
            } else {
                Log.e("StyleAdapter", "❌ onItemClickListener is NULL! Check if setOnItemClickListener was called");
            }
        });

        // DEBUG: Test if card is clickable - ADDED DEBUG LOGGING
        holder.cardView.setOnClickListener(v -> {
            Log.d("StyleAdapter", "🎯 CARD CLICKED for: " + styleItem.getName() + " at position " + position);
            if (onItemClickListener != null) {
                Log.d("StyleAdapter", "🎯 Notifying listener about card click");
                onItemClickListener.onItemClick(position, styleItem);
            } else {
                Log.e("StyleAdapter", "❌ onItemClickListener is NULL! Check if setOnItemClickListener was called");
            }
        });

        // Make sure views are clickable - ADDED
        holder.bookButton.setClickable(true);
        holder.cardView.setClickable(true);

        Log.d("StyleAdapter", "✅ Finished binding item: " + styleItem.getName());
    }

    @Override
    public int getItemCount() {
        return styleList != null ? styleList.size() : 0;
    }

    // Helper method to format price and duration
    private String formatPriceDuration(double price, int duration) {
        // Check if we have valid data
        boolean hasPrice = price > 0;
        boolean hasDuration = duration > 0;

        Log.d("StyleAdapter", "Formatting - hasPrice: " + hasPrice + " (" + price + "), hasDuration: " + hasDuration + " (" + duration + ")");

        if (hasPrice && hasDuration) {
            return String.format("R%.2f • %d min", price, duration); // Changed $ to R
        } else if (hasPrice) {
            return String.format("R%.2f", price); // Changed $ to R
        } else if (hasDuration) {
            return String.format("%d min", duration);
        } else {
            return "Contact for pricing";
        }
    }

    // Update data method
    public void updateData(List<StyleItem> newStyleList) {
        this.styleList = newStyleList;
        notifyDataSetChanged();

        // Log the update
        Log.d("StyleAdapter", "Data updated. Total items: " + (styleList != null ? styleList.size() : 0));
    }

    public static class StyleViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView serviceImageView;
        TextView serviceNameTextView;
        TextView priceDurationTextView;
        TextView bookButton;

        public StyleViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_view);
            serviceImageView = itemView.findViewById(R.id.iv_service_image);
            serviceNameTextView = itemView.findViewById(R.id.tv_service_name);
            priceDurationTextView = itemView.findViewById(R.id.tv_price_duration);
            bookButton = itemView.findViewById(R.id.btn_book);

            // Verify all views are found
            if (priceDurationTextView == null) {
                Log.e("StyleAdapter", "❌ priceDurationTextView is NULL! Check item_service.xml");
            }
            if (bookButton == null) {
                Log.e("StyleAdapter", "❌ bookButton is NULL! Check item_service.xml");
            }
            if (cardView == null) {
                Log.e("StyleAdapter", "❌ cardView is NULL! Check item_service.xml");
            }

            if (cardView != null && serviceImageView != null && serviceNameTextView != null &&
                    priceDurationTextView != null && bookButton != null) {
                Log.d("StyleAdapter", "✅ All views initialized successfully");
            }
        }
    }
}