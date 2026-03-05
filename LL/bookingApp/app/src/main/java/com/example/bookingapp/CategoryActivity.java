package com.example.bookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;

public class CategoryActivity extends AppCompatActivity {

    // Define keys for the different category types
    private static final String CATEGORY_HAIR = "Hair";
    private static final String CATEGORY_LASHES = "Lashes";
    private static final String CATEGORY_NAILS = "Nails";

    // Intent key to pass the selected category name
    public static final String EXTRA_CATEGORY_NAME = "com.example.itmda.CATEGORY_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Link the Java class to the XML layout file
        setContentView(R.layout.activity_categories);

        // Find the three category image views
        ShapeableImageView imgHair = findViewById(R.id.img_category_1); // Braided by Merry
        ShapeableImageView imgNails = findViewById(R.id.img_category_2); // Evie Nails
        ShapeableImageView imgLashes = findViewById(R.id.img_category_3); // Lashes by Lisa

        // 1. Set Listener for Hair Category (using lambda)
        imgHair.setOnClickListener(v -> navigateToServicePage(HairStylesActivity.class, CATEGORY_HAIR));

        // 2. Set Listener for Nails Category (using lambda)
        imgNails.setOnClickListener(v -> navigateToServicePage(NailStylesActivity.class, CATEGORY_NAILS));

        // 3. Set Listener for Lashes Category (using lambda)
        imgLashes.setOnClickListener(v -> navigateToServicePage(LashStylesActivity.class, CATEGORY_LASHES));

        // Set up bottom navigation
        setupBottomNavigation();
    }

    /**
     * Helper method to create and start the Intent for the service pages.
     * @param targetActivity The class of the activity to launch (e.g., HairStylesActivity.class)
     * @param categoryName The name of the category (e.g., "Hair")
     */
    private void navigateToServicePage(Class<?> targetActivity, String categoryName) {
        Intent intent = new Intent(CategoryActivity.this, targetActivity);

        // Normalize into a slug (lowercase, no spaces)
        String slug = categoryName.trim().toLowerCase().replaceAll("\\s+", "_");

        // Pass the slug instead of the pretty name.
        intent.putExtra(EXTRA_CATEGORY_NAME, slug);

        startActivity(intent);

        // Optional: Add slide animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Set up bottom navigation with click handlers
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) return; // safe-guard

        // Use modern listener that reliably receives item selections
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Already on home (CategoryActivity), so do nothing or refresh if needed
                return true;
            } else if (itemId == R.id.nav_appointments) {
                // Navigate to Appointments Activity
                navigateToAppointments();
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Navigate to Profile Activity
                navigateToProfile();
                return true;
            }
            return false;
        });

        // Set the home item as selected by default since we're on the home screen
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    /**
     * Method to navigate to the appointments activity
     */
    private void navigateToAppointments() {
        try {
            Intent intent = new Intent(CategoryActivity.this, AppointmentsActivity.class);
            // Optional: avoid creating duplicate activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

            // Optional: Add slide animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening appointments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method to navigate to the profile activity
     */
    private void navigateToProfile() {
        try {
            Intent intent = new Intent(CategoryActivity.this, ProfileActivity.class);
            // Optional: avoid creating duplicate activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

            // Optional: Add slide animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure home icon stays selected when returning to this activity
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }
}
