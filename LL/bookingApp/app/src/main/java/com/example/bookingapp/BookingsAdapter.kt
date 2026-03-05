package com.example.bookingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class BookingsAdapter(
    private val bookings: MutableList<Booking>,
    private val onDoneClickListener: (Booking, Int) -> Unit
) : RecyclerView.Adapter<BookingsAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serviceName: TextView = itemView.findViewById(R.id.serviceNameTextView)
        val bookingDate: TextView = itemView.findViewById(R.id.bookingDateTextView)
        val status: TextView = itemView.findViewById(R.id.statusTextView)
        val customerName: TextView = itemView.findViewById(R.id.customerNameTextView)
        val doneButton: Button = itemView.findViewById(R.id.doneButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.booking_item, parent, false) // Make sure this matches your XML layout name
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]

        // Set service name with fallback
        holder.serviceName.text = if (booking.serviceName.isNotEmpty()) {
            booking.serviceName
        } else {
            "Unnamed Service"
        }

        // Set customer name with fallback
        holder.customerName.text = if (booking.customerName.isNotEmpty()) {
            "Customer: ${booking.customerName}"
        } else {
            "Customer: Not specified"
        }

        // Format and set booking date/time
        holder.bookingDate.text = formatBookingDateTime(booking.bookingDate, booking.bookingTime)

        // Set status with color coding and better formatting
        val statusText = formatStatus(booking.status)
        holder.status.text = statusText

        // Set status color
        when (booking.status.lowercase()) {
            "completed" -> holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
            "confirmed" -> holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_blue_dark))
            "pending" -> holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_orange_dark))
            "cancelled" -> holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
            else -> holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray))
        }

        // Set up the Done button
        holder.doneButton.setOnClickListener {
            // Call the lambda with the booking and its position
            onDoneClickListener(booking, position)
        }

        // Hide the Done button for already completed or cancelled bookings
        val status = booking.status.lowercase()
        if (status == "completed" || status == "cancelled") {
            holder.doneButton.visibility = View.GONE
        } else {
            holder.doneButton.visibility = View.VISIBLE
            holder.doneButton.text = when (status) {
                "confirmed" -> "Mark Done"
                else -> "Confirm & Done"
            }
        }

        // Optional: Add click listener for more details
        holder.itemView.setOnClickListener {
            showQuickBookingDetails(booking)
        }
    }

    override fun getItemCount(): Int = bookings.size

    private fun formatBookingDateTime(date: String, time: String): String {
        return when {
            date.isNotEmpty() && time.isNotEmpty() -> "$date • $time"
            date.isNotEmpty() -> date
            time.isNotEmpty() -> time
            else -> "Date/time not specified"
        }
    }

    private fun formatStatus(status: String): String {
        return when (status.lowercase()) {
            "pending" -> "⏳ Pending"
            "confirmed" -> "✅ Confirmed"
            "completed" -> "🎉 Completed"
            "cancelled" -> "❌ Cancelled"
            else -> "📋 ${status.replaceFirstChar { it.uppercase() }}"
        }
    }

    private fun showQuickBookingDetails(booking: Booking) {
        // You can show a Toast with basic info or implement a detailed dialog
        val details = """
            Service: ${booking.serviceName}
            Customer: ${booking.customerName}
            Date: ${booking.bookingDate} ${booking.bookingTime}
            Status: ${booking.status}
        """.trimIndent()

        // For now, just log to console
        println("Booking details: $details")
    }

    // Method to remove a booking from the adapter (called from Activity)
    fun removeBooking(position: Int) {
        if (position in 0 until bookings.size) {
            bookings.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    // Method to update the entire dataset
    fun updateBookings(newBookings: MutableList<Booking>) {
        bookings.clear()
        bookings.addAll(newBookings)
        notifyDataSetChanged()
    }
}