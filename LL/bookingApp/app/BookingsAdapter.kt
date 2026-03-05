package com.example.bookingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookingsAdapter(private val bookings: List<Booking>) :
    RecyclerView.Adapter<BookingsAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerName: TextView = itemView.findViewById(R.id.customerNameTextView)
        val serviceName: TextView = itemView.findViewById(R.id.serviceNameTextView)
        val bookingDate: TextView = itemView.findViewById(R.id.bookingDateTextView)
        val bookingTime: TextView = itemView.findViewById(R.id.bookingTimeTextView)
        val status: TextView = itemView.findViewById(R.id.statusTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.booking_item, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.customerName.text = booking.customerName
        holder.serviceName.text = booking.serviceName
        holder.bookingDate.text = "Date: ${booking.bookingDate}"
        holder.bookingTime.text = "Time: ${booking.bookingTime}"
        holder.status.text = "Status: ${booking.status}"
    }

    override fun getItemCount(): Int = bookings.size
}