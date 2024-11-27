package com.example.firebaseauth.model

data class DTRRecord(
    val day: Int,
    var amArrival: String? = null,  // Nullable type
    var amDeparture: String? = null,  // Nullable type
    var pmArrival: String? = null,  // Nullable type
    var pmDeparture: String? = null,  // Nullable type
    var photoUrl: String? = null // URL of the photo stored in Firebase
)
