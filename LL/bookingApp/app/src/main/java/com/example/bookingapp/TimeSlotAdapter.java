package com.example.bookingapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {

    private static final String TAG = "TimeSlotAdapter";

    private List<TimeSlotItem> timeSlots;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private OnTimeSlotSelectedListener selectionListener;
    private String currentDate;
    private String currentCategory;

    public interface OnTimeSlotSelectedListener {
        void onTimeSlotSelected(TimeSlotItem selectedSlot);
        void onTimeSlotUnavailable(TimeSlotItem unavailableSlot);
    }

    public TimeSlotAdapter(List<TimeSlotItem> timeSlots, OnTimeSlotSelectedListener listener,
                           String date, String category) {
        this.timeSlots = timeSlots != null ? new ArrayList<>(timeSlots) : new ArrayList<>();
        this.selectionListener = listener;
        this.currentDate = date;
        this.currentCategory = category;

        if (this.selectionListener == null) {
            Log.w(TAG, "No OnTimeSlotSelectedListener supplied; selection callbacks will be disabled.");
        }
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_slot, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        TimeSlotItem currentSlot = timeSlots.get(position);

        // Set time slot text (e.g., "10:00 AM")
        holder.timeSlotRadioButton.setText(currentSlot.getTimeSlot());

        // Show grouping text if available and different from previous
        if (shouldShowGrouping(position)) {
            holder.groupingText.setText(currentSlot.getGrouping());
            holder.groupingText.setVisibility(View.VISIBLE);
        } else {
            holder.groupingText.setVisibility(View.GONE);
        }

        boolean available = currentSlot.isAvailable();
        holder.timeSlotRadioButton.setEnabled(available);
        holder.itemView.setEnabled(available);

        // Update visual state based on availability
        updateTimeSlotAppearance(holder, available, position == selectedPosition);

        // Set checked state
        holder.timeSlotRadioButton.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            TimeSlotItem clickedSlot = timeSlots.get(adapterPos);

            if (!clickedSlot.isAvailable()) {
                if (selectionListener != null) {
                    selectionListener.onTimeSlotUnavailable(clickedSlot);
                }
                return;
            }

            int oldSelectedPosition = selectedPosition;
            selectedPosition = adapterPos;

            // Update both old and new positions
            if (oldSelectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldSelectedPosition);
            }
            notifyItemChanged(selectedPosition);

            if (selectionListener != null) {
                selectionListener.onTimeSlotSelected(clickedSlot);
            }
        });

        // Make the RadioButton forward clicks to itemView's click handler
        holder.timeSlotRadioButton.setOnClickListener(v -> holder.itemView.performClick());
    }

    private boolean shouldShowGrouping(int position) {
        if (position == 0) {
            return true; // Always show for first item
        }

        String currentGrouping = timeSlots.get(position).getGrouping();
        String previousGrouping = timeSlots.get(position - 1).getGrouping();

        return !currentGrouping.equals(previousGrouping);
    }

    private void updateTimeSlotAppearance(TimeSlotViewHolder holder, boolean available, boolean isSelected) {
        if (available) {
            if (isSelected) {
                // Selected state - Blue background, white text
                holder.timeSlotRadioButton.setBackgroundResource(R.drawable.time_slot_selected);
                holder.timeSlotRadioButton.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
            } else {
                // Available but not selected - White background, black text
                holder.timeSlotRadioButton.setBackgroundResource(R.drawable.time_slot_available);
                holder.timeSlotRadioButton.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black));
            }
            holder.itemView.setAlpha(1f);
        } else {
            // Unavailable state - Gray background, gray text
            holder.timeSlotRadioButton.setBackgroundResource(R.drawable.time_slot_unavailable);
            holder.timeSlotRadioButton.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray));
            holder.itemView.setAlpha(0.7f);
        }
    }

    @Override
    public int getItemCount() {
        return timeSlots != null ? timeSlots.size() : 0;
    }

    public static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        RadioButton timeSlotRadioButton;
        TextView groupingText;

        public TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            timeSlotRadioButton = itemView.findViewById(R.id.rb_time_slot);
            groupingText = itemView.findViewById(R.id.tv_time_grouping);
            // Removed the availabilityText reference since it's not in the XML anymore
        }
    }

    public void clearSelection() {
        int old = selectedPosition;
        selectedPosition = RecyclerView.NO_POSITION;
        if (old != RecyclerView.NO_POSITION) {
            notifyItemChanged(old);
        }
    }

    public TimeSlotItem getSelectedSlot() {
        if (selectedPosition == RecyclerView.NO_POSITION) return null;
        if (timeSlots == null || selectedPosition < 0 || selectedPosition >= timeSlots.size()) return null;
        return timeSlots.get(selectedPosition);
    }

    public void updateSlots(List<TimeSlotItem> newSlots) {
        this.timeSlots = newSlots != null ? new ArrayList<>(newSlots) : new ArrayList<>();
        clearSelection();
        notifyDataSetChanged();
    }

    public void updateTimeSlotAvailability(String timeSlot, boolean isAvailable, String bookingId) {
        for (int i = 0; i < timeSlots.size(); i++) {
            TimeSlotItem slot = timeSlots.get(i);
            if (slot.getTimeSlot().equals(timeSlot)) {
                slot.setAvailable(isAvailable);
                slot.setBookingId(bookingId);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void refreshAvailability() {
        if (currentDate == null || currentCategory == null) {
            Log.w(TAG, "Cannot refresh availability: date or category not set");
            return;
        }

        TimeSlotItem.checkTimeSlotsAvailability(timeSlots, currentCategory,
                new TimeSlotItem.AvailabilityCheckCallback() {
                    @Override
                    public void onAvailabilityChecked(List<TimeSlotItem> updatedTimeSlots) {
                        timeSlots = updatedTimeSlots;
                        notifyDataSetChanged();
                        Log.d(TAG, "Time slot availability refreshed");
                    }
                });
    }

    public void setupRealtimeListener() {
        if (currentDate == null || currentCategory == null) {
            Log.w(TAG, "Cannot setup realtime listener: date or category not set");
            return;
        }

        TimeSlotItem.setupTimeSlotListener(currentDate, currentCategory,
                new TimeSlotItem.TimeSlotListener() {
                    @Override
                    public void onTimeSlotUpdate(List<TimeSlotItem.TimeSlotUpdate> updates) {
                        if (updates != null) {
                            for (TimeSlotItem.TimeSlotUpdate update : updates) {
                                updateTimeSlotAvailability(update.timeSlot, update.isAvailable, update.bookingId);
                            }
                            Log.d(TAG, "Real-time time slot update received");
                        }
                    }
                });
    }

    public void setSelectionListener(OnTimeSlotSelectedListener listener) {
        this.selectionListener = listener;
    }

    public void setDateAndCategory(String date, String category) {
        this.currentDate = date;
        this.currentCategory = category;
    }
}