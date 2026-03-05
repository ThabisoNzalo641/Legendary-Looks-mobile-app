package com.example.bookingapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class PaymentActivity extends AppCompatActivity {

    // Extra keys to hold data passed from previous screens (Client Details)
    private String serviceSummary;
    private String selectedTime;
    private String fullName;
    private String phoneNumber;
    private String notes;

    // UI
    private LinearLayout layoutCashPayment;
    private LinearLayout layoutPaySharp;
    private TextView tvPaySharpId;
    private Button processPaymentButton;
    private ImageButton backButton;

    // Payment selection state
    private boolean isCashSelected = false;
    private boolean isPaySharpSelected = false;

    // The PaySharp ID shown in the XML
    private static final String PAYSHARP_ID = "061905024@StandardBank";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // 1. Get ALL data passed from BookingDetailsActivity (essential for Confirmation)
        Intent intent = getIntent();
        serviceSummary = intent.getStringExtra("SELECTED_SERVICE_SUMMARY");
        selectedTime = intent.getStringExtra("SELECTED_TIME");
        // We assume Client details were sent via these extras (from BookingDetailsActivity)
        fullName = intent.getStringExtra(BookingDetailsActivity.EXTRA_FULL_NAME);
        phoneNumber = intent.getStringExtra(BookingDetailsActivity.EXTRA_PHONE_NUMBER);
        notes = intent.getStringExtra(BookingDetailsActivity.EXTRA_NOTES);

        // 2. Setup UI elements
        backButton = findViewById(R.id.btn_back);
        processPaymentButton = findViewById(R.id.btn_process_payment);

        layoutCashPayment = findViewById(R.id.layout_cash_payment);
        layoutPaySharp = findViewById(R.id.layout_paysharp);
        tvPaySharpId = findViewById(R.id.tv_paysharp_id);

        // Ensure the PaySharp ID text matches the constant (in case XML was changed)
        if (tvPaySharpId != null) {
            tvPaySharpId.setText(PAYSHARP_ID);
        }

        // 3. Back Button Listener
        backButton.setOnClickListener(v -> finish());

        // 4. Select cash payment when the cash layout is clicked
        layoutCashPayment.setOnClickListener(v -> {
            selectCash();
        });

        // 5. Select PaySharp when layout clicked; also copy ID to clipboard for convenience
        layoutPaySharp.setOnClickListener(v -> {
            selectPaySharp();
            copyPaySharpToClipboard();
        });

        // Tapping the PaySharp ID text also selects and copies (good UX)
        tvPaySharpId.setOnClickListener(v -> {
            selectPaySharp();
            copyPaySharpToClipboard();
        });

        // 6. Process Payment Listener (Transition to Confirmation Screen)
        processPaymentButton.setOnClickListener(v -> {
            if (validatePaymentSelection()) {
                Intent confirmationIntent = new Intent(PaymentActivity.this, BookingConfirmationActivity.class);

                // Pass ALL booking data to the Confirmation screen
                confirmationIntent.putExtra("SELECTED_SERVICE_SUMMARY", serviceSummary);
                confirmationIntent.putExtra("SELECTED_TIME", selectedTime);
                confirmationIntent.putExtra(BookingDetailsActivity.EXTRA_FULL_NAME, fullName);
                confirmationIntent.putExtra(BookingDetailsActivity.EXTRA_PHONE_NUMBER, phoneNumber);
                confirmationIntent.putExtra(BookingDetailsActivity.EXTRA_NOTES, notes);

                // Also pass selected payment method (optional, useful for confirmation screen)
                if (isCashSelected) {
                    confirmationIntent.putExtra("SELECTED_PAYMENT_METHOD", "CASH");
                } else if (isPaySharpSelected) {
                    confirmationIntent.putExtra("SELECTED_PAYMENT_METHOD", "PAYSHARP");
                    confirmationIntent.putExtra("PAYSHARP_ID", PAYSHARP_ID);
                }

                startActivity(confirmationIntent);
            } else {
                Toast.makeText(PaymentActivity.this, "Please select a payment method.", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize visuals
        updateSelectionVisuals();
    }

    // Selectors
    private void selectCash() {
        isCashSelected = true;
        isPaySharpSelected = false;
        updateSelectionVisuals();
    }

    private void selectPaySharp() {
        isPaySharpSelected = true;
        isCashSelected = false;
        updateSelectionVisuals();
    }

    // Update UI backgrounds to indicate selection
    private void updateSelectionVisuals() {
        // Safe-guards (views may be null in some edge cases)
        if (layoutCashPayment != null) {
            if (isCashSelected) {
                layoutCashPayment.setBackgroundColor(Color.parseColor("#FFF3E0")); // light highlight
            } else {
                layoutCashPayment.setBackgroundColor(Color.WHITE);
            }
        }

        if (layoutPaySharp != null) {
            if (isPaySharpSelected) {
                layoutPaySharp.setBackgroundColor(Color.parseColor("#FFF3E0"));
            } else {
                layoutPaySharp.setBackgroundColor(Color.WHITE);
            }
        }
    }

    // Copies the PAYSHARP_ID to clipboard and shows a toast
    private void copyPaySharpToClipboard() {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("PaySharp ID", PAYSHARP_ID);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "PaySharp ID copied to clipboard", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Clipboard unavailable", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Could not copy PaySharp ID", Toast.LENGTH_SHORT).show();
        }
    }

    // Validation: ensure a payment method was selected
    private boolean validatePaymentSelection() {
        return isCashSelected || isPaySharpSelected;
    }
}

