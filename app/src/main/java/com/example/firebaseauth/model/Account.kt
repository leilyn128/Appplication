package com.example.firebaseauth.model


data class UserModel(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val contactNo: String = "",
    val employeeID: String = "",
    val address: String = "",
    val role: String = "employee" // Default role
)




