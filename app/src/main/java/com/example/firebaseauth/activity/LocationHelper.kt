package com.example.firebaseauth.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationServices


class LocationHelper(
private val context: Context,
private val onLocationUpdate: (LatLng) -> Unit
) {
    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // LocationRequest initialization
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 5000L
    ).setMinUpdateIntervalMillis(2000L)  // Update frequency every 2 seconds
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)  // High accuracy
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location: Location? = locationResult.lastLocation
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                onLocationUpdate(latLng)
            }
        }
    }


    // Starts location updates (without permission handling)
    fun startLocationUpdates(){
        // Start location updates only if permission is granted
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, null
            )
        }
    }
    // Stops location updates
    fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}
