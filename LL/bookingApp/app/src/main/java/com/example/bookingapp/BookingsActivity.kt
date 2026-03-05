package com.example.bookingapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class BookingsActivity : AppCompatActivity() {

    private lateinit var db: com.google.firebase.firestore.FirebaseFirestore
    private var adminId: String = ""
    private var serviceCategory: String = ""
    private lateinit var bookingsRecyclerView: RecyclerView
    private lateinit var noBookingsText: TextView
    private lateinit var bookingsAdapter: BookingsAdapter
    private val bookingsList = mutableListOf<Booking>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bookings_activity)

        // Initialize Firestore
        db = Firebase.firestore

        // Get admin data from intent
        adminId = intent.getStringExtra("ADMIN_ID") ?: ""
        serviceCategory = intent.getStringExtra("SERVICE_CATEGORY") ?: ""

        Log.d("BookingsActivity", "🎯 BookingsActivity started")
        Log.d("BookingsActivity", "Admin ID: '$adminId'")
        Log.d("BookingsActivity", "Service Category: '$serviceCategory'")

        // Initialize views
        bookingsRecyclerView = findViewById(R.id.bookingsRecyclerView)
        noBookingsText = findViewById(R.id.noBookingsText)

        // Setup RecyclerView with the updated adapter
        bookingsAdapter = BookingsAdapter(bookingsList) { booking, position ->
            // This lambda will be called when the "Done" button is clicked
            markBookingAsDone(booking, position)
        }
        bookingsRecyclerView.layoutManager = LinearLayoutManager(this)
        bookingsRecyclerView.adapter = bookingsAdapter

        // Load bookings
        loadBookings()
    }

    private fun loadBookings() {
        Log.d("BookingsActivity", "🔄 Starting to load bookings by category: '$serviceCategory'")

        // Show loading state
        noBookingsText.text = "Loading bookings..."
        noBookingsText.visibility = View.VISIBLE
        bookingsRecyclerView.visibility = View.GONE

        val bookingsCollection = db.collection("bookings")

        // Filter by service category instead of admin ID
        val query = if (serviceCategory.isNotEmpty()) {
            Log.d("BookingsActivity", "🔍 Filtering by category: '$serviceCategory'")
            bookingsCollection.whereEqualTo("serviceCategory", serviceCategory)
        } else {
            Log.d("BookingsActivity", "🔍 Showing ALL bookings (no category filter)")
            bookingsCollection
        }

        query.get()
            .addOnSuccessListener { documents ->
                Log.d("BookingsActivity", "✅ Query successful. Found ${documents.size()} documents")

                bookingsList.clear()

                if (documents.isEmpty) {
                    Log.d("BookingsActivity", "❌ No documents found in query")
                    updateUI()
                    return@addOnSuccessListener
                }

                var validBookingsCount = 0
                var matchingBookingsCount = 0

                for (document in documents) {
                    try {
                        val booking = document.toObject(Booking::class.java)
                        // Set the document ID as bookingId
                        booking.bookingId = document.id

                        Log.d("BookingsActivity", "📄 Booking: ${booking.serviceName} | Category: '${booking.serviceCategory}' | Admin: '${booking.adminId}' | Status: ${booking.status}")

                        // Check if this booking matches our category filter
                        val matchesCategory = serviceCategory.isEmpty() ||
                                booking.serviceCategory.equals(serviceCategory, ignoreCase = true)

                        if (matchesCategory) {
                            matchingBookingsCount++
                        }

                        // Only add bookings that have basic required data AND match our category filter
                        if ((booking.serviceName.isNotEmpty() || booking.customerName.isNotEmpty()) && matchesCategory) {
                            bookingsList.add(booking)
                            validBookingsCount++
                            Log.d("BookingsActivity", "✅ ADDED: ${booking.serviceName} (Category: ${booking.serviceCategory})")
                        } else {
                            Log.d("BookingsActivity", "❌ SKIPPED - Missing data or wrong category")
                        }

                    } catch (e: Exception) {
                        Log.e("BookingsActivity", "❌ Error parsing booking document: ${e.message}")
                    }
                }

                // Sort by creation date (newest first) manually
                bookingsList.sortByDescending { it.createdAt?.seconds ?: 0 }

                Log.d("BookingsActivity", "📊 Results: $matchingBookingsCount match category, $validBookingsCount valid bookings")
                updateUI()

                // Show appropriate message based on results
                when {
                    validBookingsCount > 0 -> {
                        Toast.makeText(this, "Loaded $validBookingsCount $serviceCategory bookings", Toast.LENGTH_SHORT).show()
                    }
                    serviceCategory.isNotEmpty() -> {
                        Toast.makeText(this, "No bookings found for $serviceCategory", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(this, "No bookings found in the system", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("BookingsActivity", "❌ Firestore query failed: ${e.message}")
                Toast.makeText(this, "Error loading bookings: ${e.message}", Toast.LENGTH_LONG).show()
                noBookingsText.text = "Failed to load bookings. Please check your connection."
                noBookingsText.visibility = View.VISIBLE
                bookingsRecyclerView.visibility = View.GONE
            }
    }

    private fun updateUI() {
        if (bookingsList.isEmpty()) {
            if (serviceCategory.isNotEmpty()) {
                noBookingsText.text = "No bookings found for $serviceCategory"
            } else {
                noBookingsText.text = "No bookings found"
            }
            noBookingsText.visibility = View.VISIBLE
            bookingsRecyclerView.visibility = View.GONE
            Log.d("BookingsActivity", "📱 UI Updated: No bookings to show")
        } else {
            noBookingsText.visibility = View.GONE
            bookingsRecyclerView.visibility = View.VISIBLE
            bookingsAdapter.notifyDataSetChanged()
            Log.d("BookingsActivity", "📱 UI Updated: Showing ${bookingsList.size} bookings")
        }
    }

    private fun markBookingAsDone(booking: Booking, position: Int) {
        Log.d("BookingsActivity", "🎯 Marking booking as done: ${booking.serviceName}")

        // Update the booking status in Firestore
        db.collection("bookings").document(booking.bookingId)
            .update("status", "completed")
            .addOnSuccessListener {
                Log.d("BookingsActivity", "✅ Booking marked as completed in Firestore")

                // Remove the booking from the local list
                bookingsList.removeAt(position)

                // Notify the adapter about the item removal
                bookingsAdapter.notifyItemRemoved(position)

                // Update the UI in case the list becomes empty
                updateUI()

                Toast.makeText(this, "Booking marked as completed!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("BookingsActivity", "❌ Failed to update booking status: ${e.message}")
                Toast.makeText(this, "Failed to mark as completed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Refresh functionality
    fun refreshBookings(view: View) {
        Log.d("BookingsActivity", "🔄 Manual refresh triggered")
        loadBookings()
        Toast.makeText(this, "Refreshing bookings...", Toast.LENGTH_SHORT).show()
    }
}