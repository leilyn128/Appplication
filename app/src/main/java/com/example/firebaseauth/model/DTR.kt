package com.example.firebaseauth.model

data class DTR(
    val date: String, // Date in string format (e.g., "YYYY-MM-DD")
    val morningArrival: String? = null,
    val morningDeparture: String? = null,
    val afternoonArrival: String? = null,
    val afternoonDeparture: String? = null,
    val remarks: String? = null // Optional remarks
)

