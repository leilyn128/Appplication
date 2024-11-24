package com.example.firebaseauth.model



data class GeofenceData(
    val geofenceId: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float
)
