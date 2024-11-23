package com.example.firebaseauth.repository

import android.util.Log
import com.example.firebaseauth.model.DTR
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DTRRepository(private val firestore: FirebaseFirestore) {

    private val dtrCollection = firestore.collection("dtr") // Collection where DTR is stored

    // Function to save DTR data to Firestore
    suspend fun saveDTR(dtr: DTR) {
        try {
            // Use only the date for the document reference
            val documentRef = dtrCollection.document(dtr.date) // document is now identified only by date
            documentRef.set(dtr).await()
        } catch (e: Exception) {
            Log.e("DTRRepository", "Error saving DTR: ${e.message}")
        }
    }
}

