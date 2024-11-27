package com.example.firebaseauth.pages

import DTRViewModel
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebaseauth.R
import com.example.firebaseauth.model.DTRRecord
import java.util.Calendar
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.firebaseauth.activity.GeofenceUtils
import com.example.firebaseauth.model.GeofenceData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng

@Composable
fun DTR(
    modifier: Modifier = Modifier,
    dtrViewModel: DTRViewModel,
    onNavigateToCamera: () -> Unit,
    getCurrentMonth: () -> String,
    fusedLocationClient: FusedLocationProviderClient,
    context: Context,
    navController: NavController
) {
    val geofenceData by dtrViewModel.geofenceData.observeAsState(GeofenceData(LatLng(0.0, 0.0), 0.0))
    val geofenceCenter = geofenceData.center
    val geofenceLatitude = geofenceCenter.latitude
    val geofenceLongitude = geofenceCenter.longitude
    val geofenceRadius = geofenceData.radius

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Pass the geofence data to the header
        DTRCustomHeader(
            fusedLocationClient = fusedLocationClient,
            geofenceLatitude = geofenceLatitude,
            geofenceLongitude = geofenceLongitude,
            geofenceRadius = geofenceRadius,
            onNavigateToCamera = onNavigateToCamera,
            context = context,
            navController = navController
        )

        // Main Content Section
        DTRContent(dtrViewModel.dtrHistory.collectAsState(emptyList()).value, getCurrentMonth)


    }
}




@Composable
fun DTRCustomHeader(
    fusedLocationClient: FusedLocationProviderClient,
    geofenceLatitude: Double,
    geofenceLongitude: Double,
    geofenceRadius: Double,
    onNavigateToCamera: () -> Unit,
    context: Context,
    navController: NavController
) {
    val customGreen = Color(0xFF5F8C60) // Define the custom green color

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(customGreen) // Use the custom green color
            .padding(vertical = 12.dp)
    ) {
        // Logo positioned at the top-left
        Image(
            painter = painterResource(id = R.drawable.logo), // Replace with your logo resource
            contentDescription = "Logo",
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.TopStart)
                .padding(start = 16.dp)
        )

        // Title centered horizontally
        Text(
            text = "Daily Time Record",
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )

        // Button with camera icon placed on the right side
        IconButton(
            onClick = {
                // Validate geofence access and navigate to camera page
                GeofenceUtils.validateGeofenceAccess(
                    fusedLocationClient = fusedLocationClient,
                    geofenceLatitude = geofenceLatitude,
                    geofenceLongitude = geofenceLongitude,
                    geofenceRadius = geofenceRadius,
                    context = context,
                    onSuccess = {
                        navController.navigate("cameraPage")
                    },
                    onFailure = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = "Camera Icon",
                tint = Color.White
            )
        }
    }
}

@Composable
fun DTRContent(dtrHistory: List<DTRRecord>, getCurrentMonth: () -> String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        // Display current month dynamically
        val currentMonth = remember { getCurrentMonth() }
        Text(
            text = "For the Month of: $currentMonth",
            fontSize = 18.sp,
            modifier = Modifier.padding(7.dp)
        )


        // Table Section
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
        ) {
            // Table Header
            item {
                DTRTableHeader()
            }

            // Table Rows
            items(dtrHistory) { record ->
                DTRTableRow(record)
            }
        }
    }
}

@Composable
fun DTRTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "Day",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "A.M.",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "P.M.",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DTRTableRow(record: DTRRecord) {
    // Get the current day of the month
    val dayOfMonth = remember { getCurrentDayOfMonth() }

    // Ensuring visibility with a bright color
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Yellow) // Use bright color for visibility
            .border(2.dp, Color.Black) // Border for visual confirmation
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Display the day of the month (1, 2, 3, etc.)
        Text(
            text = dayOfMonth.toString(),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = Color.Black // Black text for visibility
        )

        // AM Section
        Column(
            modifier = Modifier.weight(2f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AM",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Text(
                text = record.amArrival?.takeIf { it.isNotEmpty() } ?: "____",
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Text(
                text = record.amDeparture?.takeIf { it.isNotEmpty() } ?: "____",
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }

        // PM Section
        Column(
            modifier = Modifier.weight(2f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PM",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Text(
                text = record.pmArrival?.takeIf { it.isNotEmpty() } ?: "____",
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Text(
                text = record.pmDeparture?.takeIf { it.isNotEmpty() } ?: "____",
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }
    }
}


@Composable
fun DTRTable() {
    // Hardcoded dummy data for testing
    val record = DTRRecord(
        day = 1,
        amArrival = "8:00 AM",
        amDeparture = "12:00 PM",
        pmArrival = "1:00 PM",
        pmDeparture = "5:00 PM"
    )

    // Add a simple Column layout
    Column(modifier = Modifier.padding(16.dp)) {
        // Add debugging Text to check the hierarchy
        Text(text = "DTR Table", color = Color.Black)

        // Add the DTRTableRow to make sure it's being displayed
        DTRTableRow(record = record)

        // More debugging Text to verify rendering
        Text(text = "End of Row", color = Color.Black)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DTRTable()
}



// Helper function to get the current day of the month
fun getCurrentDayOfMonth(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.DAY_OF_MONTH) // Returns the current day of the month (1, 2, 3, etc.)
}
