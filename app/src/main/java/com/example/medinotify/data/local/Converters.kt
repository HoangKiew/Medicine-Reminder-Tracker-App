package com.example.medinotify.data.local

import androidx.room.TypeConverter
import com.example.medinotify.data.model.Frequency // ✅ THÊM IMPORT NÀY
import java.time.DayOfWeek // ✅ THÊM IMPORT NÀY
import java.util.Date

/**
 * Bộ chuyển đổi kiểu dữ liệu cho Room.
 * Giúp Room hiểu cách lưu trữ và đọc các kiểu dữ liệu phức tạp như
 * Date, Set<DayOfWeek>, và enum Frequency.
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

    // ✅ BƯỚC QUAN TRỌNG: THÊM CÁC CONVERTER CÒN THIẾU

    // --- Converter cho Set<DayOfWeek> <-> String ---
    /**
     * Chuyển một Set các ngày trong tuần thành một chuỗi String duy nhất để lưu vào DB.
     * Ví dụ: { MONDAY, WEDNESDAY } -> "MONDAY,WEDNESDAY"
     */
    @TypeConverter
    fun fromDayOfWeekSet(days: Set<DayOfWeek>?): String? {
        if (days == null) {
            return null
        }
        return days.joinToString(",") { it.name }
    }

    /**
     * Chuyển một chuỗi String từ DB thành một Set các ngày trong tuần.
     * Ví dụ: "MONDAY,WEDNESDAY" -> { MONDAY, WEDNESDAY }
     */
    @TypeConverter
    fun toDayOfWeekSet(daysString: String?): Set<DayOfWeek>? {
        if (daysString.isNullOrEmpty()) {
            return emptySet()
        }
        return daysString.split(',').map { DayOfWeek.valueOf(it) }.toSet()
    }

    // --- Converter cho enum Frequency <-> String ---
    /**
     * Chuyển enum Frequency thành tên của nó (String) để lưu vào DB.
     * Ví dụ: Frequency.DAILY -> "DAILY"
     */
    @TypeConverter
    fun fromFrequency(frequency: Frequency?): String? {
        return frequency?.name
    }

    /**
     * Chuyển một chuỗi String từ DB thành enum Frequency tương ứng.
     * Ví dụ: "DAILY" -> Frequency.DAILY
     */
    @TypeConverter
    fun toFrequency(frequencyName: String?): Frequency? {
        return frequencyName?.let { Frequency.valueOf(it) }
    }
}
