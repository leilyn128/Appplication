package com.example.firebaseauth.pages

import DTRViewModel
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.firebaseauth.R
import com.example.firebaseauth.model.DTRRecord
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import android.location.Location
import androidx.compose.runtime.livedata.observeAsState
import com.example.firebaseauth.activity.GeofenceUtils
import com.example.firebaseauth.activity.isLocationPermissionGranted
import com.example.firebaseauth.model.GeofenceData
import com.google.android.gms.location.FusedLocationProviderClient


@Composable
fun DTR(
    viewModel: DTRViewModel,  // Directly use the viewModel passed as parameter
    employeeId: String,
    fusedLocationClient: FusedLocationProviderClient,
) {
    // Use the passed viewModel directly, no need to call viewModel() again
    val geofenceData by viewModel.geofenceData.observeAsState(GeofenceData(LatLng(0.0, 0.0), 0.0))

    val geofenceCenter = geofenceData.center
    val geofenceLatitude = geofenceCenter.latitude
    val geofenceLongitude = geofenceCenter.longitude
    val geofenceRadius = geofenceData.radius

    val context = LocalContext.current
    // You don't need to reinitialize fusedLocationClient here since it's already passed
    // val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val locationPermissionGranted = isLocationPermissionGranted(context)

    val isInGeofence = remember { mutableStateOf(false) }
    val message = remember { mutableStateOf("") }

    val dtrRecords = viewModel.dtrRecords.collectAsState().value

    LaunchedEffect(employeeId) {
        viewModel.fetchDTRRecords(employeeId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Keep the header fixed at the top
        DTRCustomHeader()

        Spacer(modifier = Modifier.height(8.dp)) // Add spacing below the header

        // Main content centered
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center // Center the content within the remaining space
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (dtrRecords.isEmpty()) {
                    Text("No records found.", style = MaterialTheme.typography.bodyLarge)
                } else {
                    // Show the first DTR record only for now
                    DTRCard(record = dtrRecords.first())
                }

                Spacer(modifier = Modifier.height(24.dp)) // Add spacing between the card and buttons

                // Center the clock-in and clock-out buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClockInButton(employeeId, geofenceLatitude, geofenceLongitude, geofenceRadius, fusedLocationClient)
                    ClockOutButton(employeeId, geofenceLatitude, geofenceLongitude, geofenceRadius, fusedLocationClient)
                }
            }
        }
    }
}


@Composable
fun DTRCustomHeader() {
    val customGreen = Color(0xFF5F8C60) // Define the custom green color

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(customGreen) // Use the custom green color
            .padding(vertical = 6.dp)
    ) {
        // Logo positioned at the top-left
        Image(
            painter = painterResource(id = R.drawable.logo), // Replace with your logo resource
            contentDescription = "Logo",
            modifier = Modifier
                .size(75.dp)
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        )

        // Title centered horizontally
        Text(
            text = "Daily Time Record",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun DTRCard(record: DTRRecord) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val arrivalTime = record.morningArrival?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "N/A"
    val morningDepartureTime = record.morningDeparture?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "N/A"
    val afternoonArrivalTime = record.afternoonArrival?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "N/A"
    val afternoonDepartureTime = record.afternoonDeparture?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "N/A"

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f) // Make the card width 90% of the screen
            .height(350.dp), // Set a fixed height
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly // Distribute items evenly
        ) {
            Text(
                text = "Date: ${dateFormat.format(record.date)}",
                style = TextStyle(fontSize = 18.sp, color = Color.White) // Custom font size and color
            )
            Text(
                text = "Morning Arrival: $arrivalTime",
                style = TextStyle(fontSize = 18.sp, color = Color.White) // Custom font size and color
            )
            Text(
                text = "Morning Departure: $morningDepartureTime",
                style = TextStyle(fontSize = 18.sp, color = Color.White) // Custom font size and color
            )
            Text(
                text = "Afternoon Arrival: $afternoonArrivalTime",
                style = TextStyle(fontSize = 18.sp, color = Color.White) // Custom font size and color
            )
            Text(
                text = "Afternoon Departure: $afternoonDepartureTime",
                style = TextStyle(fontSize = 18.sp, color = Color.White) // Custom font size and color
            )
        }
    }
}

@Composable
fun ClockInButton(
    employeeId: String,
    geofenceLatitude: Double,
    geofenceLongitude: Double,
    geofenceRadius: Double,
    fusedLocationClient: FusedLocationProviderClient
) {
    val context = LocalContext.current

    Button(
        onClick = {
            // Validate geofence access first
            GeofenceUtils.validateGeofenceAccess(
                fusedLocationClient,
                geofenceLatitude,
                geofenceLongitude,
                geofenceRadius,
                context,
                onSuccess = {
                    // Proceed with clocking in
                    val currentTime = Calendar.getInstance().time
                    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

                    val db = FirebaseFirestore.getInstance()
                    val recordId = "${employeeId}-${getCurrentDate()}"

                    db.collection("dtr_records")
                        .document(recordId)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val morningArrival = documentSnapshot.getDate("morningArrival")
                                val afternoonArrival = documentSnapshot.getDate("afternoonArrival")

                                if (currentHour < 12) {
                                    if (morningArrival != null) {
                                        Toast.makeText(context, "You have already clocked in for the morning.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        handleMorningArrival(employeeId, currentTime)
                                        Toast.makeText(context, "Morning clock-in successful!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    if (afternoonArrival != null) {
                                        Toast.makeText(context, "You have already clocked in for the afternoon.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        handleAfternoonArrival(employeeId, currentTime)
                                        Toast.makeText(context, "Afternoon clock-in successful!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                // No record exists, allow the first clock-in
                                if (currentHour < 12) {
                                    handleMorningArrival(employeeId, currentTime)
                                    Toast.makeText(context, "Morning clock-in successful!", Toast.LENGTH_SHORT).show()
                                } else {
                                    handleAfternoonArrival(employeeId, currentTime)
                                    Toast.makeText(context, "Afternoon clock-in successful!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(context, "Error checking DTR record: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                onFailure = { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
        },
        modifier = Modifier
            .height(50.dp)
            .width(150.dp) // Button size adjustment
    ) {
        Text(text = "Clock In")
    }
}

@Composable
fun ClockOutButton(
    employeeId: String,
    geofenceLatitude: Double,
    geofenceLongitude: Double,
    geofenceRadius: Double,
    fusedLocationClient: FusedLocationProviderClient
) {
    val context = LocalContext.current

    Button(
        onClick = {
            // Validate geofence access first
            GeofenceUtils.validateGeofenceAccess(
                fusedLocationClient,
                geofenceLatitude,
                geofenceLongitude,
                geofenceRadius,
                context,
                onSuccess = {
                    // Proceed with clocking out
                    val currentTime = Calendar.getInstance().time
                    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

                    val db = FirebaseFirestore.getInstance()
                    val recordId = "${employeeId}-${getCurrentDate()}"

                    db.collection("dtr_records")
                        .document(recordId)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val morningArrival = documentSnapshot.getDate("morningArrival")
                                val afternoonArrival = documentSnapshot.getDate("afternoonArrival")
                                val morningDeparture = documentSnapshot.getDate("morningDeparture")
                                val afternoonDeparture = documentSnapshot.getDate("afternoonDeparture")

                                if (currentHour < 12) {
                                    // Morning clock-out logic
                                    if (morningArrival == null) {
                                        Toast.makeText(context, "You must clock in for the morning first.", Toast.LENGTH_SHORT).show()
                                    } else if (morningDeparture != null) {
                                        Toast.makeText(context, "You have already clocked out for the morning.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        handleMorningDeparture(employeeId, currentTime)
                                        Toast.makeText(context, "Morning clock-out successful!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    // Afternoon clock-out logic
                                    if (afternoonArrival == null) {
                                        Toast.makeText(context, "You must clock in for the afternoon first.", Toast.LENGTH_SHORT).show()
                                    } else if (afternoonDeparture != null) {
                                        Toast.makeText(context, "You have already clocked out for the afternoon.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        handleAfternoonDeparture(employeeId, currentTime)
                                        Toast.makeText(context, "Afternoon clock-out successful!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "No clock-in record found for today. Please clock in first.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(context, "Error checking DTR record: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                onFailure = { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
        },
        modifier = Modifier
            .height(50.dp)
            .width(150.dp) // Button size adjustment
    ) {
        Text(text = "Clock Out")
    }
}

private fun handleMorningArrival(employeeId: String, time: Date) {
    val db = FirebaseFirestore.getInstance()

    db.collection("dtr_records")
        .document("${employeeId}-${getCurrentDate()}")
        .get()
        .addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                // Create a new record if it doesn't exist
                val dtrRecord = DTRRecord(
                    employeeId = employeeId,
                    date = time,
                    morningArrival = time, // Set morning arrival time
                    morningDeparture = null,
                    afternoonArrival = null,
                    afternoonDeparture = null
                )

                db.collection("dtr_records")
                    .document("${employeeId}-${getCurrentDate()}")
                    .set(dtrRecord)
                    .addOnSuccessListener {
                        Log.d("DTR", "Morning arrival saved successfully for $employeeId on ${getCurrentDate()}")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("DTR", "Error saving morning arrival for $employeeId: ${exception.message}")
                    }
            } else {
                // If record exists, log it
                Log.d("DTR", "DTR record already exists for $employeeId on ${getCurrentDate()}")
            }
        }
        .addOnFailureListener { exception ->
            Log.e("DTR", "Error fetching DTR record for $employeeId: ${exception.message}")
        }
}
private fun handleMorningDeparture(employeeId: String, time: Date) {
    val db = FirebaseFirestore.getInstance()

    db.collection("dtr_records")
        .document("${employeeId}-${getCurrentDate()}")
        .get()
        .addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // Update the morningDeparture field
                db.collection("dtr_records")
                    .document("${employeeId}-${getCurrentDate()}")
                    .update("morningDeparture", time)
                    .addOnSuccessListener {
                        Log.d("DTR", "Morning departure updated successfully for $employeeId on ${getCurrentDate()}")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("DTR", "Error updating morning departure: ${exception.message}")
                    }
            } else {
                Log.e("DTR", "No DTR record found for $employeeId to update morning departure.")
            }
        }
        .addOnFailureListener { exception ->
            Log.e("DTR", "Error fetching DTR record: ${exception.message}")
        }
}



private fun handleAfternoonArrival(employeeId: String, time: Date) {
    val db = FirebaseFirestore.getInstance()

    db.collection("dtr_records")
        .document("${employeeId}-${getCurrentDate()}")
        .get()
        .addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                // Create a new record if it doesn't exist
                val dtrRecord = DTRRecord(
                    employeeId = employeeId,
                    date = time,
                    morningArrival = null, // Morning arrival remains null
                    morningDeparture = null,
                    afternoonArrival = time, // Set afternoon arrival time
                    afternoonDeparture = null
                )

                db.collection("dtr_records")
                    .document("${employeeId}-${getCurrentDate()}")
                    .set(dtrRecord)
                    .addOnSuccessListener {
                        Log.d("DTR", "Afternoon arrival saved successfully for $employeeId on ${getCurrentDate()}")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("DTR", "Error saving afternoon arrival for $employeeId: ${exception.message}")
                    }
            } else {
                // If record exists, update the afternoon arrival
                db.collection("dtr_records")
                    .document("${employeeId}-${getCurrentDate()}")
                    .update("afternoonArrival", time)
                    .addOnSuccessListener {
                        Log.d("DTR", "Afternoon arrival updated for $employeeId on ${getCurrentDate()}")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("DTR", "Error updating afternoon arrival for $employeeId: ${exception.message}")
                    }
            }
        }
        .addOnFailureListener { exception ->
            Log.e("DTR", "Error fetching DTR record for $employeeId: ${exception.message}")
        }
}

private fun handleAfternoonDeparture(employeeId: String, time: Date) {
    val db = FirebaseFirestore.getInstance()

    db.collection("dtr_records")
        .document("${employeeId}-${getCurrentDate()}")
        .get()
        .addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // Update the afternoonDeparture field
                db.collection("dtr_records")
                    .document("${employeeId}-${getCurrentDate()}")
                    .update("afternoonDeparture", time)
                    .addOnSuccessListener {
                        Log.d("DTR", "Afternoon departure updated successfully for $employeeId on ${getCurrentDate()}")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("DTR", "Error updating afternoon departure: ${exception.message}")
                    }
            } else {
                Log.e("DTR", "No DTR record found for $employeeId to update afternoon departure.")
            }
        }
        .addOnFailureListener { exception ->
            Log.e("DTR", "Error fetching DTR record: ${exception.message}")
        }
}



private fun getCurrentDate(): String {
    val calendar = Calendar.getInstance()
    return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
}
