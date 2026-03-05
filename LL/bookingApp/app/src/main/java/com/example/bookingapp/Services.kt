package com.example.bookingapp

data class Service(
    var serviceId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val duration: Int = 0,
    val category: String = "",
    val adminId: String = "",
    val isActive: Boolean = true,
    val imageUrl: String? = null,
    val createdAt: com.google.firebase.Timestamp? = null
)