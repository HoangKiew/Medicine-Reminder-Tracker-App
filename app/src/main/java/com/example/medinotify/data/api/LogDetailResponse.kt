package com.example.medinotify.data.api

data class LogDetailResponse(
    val logId: String,
    val medicineId: String,
    val name: String,
    val dosage: String?,
    val medicineType: String?,
    val scheduledTime: String // "2025-11-18 17:30:00"
)