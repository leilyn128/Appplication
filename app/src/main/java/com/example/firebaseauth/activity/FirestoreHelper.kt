package com.example.firebaseauth.helper

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreHelper {

    private val db = FirebaseFirestore.getInstance()

    // Fetch user role from Firestore based on user UID
    fun getUserRole(onRoleFetched: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role") ?: "employee"
                        onRoleFetched(role)
                    } else {
                        onRoleFetched("employee")
                    }
                }
                .addOnFailureListener {
                    onRoleFetched("employee") // Default to employee if error occurs
                }
        } else {
            onRoleFetched("employee") // Default to employee if no user is logged in
        }
    }
}
///