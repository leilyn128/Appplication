package com.example.firebaseauth.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.firebaseauth.model.DTRRecord
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class GeofenceReceiver : BroadcastReceiver() {

    private val db = FirebaseFirestore.getInstance()

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)


        geofencingEvent?.let {
            if (it.hasError()) {

                return
            }

            val transition = it.geofenceTransition
            val triggerTime = Calendar.getInstance().time
            val email = getEmailFromFirebaseAuth()

            if (email.isNullOrEmpty()) {
                Toast.makeText(context, "User email not found", Toast.LENGTH_SHORT).show()
                return
            }

            db.collection("geofences")
                .document("bisu_clarin")
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val lat = document.getDouble("latitude") ?: 0.0
                        val lon = document.getDouble("longitude") ?: 0.0
                        val radius = document.getDouble("radius") ?: 0.0
                        val geofenceLatLng = LatLng(lat, lon)

                        when (transition) {
                            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                                handleEnterGeofence(context, email, triggerTime, geofenceLatLng, radius)
                            }
                            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                                handleExitGeofence(context,email, triggerTime)
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error fetching geofence: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {

        }
    }

    private fun handleEnterGeofence(context: Context, email: String, time: Date, geofenceLatLng: LatLng, geofenceRadius: Double) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLocation = LatLng(location.latitude, location.longitude)

                        val distance = FloatArray(1)
                        android.location.Location.distanceBetween(
                            location.latitude, location.longitude,
                            geofenceLatLng.latitude, geofenceLatLng.longitude,
                            distance
                        )

                        if (distance[0] <= geofenceRadius) {
                            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                            if (currentHour < 12) {
                                handleMorningArrival(context,email, time)
                            } else {
                                handleAfternoonArrival(context,email, time)
                            }
                        } else {
                            Toast.makeText(context, "You must be inside the geofenced area to clock in.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Unable to get location.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Location permission is required to check your position.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleExitGeofence(context: Context,email: String, time: Date) {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        if (currentHour < 12) {
            handleMorningDeparture(context,email, time)
        } else {
            handleAfternoonDeparture(context,email, time)
        }
    }

    private fun handleMorningArrival(context: Context, email: String, time: Date) {
        val currentDate = getCurrentDate()
        val documentId = currentDate // Use the current date as the document ID

        db.collection("dtr_records")
            .document(email) // Main document for the user (email as ID)
            .collection("daily_records") // Subcollection for daily records
            .document(documentId) // Document for the current date
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (!documentSnapshot.exists()) {
                    // Create a new record if it doesn't exist
                    val dtrRecord = DTRRecord(
                        email = email,
                        date = time,
                        morningArrival = time,
                        morningDeparture = null,
                        afternoonArrival = null,
                        afternoonDeparture = null
                    )

                    db.collection("dtr_records")
                        .document(email)
                        .collection("daily_records")
                        .document(documentId)
                        .set(dtrRecord)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Morning Arrival Recorded.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to record Morning Arrival.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Morning Arrival already recorded for today.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error checking DTR record.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleMorningDeparture(context: Context,email: String, time: Date) {
        val currentDate = getCurrentDate()
        val documentId = currentDate

        db.collection("dtr_records")
            .document(email)
            .collection("daily_records")
            .document(documentId)
            .update("morningDeparture", time)
            .addOnSuccessListener {
                Toast.makeText(context, "Morning Departure Recorded.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to record Morning Departure.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleAfternoonArrival(context: Context,email: String, time: Date) {
        val currentDate = getCurrentDate()
        val documentId = currentDate

        db.collection("dtr_records")
            .document(email)
            .collection("daily_records")
            .document(documentId)
            .update("afternoonArrival", time)
            .addOnSuccessListener {
                Toast.makeText(context, "Afternoon Arrival Recorded.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to record Afternoon Arrival.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleAfternoonDeparture(context: Context,email: String, time: Date) {
        val currentDate = getCurrentDate()
        val documentId = currentDate

        db.collection("dtr_records")
            .document(email)
            .collection("daily_records")
            .document(documentId)
            .update("afternoonDeparture", time)
            .addOnSuccessListener {
                Toast.makeText(context, "Afternoon Departure Recorded.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to record Afternoon Departure.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun getEmailFromFirebaseAuth(): String? {
        return FirebaseAuth.getInstance().currentUser?.email
    }
}

