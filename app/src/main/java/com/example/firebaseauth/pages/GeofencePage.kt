package com.example.firebaseauth.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.firebaseauth.model.EmployeeLocation
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.math.*
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


@Composable
fun GeofencePage(navController: NavController) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    // Geofence data
    val geofenceLat = 9.9643
    val geofenceLon = 124.0264
    val geofenceRadius = 200.0 // Radius in meters
    val geofenceCenter = LatLng(geofenceLat, geofenceLon)

    // Camera position
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(geofenceCenter, 15f)
    }

    // State to hold employee locations
    var employeeLocations by remember { mutableStateOf<List<EmployeeLocation>>(emptyList()) }

    LaunchedEffect(Unit) {
        checkAndRequestNotificationPermission(context)
    }

    LaunchedEffect(Unit) {
        createNotificationChannel(context)
    }

    // Fetch employee locations from Firestore
    LaunchedEffect(Unit) {
        firestore.collection("employee_locations")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Error fetching employee data: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    employeeLocations = snapshot.documents.map { document ->
                        EmployeeLocation(
                            employeeId = document.getString("employeeId") ?: "Unknown",
                            latitude = document.getDouble("latitude") ?: 0.0,
                            longitude = document.getDouble("longitude") ?: 0.0
                        )
                    }
                }
            }
    }

    // Check if employees are inside the geofence and send notifications
    LaunchedEffect(employeeLocations) {
        employeeLocations.forEach { location ->
            if (isInsideGeofence(geofenceCenter, geofenceRadius, LatLng(location.latitude, location.longitude))) {
                sendNotification(
                    context,
                    "Employee ${location.employeeId} has entered the geofenced area!",
                    "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
                )
            }
        }

    }

    // Display map with geofence and employee markers
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            // Geofence Marker
            Marker(
                state = MarkerState(position = geofenceCenter),
                title = "Geofence Center"
            )

            // Geofence Circle
            Circle(
                center = geofenceCenter,
                radius = geofenceRadius,
                strokeColor = Color.Blue,
                strokeWidth = 2f,
                fillColor = Color(0x220000FF) // Light blue
            )

            // Employee Markers
            employeeLocations.forEach { location ->
                val employeePosition = LatLng(location.latitude, location.longitude)
                Marker(
                    state = MarkerState(position = employeePosition),
                    title = "Employee: ${location.employeeId}",
                    snippet = "Inside Geofence"
                )
            }
        }
    }
}

// Helper to check if a point is inside the geofence
fun isInsideGeofence(center: LatLng, radius: Double, point: LatLng): Boolean {
    val earthRadius = 6371000.0 // in meters

    val dLat = Math.toRadians(point.latitude - center.latitude)
    val dLon = Math.toRadians(point.longitude - center.longitude)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(center.latitude)) * cos(Math.toRadians(point.latitude)) *
            sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val distance = earthRadius * c

    return distance <= radius
}


fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Geofence Notification"
        val descriptionText = "Notifies when an employee enters the geofence"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("GEOFENCE_CHANNEL", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
fun checkAndRequestNotificationPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1 // Request code
            )
        }
    }
}


fun sendNotification(context: Context, title: String, message: String) {
    val channelId = "geofence_alerts"
    val notificationId = (0..1000).random()

    // Create the notification channel (required for Android 8+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Geofence Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for geofence entry alerts."
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // Build the notification
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_notification_overlay) // Replace with your app icon
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    // Show the notification
    with(NotificationManagerCompat.from(context)) {
        notify(notificationId, builder.build())
    }
}