package com.example.bookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class BookingDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_FULL_NAME = "EXTRA_FULL_NAME";
    public static final String EXTRA_PHONE_NUMBER = "EXTRA_PHONE_NUMBER";
    public static final String EXTRA_NOTES = "EXTRA_NOTES";

    // We'll use these to hold the previous screen's data
    private String selectedServiceSummary;
    private String selectedTimeSlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        // 1. Get data passed from BookingSelectActivity
        Intent intent = getIntent();
        selectedServiceSummary = intent.getStringExtra("SELECTED_SERVICE_SUMMARY");
        selectedTimeSlot = intent.getStringExtra("SELECTED_TIME");

        // 2. Setup UI elements
        ImageButton backButton = findViewById(R.id.btn_back);
        TextView summaryTextView = findViewById(R.id.tv_summary_service);
        Button confirmButton = findViewById(R.id.btn_confirm_pay);

        TextInputEditText etFullName = findViewById(R.id.et_full_name);
        TextInputEditText etPhoneNumber = findViewById(R.id.et_phone_number);
        TextInputEditText etNotes = findViewById(R.id.et_notes);

        // Display the data received from the previous screen
        if (selectedServiceSummary != null && selectedTimeSlot != null) {
            summaryTextView.setText(String.format("%s on %s", selectedServiceSummary, selectedTimeSlot));
        } else if (selectedServiceSummary != null) {
            summaryTextView.setText(selectedServiceSummary);
        }

        // 3. Back Button Listener
        backButton.setOnClickListener(v -> finish());

        // 4. Confirm Button Listener (validate inputs then move to PaymentActivity)
        confirmButton.setOnClickListener(v -> {
            String fullName = (etFullName.getText() != null) ? etFullName.getText().toString().trim() : "";
            String phoneNumber = (etPhoneNumber.getText() != null) ? etPhoneNumber.getText().toString().trim() : "";
            String notes = (etNotes.getText() != null) ? etNotes.getText().toString().trim() : "";

            // Basic validation
            if (TextUtils.isEmpty(fullName)) {
                etFullName.setError("Please enter your full name");
                etFullName.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(phoneNumber)) {
                etPhoneNumber.setError("Please enter phone number");
                etPhoneNumber.requestFocus();
                return;
            }
            if (phoneNumber.length() < 6) {
                etPhoneNumber.setError("Enter a valid phone number");
                etPhoneNumber.requestFocus();
                return;
            }

            // Create intent for the PaymentActivity
            Intent paymentIntent = new Intent(BookingDetailsActivity.this, PaymentActivity.class);

            // Pass ALL collected data forward (keys match PaymentActivity)
            paymentIntent.putExtra("SELECTED_SERVICE_SUMMARY", selectedServiceSummary);
            paymentIntent.putExtra("SELECTED_TIME", selectedTimeSlot);
            paymentIntent.putExtra(EXTRA_FULL_NAME, fullName);
            paymentIntent.putExtra(EXTRA_PHONE_NUMBER, phoneNumber);
            paymentIntent.putExtra(EXTRA_NOTES, notes);

            // Pass ALL service metadata including category for Firestore
            String rawServiceName = intent.getStringExtra("serviceName");
            String rawServicePrice = intent.getStringExtra("servicePrice");
            String rawServiceDuration = intent.getStringExtra("serviceDuration");
            String rawServiceImageUrl = intent.getStringExtra("serviceImageUrl");
            String serviceCategory = intent.getStringExtra("serviceCategory");
            double rawPrice = intent.getDoubleExtra("rawPrice", 0);
            int rawDuration = intent.getIntExtra("rawDuration", 0);
            String serviceDate = intent.getStringExtra("serviceDate");
            String serviceTime = intent.getStringExtra("serviceTime");

            // Pass all data to PaymentActivity
            if (rawServiceName != null) paymentIntent.putExtra("serviceName", rawServiceName);
            if (rawServicePrice != null) paymentIntent.putExtra("servicePrice", rawServicePrice);
            if (rawServiceDuration != null) paymentIntent.putExtra("serviceDuration", rawServiceDuration);
            if (rawServiceImageUrl != null) paymentIntent.putExtra("serviceImageUrl", rawServiceImageUrl);
            if (serviceCategory != null) paymentIntent.putExtra("serviceCategory", serviceCategory);
            paymentIntent.putExtra("rawPrice", rawPrice);
            paymentIntent.putExtra("rawDuration", rawDuration);
            if (serviceDate != null) paymentIntent.putExtra("serviceDate", serviceDate);
            if (serviceTime != null) paymentIntent.putExtra("serviceTime", serviceTime);

            startActivity(paymentIntent);
        });

        // Optional: Pre-fill user data if available
        prefillUserData(etFullName, etPhoneNumber);
    }

    /**
     * Optional method to pre-fill user data if user is logged in
     */
    private void prefillUserData(TextInputEditText etFullName, TextInputEditText etPhoneNumber) {
        // You can integrate with your user management system here
        // For example, if you have a User singleton or SharedPreferences
        /*
        User currentUser = UserManager.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getFullName() != null && etFullName.getText().toString().isEmpty()) {
                etFullName.setText(currentUser.getFullName());
            }
            if (currentUser.getPhoneNumber() != null && etPhoneNumber.getText().toString().isEmpty()) {
                etPhoneNumber.setText(currentUser.getPhoneNumber());
            }
        }
        */
    }
}