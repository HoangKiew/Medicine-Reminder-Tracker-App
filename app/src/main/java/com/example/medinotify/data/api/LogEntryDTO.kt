package com.example.medinotify.data.api

data class LogEntryDTO(
    val logId: String,
    val medicineId: String,
    val name: String,
    val medicineType: String?,
    val dosage: String?,
    val time: String,         // "08:00"
    val status: String,       // Pending / Taken / Missed
    val actualTime: String?   // nullable
)

data class LogEntryResponse(
    val schedule: List<LogEntryDTO>
)
