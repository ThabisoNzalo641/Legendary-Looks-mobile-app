package com.example.bookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Arrays;

public class BookingConfirmationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        // Initialize Firestore and Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 1. Get ALL data passed from BookingDetailsActivity
        Intent intent = getIntent();

        // Debug: Log all incoming data
        logIncomingData(intent);

        // Make variables final for lambda usage
        final String serviceSummary = intent.getStringExtra("SELECTED_SERVICE_SUMMARY");
        final String selectedTime = intent.getStringExtra("SELECTED_TIME");
        final String fullName = intent.getStringExtra(BookingDetailsActivity.EXTRA_FULL_NAME);
        final String phoneNumber = intent.getStringExtra(BookingDetailsActivity.EXTRA_PHONE_NUMBER);
        final String notes = intent.getStringExtra(BookingDetailsActivity.EXTRA_NOTES);

        // Get additional service details - make them final
        final String serviceName = intent.getStringExtra("serviceName");
        final String servicePrice = intent.getStringExtra("servicePrice");
        final String serviceDuration = intent.getStringExtra("serviceDuration");
        final String serviceCategory = intent.getStringExtra("serviceCategory");
        final double rawPrice = intent.getDoubleExtra("rawPrice", 0);
        final int rawDuration = intent.getIntExtra("rawDuration", 0);
        final String serviceDate = intent.getStringExtra("serviceDate");
        final String serviceTime = intent.getStringExtra("serviceTime");

        // 2. Setup UI elements
        TextView tvConfirmationSummary = findViewById(R.id.tv_confirmation_summary);
        Button btnBackToHome = findViewById(R.id.btn_back_to_home);

        // 3. Display the full summary
        String summaryText = String.format(
                "Service: %s\nDate & Time: %s\nClient: %s\nContact: %s\nNotes: %s",
                serviceSummary, selectedTime, fullName, phoneNumber,
                notes.isEmpty() ? "None" : notes
        );
        tvConfirmationSummary.setText(summaryText);

        // 4. First check for time slot conflicts, then proceed
        checkTimeSlotAvailability(serviceName, serviceCategory, servicePrice, serviceDuration,
                rawPrice, rawDuration, selectedTime, fullName, phoneNumber, notes,
                serviceSummary, serviceDate, serviceTime);

        // 5. Set Listener to take the user back to the main service categories
        btnBackToHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(BookingConfirmationActivity.this, CategoryActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        });
    }

    private void logIncomingData(Intent intent) {
        System.out.println("=== INCOMING INTENT DATA ===");
        System.out.println("SELECTED_SERVICE_SUMMARY: " + intent.getStringExtra("SELECTED_SERVICE_SUMMARY"));
        System.out.println("SELECTED_TIME: " + intent.getStringExtra("SELECTED_TIME"));
        System.out.println("serviceName: " + intent.getStringExtra("serviceName"));
        System.out.println("servicePrice: " + intent.getStringExtra("servicePrice"));
        System.out.println("serviceDuration: " + intent.getStringExtra("serviceDuration"));
        System.out.println("serviceCategory: " + intent.getStringExtra("serviceCategory"));
        System.out.println("rawPrice: " + intent.getDoubleExtra("rawPrice", -1));
        System.out.println("rawDuration: " + intent.getIntExtra("rawDuration", -1));
        System.out.println("serviceDate: " + intent.getStringExtra("serviceDate"));
        System.out.println("serviceTime: " + intent.getStringExtra("serviceTime"));
        System.out.println("EXTRA_FULL_NAME: " + intent.getStringExtra(BookingDetailsActivity.EXTRA_FULL_NAME));
        System.out.println("EXTRA_PHONE_NUMBER: " + intent.getStringExtra(BookingDetailsActivity.EXTRA_PHONE_NUMBER));
        System.out.println("EXTRA_NOTES: " + intent.getStringExtra(BookingDetailsActivity.EXTRA_NOTES));
        System.out.println("============================");
    }

    private void checkTimeSlotAvailability(final String serviceName, final String serviceCategory,
                                           final String servicePrice, final String serviceDuration, final double rawPrice, final int rawDuration,
                                           final String selectedTime, final String fullName, final String phoneNumber, final String notes,
                                           final String serviceSummary, final String serviceDate, final String serviceTime) {

        // Parse date and time from the combined string if separate fields are not available
        String bookingDate = serviceDate != null ? serviceDate : extractDateFromCombined(selectedTime);
        String bookingTime = serviceTime != null ? serviceTime : extractTimeFromCombined(selectedTime);
        String normalizedCategory = normalizeCategory(serviceCategory);

        System.out.println("Checking time slot availability for:");
        System.out.println("Date: " + bookingDate);
        System.out.println("Time: " + bookingTime);
        System.out.println("Category: " + normalizedCategory);

        // Query Firestore for existing bookings with same date, time, and category
        // that are NOT cancelled or completed
        db.collection("bookings")
                .whereEqualTo("bookingDate", bookingDate)
                .whereEqualTo("bookingTime", bookingTime)
                .whereEqualTo("serviceCategory", normalizedCategory)
                .whereIn("status", Arrays.asList("pending", "confirmed", "in_progress"))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Time slot is available, proceed with booking
                        System.out.println("Time slot available, proceeding with booking");
                        fetchServiceCategoryAndSaveBooking(serviceName, serviceCategory, servicePrice, serviceDuration,
                                rawPrice, rawDuration, selectedTime, fullName, phoneNumber, notes,
                                serviceSummary, serviceDate, serviceTime);
                    } else {
                        // Time slot is already booked
                        System.out.println("Time slot already booked. Conflicting bookings: " + queryDocumentSnapshots.size());

                        // Find the conflicting booking details
                        DocumentSnapshot conflictingBooking = queryDocumentSnapshots.getDocuments().get(0);
                        String conflictingService = conflictingBooking.getString("serviceName");
                        String conflictingStatus = conflictingBooking.getString("status");

                        String errorMessage = String.format(
                                "Sorry, this time slot is already booked for %s.\n\n" +
                                        "Service: %s\n" +
                                        "Status: %s\n\n" +
                                        "Please choose a different time.",
                                normalizedCategory, conflictingService, conflictingStatus
                        );

                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();

                        // Optionally, navigate back to time selection
                        navigateBackToTimeSelection(serviceName, serviceCategory, servicePrice, serviceDuration);
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error checking time slot availability: " + e.getMessage());
                    // If we can't check availability, show warning but allow booking
                    Toast.makeText(this, "Warning: Could not verify time slot availability. Proceeding with booking.", Toast.LENGTH_LONG).show();
                    fetchServiceCategoryAndSaveBooking(serviceName, serviceCategory, servicePrice, serviceDuration,
                            rawPrice, rawDuration, selectedTime, fullName, phoneNumber, notes,
                            serviceSummary, serviceDate, serviceTime);
                });
    }

    private void navigateBackToTimeSelection(String serviceName, String serviceCategory,
                                             String servicePrice, String serviceDuration) {
        // You'll need to implement this based on your app's navigation structure
        // This should take the user back to the time selection screen with the service details preserved

        Intent intent = new Intent(this, TimeSlotItem.class); // Replace with your actual time selection activity
        intent.putExtra("serviceName", serviceName);
        intent.putExtra("serviceCategory", serviceCategory);
        intent.putExtra("servicePrice", servicePrice);
        intent.putExtra("serviceDuration", serviceDuration);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void fetchServiceCategoryAndSaveBooking(final String serviceName, final String serviceCategory,
                                                    final String servicePrice, final String serviceDuration, final double rawPrice, final int rawDuration,
                                                    final String selectedTime, final String fullName, final String phoneNumber, final String notes,
                                                    final String serviceSummary, final String serviceDate, final String serviceTime) {

        if (serviceName == null || serviceName.isEmpty()) {
            System.out.println("Service name is null or empty, using fallback category");
            // If we don't have a service name, try to extract from summary
            String extractedServiceName = extractServiceNameFromSummary(serviceSummary);
            if (extractedServiceName != null && !extractedServiceName.isEmpty()) {
                // Create a new final variable for the lambda
                final String finalServiceName = extractedServiceName;
                System.out.println("Extracted service name from summary: " + finalServiceName);

                // Query with the extracted service name
                queryFirestoreForService(finalServiceName, serviceCategory, servicePrice, serviceDuration,
                        rawPrice, rawDuration, selectedTime, fullName, phoneNumber, notes,
                        serviceSummary, serviceDate, serviceTime);
            } else {
                // Use the passed category or default
                String finalCategory = serviceCategory != null ? serviceCategory : "general";
                System.out.println("Service category is empty, using fallback: " + finalCategory);
                saveBookingToFirestore(serviceName, servicePrice, serviceDuration, finalCategory,
                        rawPrice, rawDuration, selectedTime, fullName, phoneNumber, notes,
                        serviceSummary, serviceDate, serviceTime);
                return;
            }
        } else {
            // Query with the original service name
            queryFirestoreForService(serviceName, serviceCategory, servicePrice, serviceDuration,
                    rawPrice, rawDuration, selectedTime, fullName, phoneNumber, notes,
                    serviceSummary, serviceDate, serviceTime);
        }
    }

    private void queryFirestoreForService(final String serviceName, final String serviceCategory,
                                          final String servicePrice, final String serviceDuration, final double rawPrice, final int rawDuration,
                                          final String selectedTime, final String fullName, final String phoneNumber, final String notes,
                                          final String serviceSummary, final String serviceDate, final String serviceTime) {

        System.out.println("Querying Firestore for service: " + serviceName);

        // Query the services collection to get the actual category
        db.collection("services")
                .whereEqualTo("name", serviceName)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Service found, get the category from Firestore
                        DocumentSnapshot serviceDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String actualCategory = serviceDoc.getString("category");

                        System.out.println("Found service in Firestore. Category: " + actualCategory);

                        if (actualCategory != null && !actualCategory.isEmpty()) {
                            // Use the category from Firestore
                            saveBookingToFirestore(serviceName, servicePrice, serviceDuration, actualCategory,
                                    rawPrice, rawDuration, selectedTime, fullName, phoneNumber, notes,
                                    serviceSummary, serviceDate, serviceTime);
                        } else {
                            // Category field is empty, use passed category or default
                            String fallbackCategory = serviceCategory != null ? serviceCategory : "general";
                            System.out.println("Service category is empty, using fallback: " + fallbackCategory);
                            saveBookingToFirestore(serviceName, servicePrice, serviceDuration, fallbackCategory,
                                    rawPrice, rawDuration, selectedTime, fullName, phoneNumber, notes,
                                    serviceSummary, serviceDate, serviceTime);
                        }
                    } else {
                        // Service not found in database, use passed category or default
                        String fallbackCategory = serviceCategory != null ? serviceCategory : "general";
                        System.out.println("Service not found in database, using fallback: " + fallbackCategory);
                        saveBookingToFirestore(serviceName, servicePrice, serviceDuration, fallbackCategory,
                                rawPrice, rawDuration, selectedTime, fullName, phoneNumber, notes,
                                serviceSummary, serviceDate, serviceTime);
                    }
                })
                .addOnFailureListener(e -> {
                    // If Firestore query fails, use passed category or default
                    String fallbackCategory = serviceCategory != null ? serviceCategory : "general";
                    System.out.println("Firestore query failed, using fallback: " + fallbackCategory);
                    saveBookingToFirestore(serviceName, servicePrice, serviceDuration, fallbackCategory,
                            rawPrice, rawDuration, selectedTime, fullName, phoneNumber, notes,
                            serviceSummary, serviceDate, serviceTime);
                });
    }

    private String extractServiceNameFromSummary(String serviceSummary) {
        if (serviceSummary == null || serviceSummary.isEmpty()) {
            return null;
        }

        // Try to extract service name from summary (e.g., "thick lashes - R600.0 / 50 min" -> "thick lashes")
        try {
            String[] parts = serviceSummary.split(" - ");
            if (parts.length > 0) {
                return parts[0].trim();
            }
        } catch (Exception e) {
            System.out.println("Failed to extract service name from summary: " + e.getMessage());
        }

        return serviceSummary; // Return the whole summary as fallback
    }

    private void saveBookingToFirestore(String serviceName, String servicePrice, String serviceDuration,
                                        String serviceCategory, double rawPrice, int rawDuration, String selectedTime,
                                        String fullName, String phoneNumber, String notes, String serviceSummary,
                                        String serviceDate, String serviceTime) {

        FirebaseUser currentUser = auth.getCurrentUser();
        String userEmail = currentUser != null ? currentUser.getEmail() : "";
        String userId = currentUser != null ? currentUser.getUid() : "";

        // Use raw values if available, otherwise parse from strings
        double totalPrice = rawPrice > 0 ? rawPrice : parsePrice(servicePrice);
        int durationMinutes = rawDuration > 0 ? rawDuration : parseDuration(serviceDuration);

        // Parse date and time from the combined string if separate fields are not available
        String bookingDate = serviceDate != null ? serviceDate : extractDateFromCombined(selectedTime);
        String bookingTime = serviceTime != null ? serviceTime : extractTimeFromCombined(selectedTime);

        // Normalize the category to ensure consistency
        String normalizedCategory = normalizeCategory(serviceCategory);

        // Ensure we have a service name
        String finalServiceName = serviceName != null ? serviceName :
                (serviceSummary != null ? extractServiceNameFromSummary(serviceSummary) : "Unknown Service");

        System.out.println("Final service name: " + finalServiceName);
        System.out.println("Final category: " + normalizedCategory);
        System.out.println("Final price: R" + totalPrice);

        // Create booking data that matches your Kotlin Booking data class
        Map<String, Object> booking = new HashMap<>();

        // Basic booking information
        booking.put("bookingId", ""); // Will be set after document creation
        booking.put("customerName", fullName != null ? fullName : "");
        booking.put("customerEmail", userEmail);
        booking.put("customerPhone", phoneNumber != null ? phoneNumber : "");
        booking.put("serviceId", ""); // You can populate this if you have service IDs
        booking.put("serviceName", finalServiceName);
        booking.put("serviceCategory", normalizedCategory);

        // Category-based admin assignment
        booking.put("adminId", assignAdminIdByCategory(normalizedCategory));

        // Date and time
        booking.put("bookingDate", bookingDate != null ? bookingDate : "");
        booking.put("bookingTime", bookingTime != null ? bookingTime : "");

        // Service details
        booking.put("duration", durationMinutes);
        booking.put("totalPrice", totalPrice);

        // Status and requests
        booking.put("status", "pending");
        booking.put("specialRequests", notes != null ? notes : "");

        // Timestamps
        booking.put("createdAt", Timestamp.now());
        booking.put("updatedAt", Timestamp.now());

        // User reference
        booking.put("userId", userId);

        // Add document to Firestore
        db.collection("bookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    String bookingId = documentReference.getId();

                    // Update the document with the actual bookingId
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("bookingId", bookingId);

                    documentReference.update(updateData)
                            .addOnSuccessListener(aVoid -> {
                                String adminAssigned = (String) booking.get("adminId");
                                String successMessage = String.format(
                                        "Booking confirmed!\nService: %s\nCategory: %s\nAssigned to: %s",
                                        finalServiceName, normalizedCategory, adminAssigned
                                );
                                Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show();

                                // Log the booking details for debugging
                                logBookingDetails(bookingId, fullName, finalServiceName, totalPrice,
                                        bookingDate, bookingTime, normalizedCategory, adminAssigned);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Booking created but failed to update ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create booking: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private double parsePrice(String servicePrice) {
        if (servicePrice == null || servicePrice.isEmpty()) {
            return 0.0;
        }
        try {
            // Remove currency symbols and parse
            String cleanPrice = servicePrice.replace("$", "").replace("R", "").replaceAll("[^0-9.]", "").trim();
            return Double.parseDouble(cleanPrice);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseDuration(String serviceDuration) {
        if (serviceDuration == null || serviceDuration.isEmpty()) {
            return 0;
        }
        try {
            // Extract numbers from duration string (e.g., "50 min" -> 50)
            String cleanDuration = serviceDuration.replaceAll("[^0-9]", "").trim();
            return Integer.parseInt(cleanDuration);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String extractDateFromCombined(String combinedDateTime) {
        if (combinedDateTime == null || combinedDateTime.isEmpty()) {
            return new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(new Date());
        }
        try {
            // Split by " @ " to separate date and time
            String[] parts = combinedDateTime.split(" @ ");
            return parts.length > 0 ? parts[0] : combinedDateTime;
        } catch (Exception e) {
            return combinedDateTime;
        }
    }

    private String extractTimeFromCombined(String combinedDateTime) {
        if (combinedDateTime == null || combinedDateTime.isEmpty()) {
            return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        }
        try {
            // Split by " @ " to separate date and time
            String[] parts = combinedDateTime.split(" @ ");
            return parts.length > 1 ? parts[1] : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Normalizes the category to ensure only "lashes", "hair", or "nails" are used
     */
    private String normalizeCategory(String serviceCategory) {
        if (serviceCategory == null || serviceCategory.isEmpty()) {
            return "general";
        }

        String category = serviceCategory.toLowerCase().trim();

        // Map various category names to the three main categories
        if (category.contains("lash")) {
            return "lashes";
        } else if (category.contains("hair")) {
            return "hair";
        } else if (category.contains("nail")) {
            return "nails";
        } else {
            // If it doesn't match any known category, use "general"
            return "general";
        }
    }

    /**
     * Assigns admin based on the normalized category
     */
    private String assignAdminIdByCategory(String normalizedCategory) {
        switch (normalizedCategory) {
            case "lashes":
                return "lash_admin";
            case "hair":
                return "hair_admin";
            case "nails":
                return "nail_admin";
            default:
                return "general_admin";
        }
    }

    private void logBookingDetails(String bookingId, String customerName, String serviceName,
                                   double price, String date, String time, String category, String adminAssigned) {
        System.out.println("=== BOOKING CREATED SUCCESSFULLY ===");
        System.out.println("Booking ID: " + bookingId);
        System.out.println("Customer: " + customerName);
        System.out.println("Service: " + serviceName);
        System.out.println("Category: " + category);
        System.out.println("Price: R" + price);
        System.out.println("Date: " + date);
        System.out.println("Time: " + time);
        System.out.println("Assigned Admin: " + adminAssigned);
        System.out.println("=====================================");
    }
}