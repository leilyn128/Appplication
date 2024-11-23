package com.example.firebaseauth.pages

import DTRViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebaseauth.model.DTRRecord

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
    // Custom Header Layout
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF5F8C60)) // Green color for background
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Title Text
        Text(
            text = "Daily Time Record",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        // Camera Icon Button
        IconButton(onClick = onNavigateToCamera) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Capture Photo",
                tint = Color.White // Set the icon color to white
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
            fontSize = 14.sp,
            modifier = Modifier.padding(8.dp)
        )

        // Display office hours
        Text(
            text = "Office Hour: Arrival A.M. 8:00 P.M. 1:00 \nDeparture A.M. 12:00 P.M. 5:00",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(8.dp)
        )

        // Table Section
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.LightGray)
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
            .background(Color.White)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "Date",
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (record.date.hashCode() % 2 == 0) Color.White else Color.LightGray)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = record.date,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier.weight(2f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = record.morningArrival ?: "____",
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = record.morningDeparture ?: "____",
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.weight(2f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = record.afternoonArrival ?: "____",
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = record.afternoonDeparture ?: "____",
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Data model for DTR record (you might already have it)
