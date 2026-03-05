package com.example.bookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class LashStylesActivity extends AppCompatActivity {
    private static final String TAG = "LashStylesActivity";

    private RecyclerView recyclerView;
    private StyleAdapter adapter;
    private ProgressBar progressBar;
    private List<StyleItem> lashStyleList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lash_styles);

        initializeFirebase();
        initializeViews();
        loadLashServicesFromFirestore();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.rv_lash_styles);
        progressBar = findViewById(R.id.progressBar);

        lashStyleList = new ArrayList<>();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        int darkButtonId = R.drawable.button_gradient_dark;
        adapter = new StyleAdapter(this, lashStyleList, darkButtonId);

        // Set click listener for Book buttons
        adapter.setOnItemClickListener(new StyleAdapter.OnItemClickListener() {
            @Override
            public void onBookButtonClick(int position, StyleItem serviceItem) {
                Log.d(TAG, "🎯 Book button clicked for: " + serviceItem.getName());
                openBookingActivity(serviceItem);
            }

            @Override
            public void onItemClick(int position, StyleItem serviceItem) {
                Log.d(TAG, "🎯 Card clicked for: " + serviceItem.getName());
                openBookingActivity(serviceItem);
            }
        });

        recyclerView.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void loadLashServicesFromFirestore() {
        Log.d(TAG, "=== QUERYING FOR LASH SERVICES ===");

        db.collection("services")
                .whereEqualTo("category", "lashes")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    if (task.isSuccessful()) {
                        lashStyleList.clear();
                        QuerySnapshot querySnapshot = task.getResult();

                        Log.d(TAG, "Found " + querySnapshot.size() + " lash service documents");

                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                try {
                                    Log.d(TAG, "📄 Document ID: " + document.getId());
                                    Log.d(TAG, "📄 Full document data: " + document.getData());

                                    // Debug individual fields
                                    String name = document.getString("name");
                                    Double price = document.getDouble("price");
                                    Long duration = document.getLong("duration");
                                    String category = document.getString("category");

                                    Log.d(TAG, "🔍 Field check - Name: " + name +
                                            ", Price: " + price + ", Duration: " + duration + ", Category: " + category);

                                    StyleItem service = document.toObject(StyleItem.class);
                                    service.setId(document.getId());
                                    lashStyleList.add(service);

                                    Log.d(TAG, "✅ Added service: " + service.getName() +
                                            " | Price: " + service.getPrice() + " | Duration: " + service.getDuration());

                                } catch (Exception e) {
                                    Log.e(TAG, "❌ Error parsing service data: " + e.getMessage(), e);
                                }
                            }

                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "🎉 Successfully loaded " + lashStyleList.size() + " lash services");

                            if (lashStyleList.isEmpty()) {
                                Toast.makeText(this, "No lash services found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d(TAG, "❌ No lash service documents found");
                            Toast.makeText(this, "No lash services available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "❌ Error loading lash services: " + task.getException());
                        Toast.makeText(this, "Error loading services", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ⭐⭐⭐ UPDATED: Use correct intent extra keys
    private void openBookingActivity(StyleItem serviceItem) {
        Intent intent = new Intent(this, BookingSelectActivity.class);

        // Use the same keys that BookingSelectActivity expects
        intent.putExtra("serviceName", serviceItem.getName());
        intent.putExtra("servicePrice", "R" + serviceItem.getPrice());
        intent.putExtra("serviceDuration", serviceItem.getDuration() + " min");
        intent.putExtra("serviceImageUrl", serviceItem.getImageUrl());
        intent.putExtra("serviceCategory", "lashes"); // Hardcode since we're in lashes activity
        intent.putExtra("rawPrice", serviceItem.getPrice());
        intent.putExtra("rawDuration", serviceItem.getDuration());

        // Log what we're passing
        Log.d(TAG, "=== PASSING TO BOOKING SELECT ===");
        Log.d(TAG, "serviceName: " + serviceItem.getName());
        Log.d(TAG, "serviceCategory: lashes");
        Log.d(TAG, "rawPrice: " + serviceItem.getPrice());
        Log.d(TAG, "rawDuration: " + serviceItem.getDuration());
        Log.d(TAG, "=================================");

        startActivity(intent);
    }
}