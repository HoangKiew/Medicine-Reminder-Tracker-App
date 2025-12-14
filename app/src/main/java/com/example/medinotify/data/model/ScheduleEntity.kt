package com.example.medinotify.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["medicineId"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicineId"])]
)
data class ScheduleEntity(
    @PrimaryKey
    val scheduleId: String,

    val userId: String,

    val medicineId: String,

    // ✅ FIX: Đổi tên cột để khớp với Domain Model (đã sửa cho Firebase)
    val specificTimeStr: String, // Định dạng "HH:mm"

    val nextScheduledTimestamp: Long,
    val reminderStatus: Boolean
)