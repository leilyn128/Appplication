package com.example.firebaseauth.activity
import android.net.Uri
import android.util.Log
import com.example.firebaseauth.model.DTRRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class ClockInManager {

    private val firestore = FirebaseFirestore.getInstance()

    fun timeIn(photoUri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("ClockInManager", "User is not logged in!")
            return
        }

        val date =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) // Get current date
        val timeIn =
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()) // Get current time

        // Create a new DTR record for morning arrival
        val dtr = DTRRecord(
            date = date,
            morningArrival = timeIn, // Set morning arrival time
            morningDeparture = null,
            afternoonArrival = null,
            afternoonDeparture = null,
            timestamp = null // Firestore will handle this
        )

        Log.d("ClockInManager", "Attempting to save DTR: $dtr")

        // Save the DTR record with Firestore's server timestamp
        saveDTR(dtr, userId)
    }

    private fun saveDTR(dtr: DTRRecord, userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val documentRef = firestore
                    .collection("dtr") // Collection for DTR
                    .document(userId) // Use the userId as the document ID
                    .collection("records") // Sub-collection for records
                    .document(dtr.date) // Each document will be identified by the date

                val dtrMap = hashMapOf<String, Any?>(
                    "date" to dtr.date,
                    "morningArrival" to dtr.morningArrival,
                    "morningDeparture" to dtr.morningDeparture,
                    "afternoonArrival" to dtr.afternoonArrival,
                    "afternoonDeparture" to dtr.afternoonDeparture,
                    "timestamp" to FieldValue.serverTimestamp() // Automatically set server timestamp
                )

                // Save the DTR record
                documentRef.set(dtrMap).await()
                Log.d("ClockInManager", "DTR saved successfully for date: ${dtr.date}")
            } catch (e: Exception) {
                Log.e("ClockInManager", "Error saving DTR: ${e.message}")
            }
        }
    }
}