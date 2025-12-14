package com.example.medinotify.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log_entries")
data class LogEntryEntity(
    @PrimaryKey
    val logId: String,

    val userId: String,

    val medicineId: String,

    val medicineName: String,

    val intakeTime: Long,
    val status: String
)