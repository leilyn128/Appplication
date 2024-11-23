package com.example.dtrapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.GeofencingEvent
import android.util.Log
import com.google.android.gms.location.Geofence

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofenceEvent = GeofencingEvent.fromIntent(intent)
        if (geofenceEvent?.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // Log or trigger a notification, or take action on entering geofence
            Log.d("GeofenceReceiver", "User has entered the geofence")
            // You could also trigger the camera to capture photo, etc.
        }
    }
}
