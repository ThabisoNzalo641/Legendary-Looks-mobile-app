package com.example.bookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AppointmentsActivity extends AppCompatActivity {

    private static final String TAG = "AppointmentsActivity";
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private RecyclerView upcomingRecyclerView;
    private RecyclerView completedRecyclerView;
    private ProgressBar progressBar;
    private TextView tvNoUpcoming, tvNoCompleted;
    private View nestedScrollView;

    private List<Appointment> upcomingList;
    private List<Appointment> completedList;
    private AppointmentAdapter upcomingAdapter;
    private AppointmentAdapter completedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);

        Log.d(TAG, "🎯 AppointmentsActivity started");

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Setup UI elements
        initializeViews();

        // -------------------------------------------------------
        // ✅ FORCE HIGHLIGHT APPOINTMENTS TAB + ENABLE TAB CLICKS
        // -------------------------------------------------------
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            // highlight correct tab
            bottomNav.setSelectedItemId(R.id.nav_appointments);

            // handle tab switching
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    Intent intent = new Intent(AppointmentsActivity.this, CategoryActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                    return true;
                }

                if (id == R.id.nav_appointments) {
                    return true; // already here
                }

                if (id == R.id.nav_profile) {
                    Intent intent = new Intent(AppointmentsActivity.this, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                    return true;
                }

                return false;
            });
        }
        // -------------------------------------------------------

        // Check authentication and load appointments
        if (checkUserAuthentication()) {
            loadUserAppointments();
        }
    }

    private boolean checkUserAuthentication() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Log.e(TAG, "🚨🚨🚨 NO USER AUTHENTICATED");
            Toast.makeText(this, "Please log in to view appointments", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();

            return false;
        }

        Log.d(TAG, "✅ USER AUTHENTICATED SUCCESSFULLY");
        return true;
    }

    private void initializeViews() {
        ImageButton backButton = findViewById(R.id.btn_back);
        upcomingRecyclerView = findViewById(R.id.rv_upcoming_appointments);
        completedRecyclerView = findViewById(R.id.rv_completed_appointments);
        progressBar = findViewById(R.id.progressBar);
        tvNoUpcoming = findViewById(R.id.tv_no_upcoming);
        tvNoCompleted = findViewById(R.id.tv_no_completed);
        nestedScrollView = findViewById(R.id.nested_scroll_view);

        // Back Button
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AppointmentsActivity.this, CategoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        upcomingList = new ArrayList<>();
        completedList = new ArrayList<>();

        upcomingAdapter = new AppointmentAdapter(upcomingList);
        completedAdapter = new AppointmentAdapter(completedList);

        upcomingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        completedRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        upcomingRecyclerView.setAdapter(upcomingAdapter);
        completedRecyclerView.setAdapter(completedAdapter);

        progressBar.setVisibility(View.VISIBLE);
        nestedScrollView.setVisibility(View.GONE);

        Log.d(TAG, "✅ UI initialized successfully");
    }

    private void loadUserAppointments() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            progressBar.setVisibility(View.GONE);
            tvNoUpcoming.setText("Please log in to view appointments");
            tvNoUpcoming.setVisibility(View.VISIBLE);
            return;
        }

        String userEmail = currentUser.getEmail();
        Log.d(TAG, "🔍 Loading appointments for: " + userEmail);

        db.collection("bookings")
                .whereEqualTo("customerEmail", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    nestedScrollView.setVisibility(View.VISIBLE);

                    if (task.isSuccessful()) {
                        upcomingList.clear();
                        completedList.clear();

                        int totalDocs = (task.getResult() != null) ? task.getResult().size() : 0;

                        if (totalDocs == 0) {
                            checkBookingsByUserId(currentUser.getUid());
                            return;
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Appointment appointment = document.toObject(Appointment.class);
                                appointment.setBookingId(document.getId());

                                String status = appointment.getStatus();
                                if (status != null && isUpcomingStatus(status)) {
                                    upcomingList.add(appointment);
                                } else if ("completed".equalsIgnoreCase(status)) {
                                    completedList.add(appointment);
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing appointment", e);
                            }
                        }

                        updateUI();

                    } else {
                        Toast.makeText(this, "Failed to load appointments", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkBookingsByUserId(String userId) {
        db.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    tvNoUpcoming.setText("No appointments found");
                    tvNoUpcoming.setVisibility(View.VISIBLE);
                    tvNoCompleted.setText("No completed appointments");
                    tvNoCompleted.setVisibility(View.VISIBLE);
                });
    }

    private boolean isUpcomingStatus(String status) {
        if (status == null) return false;

        String lower = status.toLowerCase();
        return lower.equals("pending") ||
                lower.equals("confirmed") ||
                lower.equals("approved");
    }

    private void updateUI() {

        // upcoming
        if (upcomingList.isEmpty()) {
            tvNoUpcoming.setVisibility(View.VISIBLE);
            upcomingRecyclerView.setVisibility(View.GONE);
        } else {
            tvNoUpcoming.setVisibility(View.GONE);
            upcomingRecyclerView.setVisibility(View.VISIBLE);
            upcomingAdapter.notifyDataSetChanged();
        }

        // completed
        if (completedList.isEmpty()) {
            tvNoCompleted.setVisibility(View.VISIBLE);
            completedRecyclerView.setVisibility(View.GONE);
        } else {
            tvNoCompleted.setVisibility(View.GONE);
            completedRecyclerView.setVisibility(View.VISIBLE);
            completedAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (auth.getCurrentUser() != null) {
            loadUserAppointments();
        }
    }
}
