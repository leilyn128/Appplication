package com.example.firebaseauth.model

data class DTRRecord(
    val day: Int,
    var amArrival: String = "",
    var amDeparture: String = "",
    var pmArrival: String = "",
    var pmDeparture: String = "",
    var photoUrl: String? = null // URL of the photo stored in Firebase
)
