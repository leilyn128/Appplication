package com.example.firebaseauth.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.firebaseauth.R // Assuming logo is in the res/drawable folder

@Composable
fun DTRRecordPage(modifier: Modifier = Modifier) {
    val emailList = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        fetchAllEmails(emailList)
    }

    Column(modifier = modifier.padding(16.dp)) {
        // Title with the green background color
        Row(
            modifier = Modifier
                .background(Color(0xFF5F8C60)) // Green background color
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo image
            Icon(
                painter = painterResource(id = R.drawable.logo), // Replace with your logo resource ID
                contentDescription = "Logo",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Title text
            Text(
                text = "Employees Account",
                style = MaterialTheme.typography.h6.copy(color = Color.White)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Check if emailList is empty and display appropriate message
        if (emailList.isEmpty()) {
            Text(text = "No employee records found.", style = MaterialTheme.typography.body1)
        } else {
            // Display the list of emails using LazyColumn
            LazyColumn(modifier = Modifier.fillMaxHeight(0.8f)) {
                items(emailList.size) { index ->
                    val email = emailList[index]
                    EmailCard(email = email)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}


@Composable
fun EmailCard(email: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color.LightGray
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Employee Email:", style = MaterialTheme.typography.body2)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = email, style = MaterialTheme.typography.body1)
        }
    }
}

suspend fun fetchAllEmails(emailList: MutableList<String>) {
    val db = FirebaseFirestore.getInstance()

    try {
        // Fetch documents from the "dtr_records" collection
        val snapshot = db.collection("dtr_records").get().await()

        // Iterate over each document and extract the email
        snapshot.documents.forEach { doc ->
            val email = doc.getString("email") // Assuming you store email under the "email" field
            email?.let {
                emailList.add(it) // Add email to the list if it's not null
            }
        }
    } catch (e: Exception) {
        Log.e("DTRRecordPage", "Error fetching emails: ${e.message}")
    }
}
