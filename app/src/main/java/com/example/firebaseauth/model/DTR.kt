package com.example.firebaseauth.model

data class DTRRecord(
    val date: String, // Use LocalDate if you need a specific date type
    val morningArrival: String?,
    val morningDeparture: String?,
    val afternoonArrival: String?,
    val afternoonDeparture: String?,
    val timestamp: Long? = null // Add the timestamp to track when the DTR is saved
)
