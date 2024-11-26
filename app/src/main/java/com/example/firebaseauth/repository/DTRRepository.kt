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
                    .document(userId)
                    .collection("records")
                    .document(dtr.date)

                // Set timestamp using Firestore's server timestamp
                val dtrMap = hashMapOf<String, Any?>(
                    "date" to dtr.date,
                    "morningArrival" to dtr.morningArrival,
                    "morningDeparture" to dtr.morningDeparture,
                    "afternoonArrival" to dtr.afternoonArrival,
                    "afternoonDeparture" to dtr.afternoonDeparture,
                    "timestamp" to FieldValue.serverTimestamp() // Firestore handles this
                )

                documentRef.set(dtrMap).await()
            } else {
                Log.e("DTRRepository", "User is not logged in.")
            }
        } catch (e: Exception) {
            Log.e("DTRRepository", "Error saving DTR: ${e.message}")
        }
    }
}
