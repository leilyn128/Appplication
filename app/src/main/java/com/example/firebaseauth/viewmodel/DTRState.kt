package com.example.firebaseauth.viewmodel

import com.example.firebaseauth.model.DTRRecord

// Updated DTRState
data class DTRState(
    val timeIn: String = "",
    val timeOut: String = "",
    val totalHoursWorked: Double = 0.0,
    val DTRRecords: List<DTRRecord> = emptyList()
)


