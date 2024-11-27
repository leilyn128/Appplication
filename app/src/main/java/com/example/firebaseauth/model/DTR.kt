package com.example.firebaseauth.model

import java.util.Date

data class DTRRecord(
    val employeeId: String,
    val date: Date,
    val morningArrival: Date?,
    val morningDeparture: Date?,
    val afternoonArrival: Date?,
    val afternoonDeparture: Date?

)
