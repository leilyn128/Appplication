package com.example.firebaseauth.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import kotlin.math.pow
import kotlin.math.sqrt

object GeofenceUtils {
    fun validateGeofenceAccess(
        fusedLocationClient: FusedLocationProviderClient,
        geofenceLatitude: Double,
        geofenceLongitude: Double,
        geofenceRadius: Double,  // Convert to Float if needed
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        if (isWithinGeofence(location, geofenceLatitude, geofenceLongitude, geofenceRadius)) {
                            onSuccess()
                        } else {
                            onFailure("You must be within the geofenced area to clock in.")
                        }
                    } else {
                        onFailure("Unable to fetch location. Please check your GPS settings.")
                    }
                }
                .addOnFailureListener {
                    onFailure("Failed to retrieve location. Please try again.")
                }
        } catch (e: SecurityException) {
            onFailure("Permission denied. Cannot access location.")
        }
    }
}

    private fun isWithinGeofence(
        location: Location,
        geofenceLatitude: Double,
        geofenceLongitude: Double,
        geofenceRadius: Double
    ): Boolean {
        // Calculate the distance between the current location and the geofence center
        val distance = calculateDistance(
            location.latitude,
            location.longitude,
            geofenceLatitude,
            geofenceLongitude
        )
        return distance <= geofenceRadius
    }

    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val earthRadius = 6371000.0 // Radius of the Earth in meters

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2).pow(2.0) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2).pow(2.0)
        val c = 2 * Math.atan2(sqrt(a), sqrt(1 - a))
        return (earthRadius * c).toFloat()
    }

