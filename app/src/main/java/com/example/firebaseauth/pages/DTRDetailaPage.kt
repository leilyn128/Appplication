package com.example.firebaseauth.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun DTRDetailPage(employeeId: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "DTR Record for Employee: $employeeId", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))

        // Display DTR details here (e.g., date, hours worked, etc.)
        // Provide an option to view or download the DTR file
        Button(onClick = {
            // Handle view or download DTR file
        }) {
            Text(text = "Open DTR File")
        }
    }
}
