package com.example.medinotify.data.local

import androidx.room.TypeConverter
import com.example.medinotify.data.model.Frequency
import java.time.DayOfWeek
import java.util.Date

/**
 * Bộ chuyển đổi kiểu dữ liệu cho Room.
 * ✅ ĐÃ SỬA: Cập nhật TypeConverter cho daysOfWeek để sử dụng List thay vì Set.
 */
class Converters {
    // --- Converter cho Date <-> Long (Giữ nguyên) ---
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // --- Converter cho List<DayOfWeek> <-> String (ĐÃ SỬA CHO TƯƠNG THÍCH FIREBASE) ---
    /**
     * Chuyển một List các ngày trong tuần thành một chuỗi String duy nhất để lưu vào DB.
     * Ví dụ: [MONDAY, WEDNESDAY] -> "MONDAY,WEDNESDAY"
     */
    @TypeConverter
    // ✅ THAY ĐỔI TỪ Set<DayOfWeek> SANG List<DayOfWeek>
    fun fromDayOfWeekList(days: List<DayOfWeek>?): String? {
        if (days == null) {
            return null
        }
        // Room/Kotlin sẽ xử lý việc map List này thành Set khi chuyển sang Domain (như trong Entity Mapper)
        return days.joinToString(",") { it.name }
    }

    /**
     * Chuyển một chuỗi String từ DB thành một List các ngày trong tuần.
     * Ví dụ: "MONDAY,WEDNESDAY" -> [MONDAY, WEDNESDAY]
     */
    @TypeConverter
    // ✅ THAY ĐỔI TỪ Set<DayOfWeek> SANG List<DayOfWeek>
    fun toDayOfWeekList(daysString: String?): List<DayOfWeek>? {
        if (daysString.isNullOrEmpty()) {
            return emptyList()
        }
        // Trả về List, khớp với kiểu dữ liệu mới trong MedicineEntity
        return daysString.split(',')
            .map { DayOfWeek.valueOf(it) }
            .toList()
    }

    // --- Converter cho enum Frequency <-> String (Giữ nguyên) ---
    @TypeConverter
    fun fromFrequency(frequency: Frequency?): String? {
        return frequency?.name
    }

    @TypeConverter
    fun toFrequency(frequencyName: String?): Frequency? {
        return frequencyName?.let { Frequency.valueOf(it) }
    }
}