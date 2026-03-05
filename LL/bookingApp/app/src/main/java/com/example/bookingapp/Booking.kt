package com.example.bookingapp

data class Booking(
    var bookingId: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val serviceCategory: String = "",
    val adminId: String = "",
    val bookingDate: String = "",
    val bookingTime: String = "",
    val duration: Int = 0,
    val totalPrice: Double = 0.0,
    val status: String = "pending",
    val specialRequests: String = "",
    val createdAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val updatedAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
)