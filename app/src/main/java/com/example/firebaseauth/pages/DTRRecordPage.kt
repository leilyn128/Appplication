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
fun DTRRecordPage( modifier: Modifier = Modifier) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "DTR Records for Admin", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))

        // Display a list or grid of employee time records
        Button(onClick = {
            // Handle DTR record management or viewing records
        }) {
            Text(text = "View/Manage DTR Records")
        }
    }
}
