package com.example.firebaseauth

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Ensure intent is not null before using it
        intent?.let {
            val geofenceEvent = GeofencingEvent.fromIntent(it)  // Use 'it' as the non-null intent

            if (geofenceEvent != null) {
                // Handle geofence event if it's valid
                when (geofenceEvent.geofenceTransition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        Log.d("GeofenceReceiver", "User has entered the geofence")
                        // Perform actions such as notifying the user or updating a database
                    }
                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        Log.d("GeofenceReceiver", "User has exited the geofence")
                        // Perform actions on exit
                    }
                    Geofence.GEOFENCE_TRANSITION_DWELL -> {
                        Log.d("GeofenceReceiver", "User is dwelling in the geofence")
                        // Perform actions for dwell (optional)
                    }
                    else -> {
                        Log.e("GeofenceReceiver", "Unknown geofence transition")
                    }
                }
            } else {
                Log.e("GeofenceReceiver", "Geofencing event is null")
            }
        } ?: Log.e("GeofenceReceiver", "Received null intent")

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        // No binding required for this service
        return null
    }
}
