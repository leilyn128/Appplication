package com.example.firebaseauth.activity

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaseauth.R
import com.example.firebaseauth.helper.FirestoreHelper


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
                Toast.makeText(this, "Please enter valid latitude, longitude, and radius", Toast.LENGTH_SHORT).show()
            }
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
                Toast.makeText(this, "Employee Access - Geofencing Disabled", Toast.LENGTH_SHORT).show()
                // Optionally, disable certain features or redirect the user
            }
        }
    }
}
