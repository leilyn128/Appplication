import java.security.Timestamp

data class ClockInRecord(
    val email: String,        // Employee's email
    val clockInTime: Timestamp,  // Timestamp of when the clock-in happens
    val photoUrl: String?     // URL of the uploaded photo
)
