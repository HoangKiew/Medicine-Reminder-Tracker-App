package com.example.medinotify.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log_entries")
data class LogEntryEntity(
    @PrimaryKey
    val logId: String,

    // ✅ Cột bắt buộc để phân biệt dữ liệu giữa các người dùng
    val userId: String,

    val medicineId: String,

    // ✅ Cột này cần thiết để hiển thị tên nhanh chóng trong lịch sử
    val medicineName: String,

    val intakeTime: Long,
    val status: String
)