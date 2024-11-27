package com.example.firebaseauth.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.firebaseauth.model.DTRRecord
import com.example.firebaseauth.model.GeofenceData
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.maps.model.LatLng
import java.util.*
import android.Manifest


class GeofenceReceiver : BroadcastReceiver() {

    private val db = FirebaseFirestore.getInstance()

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        // Ensure geofencingEvent is not null
        geofencingEvent?.let {
            if (it.hasError()) {
                // Handle error
                return
            }

            val transition = it.geofenceTransition
            val triggerTime = Calendar.getInstance().time
            val employeeId = getUserIdFromPreferences(context)



            // Fetch the geofence data from Firestore
            db.collection("geofences")
                .document("bisu_clarin") // Use your document ID here
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val lat = document.getDouble("latitude") ?: 0.0
                        val lon = document.getDouble("longitude") ?: 0.0
                        val radius = document.getDouble("radius") ?: 0.0
                        val geofenceLatLng = LatLng(lat, lon)

                        when (transition) {
                            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                                handleEnterGeofence(context, employeeId, triggerTime, geofenceLatLng, radius)
                            }
                            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                                handleExitGeofence(employeeId, triggerTime)
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error fetching geofence: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            // Handle the case where geofencingEvent is null
        }
    }

    private fun handleEnterGeofence(context: Context, employeeId: String, time: Date, geofenceLatLng: LatLng, geofenceRadius: Double) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed with getting the location
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLocation = LatLng(location.latitude, location.longitude)

                        // Check if the current location is inside the geofence
                        val distance = FloatArray(1)
                        Location.distanceBetween(
                            location.latitude, location.longitude,
                            geofenceLatLng.latitude, geofenceLatLng.longitude,
                            distance
                        )

                        if (distance[0] <= geofenceRadius) {
                            // Proceed with clock-in logic
                            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                            if (currentHour < 12) {
                                handleMorningArrival(employeeId, time)
                            } else {
                                handleAfternoonArrival(employeeId, time)
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
            // Permission not granted, show a message or handle the case
            Toast.makeText(context, "Location permission is required to check your position.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleExitGeofence(employeeId: String, time: Date) {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        if (currentHour < 12) {
            // Morning Departure (Clock-out)
            handleMorningDeparture(employeeId, time)
        } else {
            // Afternoon Departure (Clock-out)
            handleAfternoonDeparture(employeeId, time)
        }
    }

    private fun handleMorningArrival(employeeId: String, time: Date) {
        db.collection("dtr_records")
            .document("${employeeId}-${getCurrentDate()}")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (!documentSnapshot.exists()) {
                    val dtrRecord = DTRRecord(
                        employeeId = employeeId,
                        date = time,
                        morningArrival = time,
                        morningDeparture = null,
                        afternoonArrival = null,
                        afternoonDeparture = null
                    )

                    db.collection("dtr_records")
                        .document("${employeeId}-${getCurrentDate()}")
                        .set(dtrRecord)
                        .addOnSuccessListener {
                            // Successfully saved morning arrival
                        }
                        .addOnFailureListener {
                            // Handle failure
                        }
                }
            }
            .addOnFailureListener {
                // Handle failure in checking existing DTRRecord
            }
    }

    private fun handleMorningDeparture(employeeId: String, time: Date) {
        db.collection("dtr_records")
            .whereEqualTo("employeeId", employeeId)
            .whereEqualTo("date", getCurrentDate())
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]
                    document.reference.update("morningDeparture", time)
                }
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    private fun handleAfternoonArrival(employeeId: String, time: Date) {
        db.collection("dtr_records")
            .whereEqualTo("employeeId", employeeId)
            .whereEqualTo("date", getCurrentDate())
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]
                    document.reference.update("afternoonArrival", time)
                }
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    private fun handleAfternoonDeparture(employeeId: String, time: Date) {
        db.collection("dtr_records")
            .whereEqualTo("employeeId", employeeId)
            .whereEqualTo("date", getCurrentDate())
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]
                    document.reference.update("afternoonDeparture", time)
                }
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun getUserIdFromPreferences(context: Context): String {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("employeeId", "") ?: ""
    }
}
