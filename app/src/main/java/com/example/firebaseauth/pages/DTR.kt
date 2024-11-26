package com.example.firebaseauth.pages

import DTRViewModel
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

@Composable
fun DTR(
    modifier: Modifier = Modifier,
    dtrViewModel: DTRViewModel,
    onNavigateToCamera: () -> Unit,
    getCurrentMonth: () -> String
) {
    val dtrHistory by dtrViewModel.dtrHistory.collectAsState(emptyList())

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Custom Header Section (without TopAppBar)
        DTRCustomHeader(onNavigateToCamera)

        // Main Content Section
        DTRContent(dtrHistory, getCurrentMonth)
    }
}

@Composable
fun DTRCustomHeader(onNavigateToCamera: () -> Unit) {
    // Custom Header Layout with Logo on Left
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF5F8C60)) // Green color for background
            .padding(12.dp),
        horizontalArrangement = Arrangement.Start, // Align items to the start
        verticalAlignment = Alignment.CenterVertically // Align items vertically in the center
    ) {
        // Logo Image (Insert your logo here)
        Image(
            painter = painterResource(id = R.drawable.logo), // Replace with your logo resource
            contentDescription = "App Logo",
            modifier = Modifier
                .size(70.dp) // Adjust size of logo
                .padding(end = 16.dp) // Add padding between logo and text
        )

        // Title Text (Centered relative to the remaining space)
        Text(
            text = "Daily Time Record",
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f) // This will make the text take up remaining space
        )

        // Camera Icon Button
        IconButton(onClick = onNavigateToCamera) {
            Icon(imageVector = Icons.Default.Camera, contentDescription = "Open Camera")
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
            text = "A.M. Arrival",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "P.M. Departure",
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (dayOfMonth % 2 == 0) Color.White else Color.LightGray) // Alternate row color
            .padding(vertical = 8.dp), // Adjust vertical padding to give space for times
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Display the day of the month (1, 2, 3, etc.)
        Text(
            text = dayOfMonth.toString(),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        // Display A.M. Arrival and Departure
        Column(
            modifier = Modifier.weight(2f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "A.M. Arrival",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = record.amArrival.ifEmpty { "____" },
                textAlign = TextAlign.Center
            )
        }

        // Display P.M. Arrival and Departure
        Column(
            modifier = Modifier.weight(2f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "P.M. Departure",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = record.pmDeparture.ifEmpty { "____" },
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper function to get the current day of the month
fun getCurrentDayOfMonth(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.DAY_OF_MONTH) // Returns the current day of the month (1, 2, 3, etc.)
}
