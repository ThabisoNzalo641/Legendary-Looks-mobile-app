package com.example.bookingapp

data class Admin(
    val adminId: String = "",
    val businessName: String = "",
    val email: String = "",
    val serviceType: String = "",
    val phone: String = "",
    val description: String = "",
    val isActive: Boolean = true,
    val workingHours: Map<String, Map<String, Any>> = mapOf(),
    val createdAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
)

data class WorkingHours(
    val start: String = "",
    val end: String = "",
    val isOpen: Boolean = true
)
