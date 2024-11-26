package com.example.firebaseauth.repository

import android.util.Log
import com.example.firebaseauth.model.DTRRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DTRRepository(private val firestore: FirebaseFirestore) {

    private val dtrCollection = firestore.collection("Records") // Collection where DTR is stored

    suspend fun saveDTR(dtr: DTRRecord) {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val documentRef = dtrCollection
                    .document(userId) // Use the user ID to store DTR under each user
                    .collection("dtr_records") // Sub-collection for each user's DTR records
                    .document(dtr.day.toString()) // Use the `day` as document ID (convert to string)

                // Set timestamp using Firestore's server timestamp
                val dtrMap = hashMapOf<String, Any?>(
                    "day" to dtr.day,
                    "amArrival" to dtr.amArrival,
                    "amDeparture" to dtr.amDeparture,
                    "pmArrival" to dtr.pmArrival,
                    "pmDeparture" to dtr.pmDeparture,
                    "photoUrl" to dtr.photoUrl, // Store the photo URL if available
                    "timestamp" to FieldValue.serverTimestamp() // Firestore handles this automatically
                )

                documentRef.set(dtrMap).await() // Save the DTR data in Firestore
            } else {
                Log.e("DTRRepository", "User is not logged in.")
            }
        } catch (e: Exception) {
            Log.e("DTRRepository", "Error saving DTR: ${e.message}")
        }
    }
}
