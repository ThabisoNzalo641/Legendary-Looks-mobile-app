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

public class HairStylesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StyleAdapter adapter;
    private ProgressBar progressBar;
    private List<StyleItem> hairStyleList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hair_styles);

        initializeFirebase();
        initializeViews();
        loadHairServicesFromFirestore();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.rv_hair_styles);
        progressBar = findViewById(R.id.progressBar);

        hairStyleList = new ArrayList<>();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        int purpleButtonId = R.drawable.gradient_purple_dark;;
        adapter = new StyleAdapter(this, hairStyleList, purpleButtonId);

        // Set click listener for Book buttons
        adapter.setOnItemClickListener(new StyleAdapter.OnItemClickListener() {
            @Override
            public void onBookButtonClick(int position, StyleItem serviceItem) {
                Log.d("HairStyles", "🎯 Book button clicked for: " + serviceItem.getName());
                openBookingActivity(serviceItem);
            }

            @Override
            public void onItemClick(int position, StyleItem serviceItem) {
                Log.d("HairStyles", "🎯 Card clicked for: " + serviceItem.getName());
                openBookingActivity(serviceItem);
            }
        });

        recyclerView.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void loadHairServicesFromFirestore() {
        Log.d("FirebaseDebug", "=== QUERYING FOR HAIR SERVICES ===");

        db.collection("services")
                .whereEqualTo("category", "hair")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    if (task.isSuccessful()) {
                        hairStyleList.clear();
                        QuerySnapshot querySnapshot = task.getResult();

                        Log.d("FirebaseDebug", "Found " + querySnapshot.size() + " hair service documents");

                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                try {
                                    Log.d("FirebaseDebug", "📄 Document ID: " + document.getId());
                                    Log.d("FirebaseDebug", "📄 Full document data: " + document.getData());

                                    // Debug individual fields
                                    String name = document.getString("name");
                                    Double price = document.getDouble("price");
                                    Long duration = document.getLong("duration");
                                    String category = document.getString("category");

                                    Log.d("FirebaseDebug", "🔍 Field check - Name: " + name +
                                            ", Price: " + price + ", Duration: " + duration + ", Category: " + category);

                                    StyleItem service = document.toObject(StyleItem.class);
                                    service.setId(document.getId());
                                    hairStyleList.add(service);

                                    Log.d("FirebaseDebug", "✅ Added service: " + service.getName() +
                                            " | Price: " + service.getPrice() + " | Duration: " + service.getDuration());

                                } catch (Exception e) {
                                    Log.e("FirestoreData", "❌ Error parsing service data: " + e.getMessage(), e);
                                }
                            }

                            adapter.notifyDataSetChanged();
                            Log.d("FirestoreData", "🎉 Successfully loaded " + hairStyleList.size() + " hair services");

                            if (hairStyleList.isEmpty()) {
                                Toast.makeText(this, "No hair services found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("FirebaseDebug", "❌ No hair service documents found");
                            Toast.makeText(this, "No hair services available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("FirestoreData", "❌ Error loading hair services: " + task.getException());
                        Toast.makeText(this, "Error loading services", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ⭐⭐⭐ UPDATED: Use correct intent extra keys
    private void openBookingActivity(StyleItem styleItem) {
        Intent intent = new Intent(this, BookingSelectActivity.class);

        // Use the same keys that BookingSelectActivity expects
        intent.putExtra("serviceName", styleItem.getName());
        intent.putExtra("servicePrice", "R" + styleItem.getPrice());
        intent.putExtra("serviceDuration", styleItem.getDuration() + " min");
        intent.putExtra("serviceImageUrl", styleItem.getImageUrl());
        intent.putExtra("serviceCategory", "hair"); // Hardcode since we're in hair activity
        intent.putExtra("rawPrice", styleItem.getPrice());
        intent.putExtra("rawDuration", styleItem.getDuration());

        // Log what we're passing
        Log.d("HairStyles", "=== PASSING TO BOOKING SELECT ===");
        Log.d("HairStyles", "serviceName: " + styleItem.getName());
        Log.d("HairStyles", "serviceCategory: hair");
        Log.d("HairStyles", "rawPrice: " + styleItem.getPrice());
        Log.d("HairStyles", "rawDuration: " + styleItem.getDuration());
        Log.d("HairStyles", "=================================");

        startActivity(intent);
    }
}