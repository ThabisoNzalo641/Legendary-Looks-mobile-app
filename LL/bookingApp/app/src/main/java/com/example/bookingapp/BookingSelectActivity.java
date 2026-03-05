package com.example.bookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingSelectActivity extends AppCompatActivity
        implements TimeSlotAdapter.OnTimeSlotSelectedListener {

    private Button nextButton;
    private TextView tvSelectedServiceSummary;
    private CalendarView calendarView;
    private RecyclerView timeSlotsRecyclerView;
    private ImageView ivServiceImage;

    private TimeSlotItem selectedTimeSlot = null;
    private long selectedDateMillis = -1;
    private String serviceCategory;
    private TimeSlotAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_select);

        ImageButton backButton = findViewById(R.id.btn_back);
        tvSelectedServiceSummary = findViewById(R.id.tv_selected_service_summary);
        calendarView = findViewById(R.id.calendar_view);
        timeSlotsRecyclerView = findViewById(R.id.rv_time_slots);
        nextButton = findViewById(R.id.btn_next_step);
        ivServiceImage = findViewById(R.id.iv_service_image);

        nextButton.setEnabled(false);
        backButton.setOnClickListener(v -> finish());

        String serviceName = safeGetStringExtra("serviceName");
        String servicePrice = safeGetStringExtra("servicePrice");
        String serviceDuration = safeGetStringExtra("serviceDuration");
        String serviceImageUrl = safeGetStringExtra("serviceImageUrl");
        serviceCategory = safeGetStringExtra("serviceCategory");
        double rawPrice = getIntent().getDoubleExtra("rawPrice", 0);
        int rawDuration = getIntent().getIntExtra("rawDuration", 0);

        String priceText = (servicePrice == null || servicePrice.isEmpty()) ? "" : servicePrice;
        String durationText = (serviceDuration == null || serviceDuration.isEmpty()) ? "" : (serviceDuration + " min");
        String nameText = (serviceName == null || serviceName.isEmpty()) ? "Service Selected" : serviceName;
        String summary = nameText;
        if (!priceText.isEmpty() || !durationText.isEmpty()) {
            summary += " - " + priceText + (durationText.isEmpty() ? "" : " / " + durationText);
        }
        tvSelectedServiceSummary.setText(summary);

        if (ivServiceImage != null && serviceImageUrl != null && !serviceImageUrl.isEmpty()) {
            Glide.with(this).load(serviceImageUrl).into(ivServiceImage);
        }

        selectedDateMillis = calendarView.getDate();
        initializeTimeSlots();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, dayOfMonth, 0, 0, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            selectedDateMillis = cal.getTimeInMillis();

            selectedTimeSlot = null;
            if (adapter != null) {
                adapter.clearSelection();
            }
            nextButton.setEnabled(false);
            refreshTimeSlotsForSelectedDate();
        });

        String finalSummary = summary;
        nextButton.setOnClickListener(v -> {
            if (selectedDateMillis <= 0) {
                Toast.makeText(this, "Please select a date.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedTimeSlot == null) {
                Toast.makeText(this, "Please select a time slot.", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedDateStr = formatDate(selectedDateMillis);
            TimeSlotItem.checkSingleTimeSlotAvailability(selectedDateStr, selectedTimeSlot.getTimeSlot(), serviceCategory,
                    new TimeSlotItem.SingleAvailabilityCallback() {
                        @Override
                        public void onAvailabilityChecked(boolean isAvailable) {
                            runOnUiThread(() -> {
                                if (isAvailable) {
                                    proceedToBookingDetails(finalSummary, selectedDateStr);
                                } else {
                                    Toast.makeText(BookingSelectActivity.this,
                                            "This time slot was just booked. Please select another time.",
                                            Toast.LENGTH_LONG).show();
                                    refreshTimeSlotsForSelectedDate();
                                }
                            });
                        }
                    });
        });
    }

    private void initializeTimeSlots() {
        String selectedDateStr = formatDate(selectedDateMillis);
        List<TimeSlotItem> timeSlots = TimeSlotItem.createTimeSlotsForDate(selectedDateStr);

        adapter = new TimeSlotAdapter(timeSlots, this, selectedDateStr, serviceCategory);
        timeSlotsRecyclerView.setAdapter(adapter);
        timeSlotsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        adapter.refreshAvailability();
        adapter.setupRealtimeListener();
    }

    private void refreshTimeSlotsForSelectedDate() {
        String selectedDateStr = formatDate(selectedDateMillis);
        List<TimeSlotItem> timeSlots = TimeSlotItem.createTimeSlotsForDate(selectedDateStr);

        if (adapter != null) {
            adapter.setDateAndCategory(selectedDateStr, serviceCategory);
            adapter.updateSlots(timeSlots);
            adapter.refreshAvailability();
        }
    }

    private void proceedToBookingDetails(String finalSummary, String dateStr) {
        String timeStr = selectedTimeSlot.getTimeSlot();
        String dateTimeCombined = dateStr + " @ " + timeStr;

        Intent intent = new Intent(BookingSelectActivity.this, BookingDetailsActivity.class);

        intent.putExtra("SELECTED_SERVICE_SUMMARY", finalSummary);
        intent.putExtra("SELECTED_TIME", dateTimeCombined);

        intent.putExtra("serviceName", safeGetStringExtra("serviceName"));
        intent.putExtra("servicePrice", safeGetStringExtra("servicePrice"));
        intent.putExtra("serviceDuration", safeGetStringExtra("serviceDuration"));
        intent.putExtra("serviceImageUrl", safeGetStringExtra("serviceImageUrl"));
        intent.putExtra("serviceCategory", serviceCategory);
        intent.putExtra("rawPrice", getIntent().getDoubleExtra("rawPrice", 0));
        intent.putExtra("rawDuration", getIntent().getIntExtra("rawDuration", 0));
        intent.putExtra("serviceDate", dateStr);
        intent.putExtra("serviceTime", timeStr);

        startActivity(intent);
    }

    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    private String safeGetStringExtra(String key) {
        Intent i = getIntent();
        return (i != null && i.hasExtra(key)) ? i.getStringExtra(key) : null;
    }

    @Override
    public void onTimeSlotSelected(TimeSlotItem selectedSlot) {
        this.selectedTimeSlot = selectedSlot;
        nextButton.setEnabled(true);
        Toast.makeText(this, "Selected: " + selectedSlot.getTimeSlot(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimeSlotUnavailable(TimeSlotItem unavailableSlot) {
        String message = String.format(
                "This time slot (%s) is already booked.\n\n" +
                        "It will become available again when the admin marks the current booking as completed.",
                unavailableSlot.getTimeSlot()
        );

        Toast.makeText(this, "This time slot is already booked", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}