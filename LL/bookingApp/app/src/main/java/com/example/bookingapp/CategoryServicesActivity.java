package com.example.bookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoryServicesActivity extends AppCompatActivity {

    private static final String TAG = "CategoryServices";
    private RecyclerView recyclerView;
    private StyleAdapter adapter;
    private ProgressBar progressBar;
    private List<StyleItem> serviceList;
    private FirebaseFirestore db;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_services);

        Log.d(TAG, "🎯🎯🎯 onCreate STARTED");

        // Get the category from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("category")) {
            category = intent.getStringExtra("category");
            Log.d(TAG, "📥 Received category: " + category);
        } else {
            category = "hair";
            Log.w(TAG, "❌ No category in intent, using default: " + category);
        }

        Log.d(TAG, "🔄 Calling initializeFirebase...");
        initializeFirebase();

        Log.d(TAG, "🔄 Calling initializeViews...");
        initializeViews();

        Log.d(TAG, "🔄 Calling loadCategoryServices...");
        loadCategoryServices();

        Log.d(TAG, "✅✅✅ onCreate COMPLETED");
    }

    private void initializeFirebase() {
        Log.d(TAG, "🔄 Initializing Firebase...");
        try {
            db = FirebaseFirestore.getInstance();
            Log.d(TAG, "✅ Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "❌ Firebase initialization failed: " + e.getMessage(), e);
        }
    }

    private void initializeViews() {
        Log.d(TAG, "🔄 initializeViews STARTED");

        try {
            Log.d(TAG, "🔍 Finding progressBar...");
            progressBar = findViewById(R.id.progressBar);
            if (progressBar == null) {
                Log.e(TAG, "❌❌❌ progressBar is NULL! ID: R.id.progressBar");
            } else {
                Log.d(TAG, "✅ progressBar found");
            }

            Log.d(TAG, "🔍 Finding recyclerView...");
            recyclerView = findViewById(R.id.recyclerView);
            if (recyclerView == null) {
                Log.e(TAG, "❌❌❌ recyclerView is NULL! ID: R.id.recyclerView");
                return;
            } else {
                Log.d(TAG, "✅ recyclerView found");
            }

            Log.d(TAG, "🔄 Creating serviceList...");
            serviceList = new ArrayList<>();
            Log.d(TAG, "✅ serviceList created with size: " + serviceList.size());

            // Setup RecyclerView with GridLayoutManager
            Log.d(TAG, "🔄 Setting LayoutManager...");
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            Log.d(TAG, "✅ LayoutManager set");

            // Create adapter
            Log.d(TAG, "🔄 Creating StyleAdapter...");
            int buttonBackgroundId = R.drawable.button_gradient;
            Log.d(TAG, "Button background ID: " + buttonBackgroundId);

            adapter = new StyleAdapter(this, serviceList, buttonBackgroundId);

            if (adapter == null) {
                Log.e(TAG, "❌❌❌ ADAPTER CREATION FAILED - adapter is NULL!");
                return;
            }
            Log.d(TAG, "✅ StyleAdapter created successfully");

            Log.d(TAG, "🔄 Setting adapter to recyclerView...");
            recyclerView.setAdapter(adapter);
            Log.d(TAG, "✅ Adapter set to recyclerView");

            // ⭐⭐⭐ CRITICAL: Set click listener
            Log.d(TAG, "🔄🔄🔄 ABOUT TO SET CLICK LISTENER...");
            Log.d(TAG, "Adapter is null: " + (adapter == null));

            if (adapter != null) {
                Log.d(TAG, "🎯 Calling setOnItemClickListener...");
                adapter.setOnItemClickListener(new StyleAdapter.OnItemClickListener() {
                    @Override
                    public void onBookButtonClick(int position, StyleItem serviceItem) {
                        Log.d(TAG, "🎯🎯🎯 BOOK BUTTON CLICK HANDLED: " + serviceItem.getName());
                        openBookingActivity(serviceItem);
                    }

                    @Override
                    public void onItemClick(int position, StyleItem serviceItem) {
                        Log.d(TAG, "🎯🎯🎯 CARD CLICK HANDLED: " + serviceItem.getName());
                        openServiceDetails(serviceItem);
                    }
                });
                Log.d(TAG, "✅✅✅✅✅ CLICK LISTENER SET SUCCESSFULLY!");
            } else {
                Log.e(TAG, "❌❌❌ CANNOT SET LISTENER - ADAPTER IS NULL!");
            }

            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            Log.d(TAG, "✅ initializeViews COMPLETED");

        } catch (Exception e) {
            Log.e(TAG, "❌❌❌ ERROR in initializeViews: " + e.getMessage(), e);
        }
    }

    private void loadCategoryServices() {
        Log.d(TAG, "🔄 loadCategoryServices STARTED for category: " + category);

        if (db == null) {
            Log.e(TAG, "❌❌❌ Firebase db is NULL!");
            return;
        }

        db.collection("services")
                .whereEqualTo("category", category)
                .get()
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "🔄 Firestore query completed");
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "✅ Firestore query successful");
                        serviceList.clear();
                        QuerySnapshot querySnapshot = task.getResult();

                        if (querySnapshot != null) {
                            Log.d(TAG, "📥 Found " + querySnapshot.size() + " documents");
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                try {
                                    StyleItem service = document.toObject(StyleItem.class);
                                    service.setId(document.getId());
                                    serviceList.add(service);

                                    Log.d(TAG, "📦 Loaded: " + service.getName());
                                } catch (Exception e) {
                                    Log.e(TAG, "❌ Error parsing document: " + e.getMessage());
                                }
                            }

                            Log.d(TAG, "🔄 Updating adapter with " + serviceList.size() + " items");
                            if (adapter != null) {
                                adapter.updateData(serviceList);
                                Log.d(TAG, "✅ Adapter updated with data");
                            } else {
                                Log.e(TAG, "❌❌❌ ADAPTER IS NULL during data update!");
                            }
                        }
                    } else {
                        Log.e(TAG, "❌ Firestore query failed: " + task.getException());
                    }
                });
    }

    private void openBookingActivity(StyleItem serviceItem) {
        Log.d(TAG, "🚀 Opening BookingSelectActivity for: " + serviceItem.getName());
        Intent intent = new Intent(this, BookingSelectActivity.class);
        intent.putExtra("service_id", serviceItem.getId());
        intent.putExtra("service_name", serviceItem.getName());
        intent.putExtra("service_price", serviceItem.getPrice());
        intent.putExtra("service_duration", serviceItem.getDuration());
        intent.putExtra("service_image", serviceItem.getImageUrl());
        startActivity(intent);
    }

    private void openServiceDetails(StyleItem serviceItem) {
        Log.d(TAG, "📖 Opening service details for: " + serviceItem.getName());
        openBookingActivity(serviceItem);
    }
}