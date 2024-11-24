package com.example.firebaseauth.pages

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.example.firebaseauth.R
import com.example.firebaseauth.activity.GeofenceHelper
import com.example.firebaseauth.activity.GoogleMapView
import com.example.firebaseauth.model.GeofenceData

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

@Composable
fun GeofencePage(
    geofenceHelper: GeofenceHelper,
    onGeofenceAdded: (Boolean) -> Unit
) {
    var lat by remember { mutableStateOf("") }
    var lon by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("") }
    var isGeofenceAdded by remember { mutableStateOf(false) }

    // Context to use for Toast message
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = lat,
            onValueChange = { lat = it },
            label = { Text("Latitude") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = lon,
            onValueChange = { lon = it },
            label = { Text("Longitude") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = radius,
            onValueChange = { radius = it },
            label = { Text("Radius (in meters)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                // Convert inputs to the appropriate types
                val latDouble = lat.toDoubleOrNull()
                val lonDouble = lon.toDoubleOrNull()
                val radiusFloat = radius.toFloatOrNull()

                if (latDouble != null && lonDouble != null && radiusFloat != null) {
                    // Add geofence if the values are valid
                    geofenceHelper.addGeofence(latDouble, lonDouble, radiusFloat)
                    isGeofenceAdded = true
                    onGeofenceAdded(isGeofenceAdded)
                    // Optionally, show a success toast
                    Toast.makeText(context, "Geofence added", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle invalid input
                    Toast.makeText(context, "Please enter valid values", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Add Geofence")
        }

        // Map to display the geofence
        if (isGeofenceAdded) {
            GoogleMapView(lat = lat.toDouble(), lon = lon.toDouble(), radius = radius.toFloat())
        }
    }
}
