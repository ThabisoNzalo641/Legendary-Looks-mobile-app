package com.example.bookingapp

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class StatisticsActivity : AppCompatActivity() {

    private lateinit var db: com.google.firebase.firestore.FirebaseFirestore
    private var adminId: String = ""
    private var serviceCategory: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistics_activity)

        // Initialize Firestore
        db = Firebase.firestore

        // Get admin data from intent
        adminId = intent.getStringExtra("ADMIN_ID") ?: ""
        serviceCategory = intent.getStringExtra("SERVICE_CATEGORY") ?: ""

        Log.d("StatisticsActivity", "📊 Loading statistics for category: '$serviceCategory'")

        // Load statistics filtered by category
        loadStatistics()
    }

    private fun loadStatistics() {
        if (serviceCategory.isEmpty()) {
            Toast.makeText(this, "Service category not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Load total bookings for this category
        db.collection("bookings")
            .whereEqualTo("serviceCategory", serviceCategory)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("StatisticsActivity", "✅ Found ${documents.size()} bookings for category: $serviceCategory")

                val totalBookings = documents.size()
                findViewById<TextView>(R.id.totalBookingsText).text = totalBookings.toString()

                // Calculate completed bookings
                val completedBookings = documents.count { document ->
                    document.getString("status")?.lowercase() == "completed"
                }
                findViewById<TextView>(R.id.completedBookingsText).text = completedBookings.toString()

                // Calculate pending bookings (pending + confirmed)
                val pendingBookings = documents.count { document ->
                    val status = document.getString("status")?.lowercase()
                    status == "pending" || status == "confirmed"
                }
                findViewById<TextView>(R.id.pendingBookingsText).text = pendingBookings.toString()

                // Calculate total revenue using batch approach
                calculateTotalRevenueBatch(documents)
            }
            .addOnFailureListener { e ->
                Log.e("StatisticsActivity", "❌ Error loading booking statistics: ${e.message}")
                Toast.makeText(this, "Error loading booking statistics: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Load services count for this category
        loadServicesCount()
    }

    private fun calculateTotalRevenueBatch(bookingDocuments: com.google.firebase.firestore.QuerySnapshot) {
        if (bookingDocuments.isEmpty) {
            findViewById<TextView>(R.id.totalRevenueText).text = "R0.00"
            Log.d("StatisticsActivity", "💰 No bookings found for revenue calculation")
            return
        }

        Log.d("StatisticsActivity", "💰 Starting batch revenue calculation for ${bookingDocuments.size()} bookings")

        // Get all unique service names from bookings
        val serviceNames = mutableSetOf<String>()
        val serviceIds = mutableSetOf<String>()

        for (bookingDocument in bookingDocuments) {
            val serviceName = bookingDocument.getString("serviceName")
            val serviceId = bookingDocument.getString("serviceId")

            serviceName?.takeIf { it.isNotEmpty() }?.let { serviceNames.add(it) }
            serviceId?.takeIf { it.isNotEmpty() }?.let { serviceIds.add(it) }
        }

        Log.d("StatisticsActivity", "🔍 Found ${serviceNames.size} unique service names: $serviceNames")
        Log.d("StatisticsActivity", "🔍 Found ${serviceIds.size} unique service IDs: $serviceIds")

        if (serviceNames.isEmpty() && serviceIds.isEmpty()) {
            findViewById<TextView>(R.id.totalRevenueText).text = "R0.00"
            Log.d("StatisticsActivity", "❌ No service names or IDs found in bookings")
            return
        }

        // Create a combined query to get all services at once
        val servicesQuery = db.collection("services")
            .whereEqualTo("category", serviceCategory)

        servicesQuery.get()
            .addOnSuccessListener { serviceDocuments ->
                Log.d("StatisticsActivity", "✅ Found ${serviceDocuments.size()} services in category: $serviceCategory")

                // Create maps for service lookup
                val servicePriceByName = mutableMapOf<String, Double>()
                val servicePriceById = mutableMapOf<String, Double>()

                for (serviceDoc in serviceDocuments) {
                    val name = serviceDoc.getString("name")
                    val id = serviceDoc.id
                    val price = serviceDoc.getDouble("price") ?: 0.0

                    name?.let { servicePriceByName[it] = price }
                    servicePriceById[id] = price

                    Log.d("StatisticsActivity", "   💰 Service: $name (ID: $id) - Price: R$price")
                }

                // Calculate total revenue by matching bookings to services
                var totalRevenue = 0.0
                var matchedBookings = 0
                var unmatchedBookings = 0

                for (bookingDocument in bookingDocuments) {
                    val serviceName = bookingDocument.getString("serviceName")
                    val serviceId = bookingDocument.getString("serviceId")
                    var priceFound = 0.0

                    // Try to find price by service ID first (more reliable)
                    if (!serviceId.isNullOrEmpty()) {
                        priceFound = servicePriceById[serviceId] ?: 0.0
                    }

                    // If not found by ID, try by name
                    if (priceFound == 0.0 && !serviceName.isNullOrEmpty()) {
                        priceFound = servicePriceByName[serviceName] ?: 0.0
                    }

                    if (priceFound > 0) {
                        totalRevenue += priceFound
                        matchedBookings++
                        Log.d("StatisticsActivity", "   ✅ Matched: $serviceName - R$priceFound")
                    } else {
                        unmatchedBookings++
                        Log.w("StatisticsActivity", "   ⚠️ No price found for: $serviceName (ID: $serviceId)")
                    }
                }

                findViewById<TextView>(R.id.totalRevenueText).text = "R${"%.2f".format(totalRevenue)}"

                Log.d("StatisticsActivity", "🎉 BATCH CALCULATION COMPLETE")
                Log.d("StatisticsActivity", "📊 Matched: $matchedBookings, Unmatched: $unmatchedBookings")
                Log.d("StatisticsActivity", "💰 TOTAL REVENUE: R$totalRevenue")

                if (unmatchedBookings > 0) {
                    Toast.makeText(this, "Revenue calculated (${unmatchedBookings} bookings unmatched)", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Revenue calculation completed!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("StatisticsActivity", "❌ Error fetching services for revenue calculation: ${e.message}")
                findViewById<TextView>(R.id.totalRevenueText).text = "R0.00"
                Toast.makeText(this, "Error calculating revenue: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadServicesCount() {
        db.collection("services")
            .whereEqualTo("category", serviceCategory)
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { documents ->
                val totalServices = documents.size()
                findViewById<TextView>(R.id.totalServicesText).text = totalServices.toString()

                Log.d("StatisticsActivity", "🛠️ Found $totalServices active services for category: $serviceCategory")
            }
            .addOnFailureListener { e ->
                Log.e("StatisticsActivity", "❌ Error loading service statistics: ${e.message}")
                Toast.makeText(this, "Error loading service statistics: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}