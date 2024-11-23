package com.example.firebaseauth.activity

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.firebaseauth.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class GeofenceHelper(private val context: Context) {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    companion object {
        private const val REQUEST_CODE_LOCATION_PERMISSION = 1
    }

    // Check if location permissions are granted
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Request location permissions
    fun requestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_CODE_LOCATION_PERMISSION
        )
    }

    // Add geofence if permission is granted
    fun addGeofence(lat: Double, lon: Double, radius: Float) {
        if (hasLocationPermission()) {
            try {
                val geofence = Geofence.Builder()
                    .setRequestId("geofence_id_${lat}_${lon}")
                    .setCircularRegion(lat, lon, radius)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()

                val geofencingRequest = GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build()

                geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                    .addOnSuccessListener {
                        Log.d("GeofenceHelper", "Geofence added successfully")
                    }
                    .addOnFailureListener {
                        Log.d("GeofenceHelper", "Failed to add geofence: ${it.message}")
                    }
            } catch (e: SecurityException) {
                Log.e("GeofenceHelper", "SecurityException: ${e.message}")
            }
        } else {
            Log.e("GeofenceHelper", "Location permission is required")
            // Request permission or inform the user
        }
    }

    // PendingIntent for GeofenceBroadcastReceiver
    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}
