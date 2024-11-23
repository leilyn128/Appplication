package com.example.firebaseauth.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Safely retrieve the GeofencingEvent
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            Log.e("GeofenceReceiver", "GeofencingEvent is null")
            return
        }

        // Check if there are errors in the geofencing event
        if (geofencingEvent.hasError()) {
            val errorCode = geofencingEvent.errorCode
            Log.e("GeofenceReceiver", "Geofencing error: $errorCode")
            return
        }

        // Get the transition type (e.g., enter or exit)
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Handle enter/exit transitions
        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d("GeofenceReceiver", "Entered geofence")
                // Add logic to handle the "enter" transition
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d("GeofenceReceiver", "Exited geofence")
                // Add logic to handle the "exit" transition
            }
            else -> {
                Log.e("GeofenceReceiver", "Unknown geofence transition: $geofenceTransition")
            }
        }
    }
}

