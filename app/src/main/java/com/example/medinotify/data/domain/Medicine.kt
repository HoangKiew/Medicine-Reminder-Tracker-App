package com.example.medinotify.data.domain

import com.example.medinotify.data.model.Frequency
import java.time.DayOfWeek
import java.util.UUID

/**
 * Lớp domain đại diện cho một loại thuốc.
 * Đã được cập nhật để bao gồm đầy đủ các trường cần thiết,
 * đặc biệt là các trường để lưu trữ thông tin về tần suất uống thuốc.
 */
data class Medicine(
    val medicineId: String = UUID.randomUUID().toString(),
    val name: String,
    val dosage: String,
    val type: String,
    val quantity: Int,
    val notes: String = "",
    val isActive: Boolean = true,

    val frequency: Frequency = Frequency.DAILY,
    val intervalDays: Int = 0, // Chỉ có giá trị > 0 khi frequency là INTERVAL
    val daysOfWeek: Set<DayOfWeek> = emptySet() // Chỉ có giá trị khi frequency là SPECIFIC_DAYS
)
