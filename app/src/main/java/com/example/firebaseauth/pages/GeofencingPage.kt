package com.example.googlemappage

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.example.firebaseauth.activity.LocationHelper

@Composable
fun GeofencingPage(
    modifier: Modifier = Modifier,
    isAdmin: Boolean,  // Pass a boolean to check if the user is an admin
    geofences: List<LatLng> = listOf()  // A list of geofenced locations for admin view
) {
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? ComponentActivity

    val locationHelper = remember {
        LocationHelper(context) { location ->
            currentLocation = location
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
        if (isGranted) {
            locationHelper.startLocationUpdates()
            Toast.makeText(context, "Location permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            locationPermissionGranted = true
            locationHelper.startLocationUpdates()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            locationHelper.stopLocationUpdates()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (isAdmin) {
            if (geofences.isNotEmpty()) {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(geofences.first(), 10f)
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    geofences.forEach { geofence ->
                        Marker(
                            state = MarkerState(position = geofence),
                            title = "Geofence"
                        )
                    }
                }
            } else {
                Text(
                    text = "No geofences available.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            }
        } else {
            if (locationPermissionGranted) {
                if (currentLocation != null) {
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(currentLocation!!, 15f)
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(
                            state = MarkerState(position = currentLocation!!),
                            title = "Current Location"
                        )
                    }
                } else {
                    Text(
                        text = "Fetching location...",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                }
            } else {
                Text(
                    text = "Location permission is required to display your current location.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Red
                )
            }
        }
    }
}///.fdvdft
