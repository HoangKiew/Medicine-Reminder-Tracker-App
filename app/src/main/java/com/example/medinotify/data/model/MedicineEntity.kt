package com.example.medinotify.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey
    val medicineId: String,

    val userId: String,

    val name: String,
    val dosage: String,
    val type: String,
    val quantity: Int,

    // ==========================================================
    // ✅ THÊM CÁC CỘT LỊCH TRÌNH MỚI CHO ROOM
    // ==========================================================

    // Tần suất uống (DAILY, SPECIFIC_DAYS, INTERVAL)
    // Lưu dưới dạng String (tên của Enum Frequency)
    val frequencyType: String,

    // Giá trị chi tiết cho tần suất:
    // - Nếu SPECIFIC_DAYS: Lưu DayOfWeek List dưới dạng JSON String (ví dụ: "[MONDAY, TUESDAY]")
    // - Nếu INTERVAL: Lưu số ngày lặp lại dưới dạng String (ví dụ: "2")
    val scheduleValue: String?,

    // Ngày bắt đầu (Lưu dưới dạng String hoặc Long Timestamp)
    // Dùng Long Timestamp để dễ dàng so sánh trong Room
    val startDateTimestamp: Long,

    // Ngày kết thúc (Long Timestamp, null nếu không giới hạn)
//    val endDateTimestamp: Long?,

    // ==========================================================

    val notes: String,
    val isActive: Boolean
)