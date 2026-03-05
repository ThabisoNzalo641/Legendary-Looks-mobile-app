package com.example.bookingapp

data class User(
    val userId: String = "",
    val email: String = "",
    val userType: String = "",
    val adminId: String = "",
    val serviceCategory: String = "",
    val displayName: String = "",
    val createdAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
)