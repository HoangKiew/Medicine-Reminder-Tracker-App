package com.example.medinotify.data.local

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date


class Converters {

    // ================== HỖ TRỢ KIỂU CŨ (java.util.Date) ==================
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // ================== ✨ HỖ TRỢ KIỂU MỚI (java.time) ==================

    // 🔴 SỬA QUAN TRỌNG: Đổi từ ISO_LOCAL_TIME sang pattern "HH:mm"
    // Điều này giúp khớp chính xác với chuỗi giờ bạn lưu trong ScheduleEntity
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    // --- LocalTime <-> String ---
    @TypeConverter
    fun fromTimeString(value: String?): LocalTime? {
        return value?.let {
            try {
                // Ưu tiên parse theo định dạng ngắn HH:mm
                LocalTime.parse(it, timeFormatter)
            } catch (e: Exception) {
                try {
                    // Fallback: Nếu dữ liệu cũ có giây (HH:mm:ss), thử parse kiểu mặc định
                    LocalTime.parse(it)
                } catch (ex: Exception) {
                    null
                }
            }
        }
    }

    @TypeConverter
    fun localTimeToString(date: LocalTime?): String? {
        // Luôn lưu vào DB dưới dạng HH:mm (bỏ giây)
        return date?.format(timeFormatter)
    }

    // --- LocalDate <-> String ---
    @TypeConverter
    fun fromDateString(value: String?): LocalDate? {
        return value?.let {
            try {
                LocalDate.parse(it, dateFormatter)
            } catch (e: Exception) {
                null
            }
        }
    }

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? {
        return date?.format(dateFormatter)
    }
}