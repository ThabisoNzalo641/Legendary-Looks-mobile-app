package com.example.bookingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;



class DashboardActivity : AppCompatActivity() {

    private lateinit var db: com.google.firebase.firestore.FirebaseFirestore
    private var adminId: String = ""
    private var serviceCategory: String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_activity)

        // Initialize Firestore
        db = Firebase.firestore

        // Get admin data from intent
        adminId = intent.getStringExtra("ADMIN_ID") ?: ""
        serviceCategory = intent.getStringExtra("SERVICE_CATEGORY") ?: ""

        // Load admin data and update UI
        loadAdminData()

        // Card click listeners
        val bookingsCard = findViewById<CardView>(R.id.bookingsCard)
        val statisticsCard = findViewById<CardView>(R.id.statisticsCard)
        val uploadServiceCard = findViewById<CardView>(R.id.uploadServiceCard)
        val updateServiceCard = findViewById<CardView>(R.id.updateServiceCard)
        val deleteServiceCard = findViewById<CardView>(R.id.deleteServiceCard)

        bookingsCard.setOnClickListener {
            val intent = Intent(this, BookingsActivity::class.java)
            intent.putExtra("ADMIN_ID", adminId)
            intent.putExtra("SERVICE_CATEGORY", serviceCategory)
            startActivity(intent)
        }

        statisticsCard.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            intent.putExtra("ADMIN_ID", adminId)
            intent.putExtra("SERVICE_CATEGORY", serviceCategory)
            startActivity(intent)
        }

        uploadServiceCard.setOnClickListener {
            val intent = Intent(this, UploadServiceActivity::class.java)
            intent.putExtra("ADMIN_ID", adminId)
            intent.putExtra("SERVICE_CATEGORY", serviceCategory)
            startActivity(intent)
        }

        updateServiceCard.setOnClickListener {
            val intent = Intent(this, UpdateServiceActivity::class.java)
            intent.putExtra("ADMIN_ID", adminId)
            intent.putExtra("SERVICE_CATEGORY", serviceCategory)
            startActivity(intent)
        }

        deleteServiceCard.setOnClickListener {
            val intent = Intent(this, DeleteServiceActivity::class.java)
            intent.putExtra("ADMIN_ID", adminId)
            intent.putExtra("SERVICE_CATEGORY", serviceCategory)
            startActivity(intent)
        }
    }

    private fun loadAdminData() {
        if (adminId.isNotEmpty()) {
            db.collection("admins").document(adminId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Try different possible field names for business name
                        val businessName = document.getString("businessName")
                            ?: document.getString("companyName")
                            ?: document.getString("name")
                            ?: getDefaultBusinessName()

                        updateWelcomeText(businessName)

                        // Debug: Print all document fields to see what's available
                        println("DEBUG: Admin document data: ${document.data}")
                    } else {
                        // Admin document doesn't exist
                        Toast.makeText(this, "Admin profile not found", Toast.LENGTH_SHORT).show()
                        updateWelcomeText(getDefaultBusinessName())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading admin data: ${e.message}", Toast.LENGTH_SHORT).show()
                    updateWelcomeText(getDefaultBusinessName())
                }
        } else {
            updateWelcomeText(getDefaultBusinessName())
        }
    }

    private fun updateWelcomeText(businessName: String) {
        val welcomeTextView = findViewById<TextView>(R.id.welcomeTextView)
        welcomeTextView.text = "Welcome to $businessName"
    }

    private fun getDefaultBusinessName(): String {
        return when (serviceCategory.toLowerCase()) {
            "lashes" -> "Lashes by Lisa"
            "nails" -> "Eviie Nails"
            "hair" -> "Braided by Nerry"
            else -> "Your Business"
        }
    }
}