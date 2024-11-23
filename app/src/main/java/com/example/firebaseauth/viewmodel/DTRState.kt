package com.example.firebaseauth.viewmodel

// Updated DTRState
data class DTRState(
    val timeIn: String = "",
    val timeOut: String = "",
    val totalHoursWorked: Double = 0.0,
    val DTRRecords: List<DTRRecord> = emptyList()
)

data class DTRRecord(
    val day: Int,
    val amIn: String? = null,
    val amOut: String? = null,
    val pmIn: String? = null,
    val pmOut: String? = null
)
