package com.example.firebaseauth.activity

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaseauth.R
import com.example.firebaseauth.helper.FirestoreHelper
import com.google.firebase.firestore.FirebaseFirestore


class GeofencingActivity : AppCompatActivity() {

    private lateinit var geofenceHelper: GeofenceHelper
    private lateinit var latitudeInput: EditText
    private lateinit var longitudeInput: EditText
    private lateinit var radiusInput: EditText
    private lateinit var addGeofenceButton: Button
    private lateinit var geofenceList: RecyclerView

    private val geofenceAdapter = GeofenceAdapter(mutableListOf())
    private val firestoreHelper = FirestoreHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofencing)

        // Initialize UI elements
        geofenceHelper = GeofenceHelper(this)
        latitudeInput = findViewById(R.id.latitudeInput)
        longitudeInput = findViewById(R.id.longitudeInput)
        radiusInput = findViewById(R.id.radiusInput)
        addGeofenceButton = findViewById(R.id.addGeofenceButton)
        geofenceList = findViewById(R.id.geofenceList)

        // Set up RecyclerView
        geofenceList.layoutManager = LinearLayoutManager(this)
        geofenceList.adapter = geofenceAdapter

        // Role-based UI logic
        checkUserRole()

        // Button click listener to add geofence
        addGeofenceButton.setOnClickListener {
            val latitude = latitudeInput.text.toString().toDoubleOrNull()
            val longitude = longitudeInput.text.toString().toDoubleOrNull()
            val radius = radiusInput.text.toString().toFloatOrNull()

            if (latitude != null && longitude != null && radius != null) {
                // Add geofence to the map
                geofenceHelper.addGeofence(latitude, longitude, radius)
                geofenceAdapter.addGeofence("Geofence at ($latitude, $longitude), Radius: $radius m")
            } else {
                Toast.makeText(
                    this,
                    "Please enter valid latitude, longitude, and radius",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted
            } else {
                // Permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun saveGeofenceData(latitude: Double, longitude: Double, radius: Double) {
        // Get the Firestore instance
        val firestore = FirebaseFirestore.getInstance()

        // Create a geofence document with a unique ID (or use an existing ID)
        val geofenceData = hashMapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "radius" to radius
        )

        // Reference to the "geofences" collection
        val geofenceRef = firestore.collection("geofences").document("geofenceId") // Use unique ID

        // Set the geofence data to the Firestore document
        geofenceRef.set(geofenceData)
            .addOnSuccessListener {
                // Handle success
                Log.d("Geofence", "Geofence data saved successfully!")
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.e("Geofence", "Error saving geofence data: ${e.message}")
            }
    }

    private fun checkUserRole() {
        // Check the user's role from Firestore and handle accordingly
        firestoreHelper.getUserRole { role ->
            if (role == "admin") {
                // Admin: Show geofencing UI
                Toast.makeText(this, "Admin Access Granted", Toast.LENGTH_SHORT).show()
                // Initialize geofencing logic if needed
            } else {
                // Employee: Show restricted access UI or message
                Toast.makeText(this, "Employee Access - Geofencing Disabled", Toast.LENGTH_SHORT)
                    .show()
                // Optionally, disable certain features or redirect the user
            }
        }
    }
}