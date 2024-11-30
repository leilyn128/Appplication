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
import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.font.FontWeight
import com.example.firebaseauth.activity.GeofenceUtility
import com.example.firebaseauth.model.GeofenceData
import com.example.firebaseauth.activity.GeofenceUtils
import com.google.android.gms.location.FusedLocationProviderClient




@Composable
fun DTR(
    viewModel: DTRViewModel,
    email: String,
    fusedLocationClient: FusedLocationProviderClient,
) {

    val geofenceData by viewModel.geofenceData.observeAsState(GeofenceData(LatLng(0.0, 0.0), 0.0))
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    val geofenceCenter = geofenceData.center
    val geofenceLatitude = geofenceCenter.latitude
    val geofenceLongitude = geofenceCenter.longitude
    val geofenceRadius = geofenceData.radius

    val context = LocalContext.current


    val dtrRecords = viewModel.dtrRecords.collectAsState().value
    var showRecordsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(email) {
        viewModel.fetchDTRRecords(email) // Fetch records using email
    }
    LaunchedEffect(currentLocation) {
        currentLocation?.let { latLng ->
            val location = Location("").apply {
                latitude = latLng.latitude
                longitude = latLng.longitude
            }

        }
    }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Keep the header fixed at the top
        DTRCustomHeader(onViewRecordsClick = { showRecordsDialog = true })

        Spacer(modifier = Modifier.height(8.dp)) // Add spacing below the header

        // Main content centered
        // Inside your composable
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Get today's date
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // Find today's record in dtrRecords
                val todaysRecord = dtrRecords.find { record ->
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(record.date) == today
                }

                if (todaysRecord == null) {
                    Text("No records found for today.", style = MaterialTheme.typography.bodyLarge)
                } else {
                    // Show today's DTR record
                    DTRCard(record = todaysRecord)
                }

                Spacer(modifier = Modifier.height(24.dp)) // Add spacing between the card and buttons

                // Center the clock-in and clock-out buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClockInButton(email, fusedLocationClient)
                    ClockOutButton(email, fusedLocationClient)
                }
            }
        }
    }
        if (showRecordsDialog) {
        RecordsDialog(
            records = dtrRecords,
            onDismiss = { showRecordsDialog = false }
        )
    }

}
@Composable
fun DTRCustomHeader(onViewRecordsClick: () -> Unit) {
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

        // "View Records" IconButton at the top-right
        IconButton(
            onClick = onViewRecordsClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {

            Icon(
                imageVector = Icons.Default.List, // You can replace this with your own icon
                contentDescription = "View Records",
                tint = Color.White // Apply the custom green color to the icon
            )
        }
    }
}

@Composable
fun DTRCard(record: DTRRecord) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val arrivalTime = record.morningArrival?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "____"
    val morningDepartureTime = record.morningDeparture?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "____"
    val afternoonArrivalTime = record.afternoonArrival?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "____"
    val afternoonDepartureTime = record.afternoonDeparture?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "____"

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
    email: String,
    fusedLocationClient: FusedLocationProviderClient
) {
    val context = LocalContext.current

    // Button for clock-in
    Button(
        onClick = {
            // Fetch geofence data set by the admin
            GeofenceUtility.fetchGeofenceData(
                onSuccess = { latitude, longitude, radius ->
                    // Validate if the employee is within the geofence
                    GeofenceUtils.validateGeofenceAccess(
                        fusedLocationClient,
                        latitude,
                        longitude,
                        radius,
                        context,
                        onSuccess = {
                            // Proceed with clocking in
                            val currentTime = Calendar.getInstance().time
                            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

                            val db = FirebaseFirestore.getInstance()
                            val recordId = "${email}-${getCurrentDate()}" // Use email in recordId

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
                                                handleMorningArrival(email, currentTime)
                                                Toast.makeText(context, "Morning clock-in successful!", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            if (afternoonArrival != null) {
                                                Toast.makeText(context, "You have already clocked in for the afternoon.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                handleAfternoonArrival(email, currentTime)
                                                Toast.makeText(context, "Afternoon clock-in successful!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        // No existing record for today, create a new one based on the time
                                        if (currentHour < 12) {
                                            handleMorningArrival(email, currentTime)
                                            Toast.makeText(context, "Morning clock-in successful!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            handleAfternoonArrival(email, currentTime)
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
                onFailure = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
        },
        modifier = Modifier
            .height(50.dp)
            .width(150.dp)
    ) {
        Text(text = "Clock In")
    }
}
@Composable
fun ClockOutButton(
    email: String, // Use email instead of employeeId
    fusedLocationClient: FusedLocationProviderClient
) {
    val context = LocalContext.current
    val currentTime = Calendar.getInstance().time
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    Button(
        onClick = {
            // Fetch geofence data set by the admin
            GeofenceUtility.fetchGeofenceData(
                onSuccess = { latitude, longitude, radius ->
                    // Validate if the employee is within the geofence
                    GeofenceUtils.validateGeofenceAccess(
                        fusedLocationClient,
                        latitude,
                        longitude,
                        radius,
                        context,
                        onSuccess = {
                            // Proceed with clocking out
                            val db = FirebaseFirestore.getInstance()
                            val recordId = "${email}-${getCurrentDate()}" // Use email in recordId

                            // Get DTR record for today
                            db.collection("dtr_records")
                                .document(recordId)
                                .get()
                                .addOnSuccessListener { documentSnapshot ->
                                    if (documentSnapshot.exists()) {
                                        // Extract existing clock-in/out times
                                        val morningArrival = documentSnapshot.getDate("morningArrival")
                                        val afternoonArrival = documentSnapshot.getDate("afternoonArrival")
                                        val morningDeparture = documentSnapshot.getDate("morningDeparture")
                                        val afternoonDeparture = documentSnapshot.getDate("afternoonDeparture")

                                        // Handle clock-out logic based on time of day
                                        if (currentHour < 12) {
                                            // Morning shift
                                            when {
                                                morningArrival == null -> {
                                                    Toast.makeText(context, "You must clock in for the morning first.", Toast.LENGTH_SHORT).show()
                                                }
                                                morningDeparture != null -> {
                                                    Toast.makeText(context, "You have already clocked out for the morning.", Toast.LENGTH_SHORT).show()
                                                }
                                                else -> {
                                                    handleMorningDeparture(email, currentTime)
                                                    Toast.makeText(context, "Morning clock-out successful!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } else {
                                            // Afternoon shift
                                            when {
                                                afternoonArrival == null -> {
                                                    Toast.makeText(context, "You must clock in for the afternoon first.", Toast.LENGTH_SHORT).show()
                                                }
                                                afternoonDeparture != null -> {
                                                    Toast.makeText(context, "You have already clocked out for the afternoon.", Toast.LENGTH_SHORT).show()
                                                }
                                                else -> {
                                                    handleAfternoonDeparture(email, currentTime)
                                                    Toast.makeText(context, "Afternoon clock-out successful!", Toast.LENGTH_SHORT).show()
                                                }
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
                onFailure = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
        },
        modifier = Modifier
            .height(50.dp)
            .width(150.dp)
    ) {
        Text(text = "Clock Out")
    }
}


private fun handleMorningArrival(email: String, time: Date) {
    val db = FirebaseFirestore.getInstance()

    db.collection("dtr_records")
        .document("${email}-${getCurrentDate()}")
        .get()
        .addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                // Create a new record if it doesn't exist
                val dtrRecord = DTRRecord(
                    email = email,
                    date = time,
                    morningArrival = time, // Set morning arrival time
                    morningDeparture = null,
                    afternoonArrival = null,
                    afternoonDeparture = null
                )

                db.collection("dtr_records")
                    .document("${email}-${getCurrentDate()}")
                    .set(dtrRecord)
                    .addOnSuccessListener {
                        Log.d("DTR", "Morning arrival saved successfully for $email on ${getCurrentDate()}")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("DTR", "Error saving morning arrival for $email: ${exception.message}")
                    }
            } else {
                // If record exists, log it
                Log.d("DTR", "DTR record already exists for $email on ${getCurrentDate()}")
            }
        }
        .addOnFailureListener { exception ->
            Log.e("DTR", "Error fetching DTR record for $email: ${exception.message}")
        }
}

private fun handleMorningDeparture(email: String, time: Date) {
    val db = FirebaseFirestore.getInstance()

    db.collection("dtr_records")
        .document("${email}-${getCurrentDate()}")
        .get()
        .addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // Update the morningDeparture field
                db.collection("dtr_records")
                    .document("${email}-${getCurrentDate()}")
                    .update("morningDeparture", time)
                    .addOnSuccessListener {
                        Log.d("DTR", "Morning departure updated successfully for $email on ${getCurrentDate()}")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("DTR", "Error updating morning departure: ${exception.message}")
                    }
            } else {
                Log.e("DTR", "No DTR record found for $email to update morning departure.")
            }
        }
        .addOnFailureListener { exception ->
            Log.e("DTR", "Error fetching DTR record: ${exception.message}")
        }
}

private fun handleAfternoonArrival(email: String, time: Date) {
    val db = FirebaseFirestore.getInstance()

    db.collection("dtr_records")
        .document("${email}-${getCurrentDate()}")
        .get()
        .addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                // Create a new record if it doesn't exist
                val dtrRecord = DTRRecord(
                    email = email,
                    date = time,
                    morningArrival = null, // Morning arrival remains null
                    morningDeparture = null,
                    afternoonArrival = time, // Set afternoon arrival time
                    afternoonDeparture = null
                )

                db.collection("dtr_records")
                    .document("${email}-${getCurrentDate()}")
                    .set(dtrRecord)
                    .addOnSuccessListener {
                        Log.d("DTR", "Afternoon arrival saved successfully for $email on ${getCurrentDate()}")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("DTR", "Error saving afternoon arrival for $email: ${exception.message}")
                    }
            } else {
                // If record exists, update the afternoon arrival
                db.collection("dtr_records")
                    .document("${email}-${getCurrentDate()}")
                    .update("afternoonArrival", time)
                    .addOnSuccessListener {
                        Log.d("DTR", "Afternoon arrival updated for $email on ${getCurrentDate()}")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("DTR", "Error updating afternoon arrival for $email: ${exception.message}")
                    }
            }
        }
        .addOnFailureListener { exception ->
            Log.e("DTR", "Error fetching DTR record for $email: ${exception.message}")
        }
}

private fun handleAfternoonDeparture(email: String, time: Date) {
    val db = FirebaseFirestore.getInstance()

    db.collection("dtr_records")
        .document("${email}-${getCurrentDate()}")
        .get()
        .addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // Update the afternoonDeparture field
                db.collection("dtr_records")
                    .document("${email}-${getCurrentDate()}")
                    .update("afternoonDeparture", time)
                    .addOnSuccessListener {
                        Log.d("DTR", "Afternoon departure updated successfully for $email on ${getCurrentDate()}")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("DTR", "Error updating afternoon departure: ${exception.message}")
                    }
            } else {
                Log.e("DTR", "No DTR record found for $email to update afternoon departure.")
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




@Composable
fun RecordsDialog(records: List<DTRRecord>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "DTR Records",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (records.isEmpty()) {
                    Text("No records available.", style = MaterialTheme.typography.bodyLarge)
                } else {
                    records.forEach { record ->
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val arrivalTime = record.morningArrival?.let {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                        } ?: "____"
                        val morningDepartureTime = record.morningDeparture?.let {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                        } ?: "____"
                        val afternoonArrivalTime = record.afternoonArrival?.let {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                        } ?: "____"
                        val afternoonDepartureTime = record.afternoonDeparture?.let {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                        } ?: "____"

                        Column(modifier = Modifier.padding(bottom = 16.dp)) {
                            // Date Row
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Date: ${dateFormat.format(record.date)}", modifier = Modifier.weight(1f))
                            }

                            // AM Section
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("AM", modifier = Modifier.weight(1f), style = TextStyle(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.weight(1f)) // Push PM label to the right
                                Text("PM", modifier = Modifier.weight(1f), style = TextStyle(fontWeight = FontWeight.Bold))
                            }

                            // AM IN/OUT and PM IN/OUT Row
                            Row(modifier = Modifier.fillMaxWidth()) {
                                // AM Times (IN/OUT)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("IN: $arrivalTime")
                                    Text("OUT: $morningDepartureTime")
                                }

                                // Spacer to align PM section to the right
                                Spacer(modifier = Modifier.weight(1f))

                                // PM Times (IN/OUT)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("IN: $afternoonArrivalTime")
                                    Text("OUT: $afternoonDepartureTime")
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
