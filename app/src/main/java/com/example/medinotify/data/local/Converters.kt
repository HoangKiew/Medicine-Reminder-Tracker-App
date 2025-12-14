package com.example.medinotify.data.local

import androidx.room.TypeConverter
import com.example.medinotify.data.model.Frequency
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.DayOfWeek
// Loại bỏ các imports java.time không cần thiết
// import java.time.Instant
// import java.time.LocalDate
// import java.time.LocalTime
// import java.time.ZoneId
// import java.time.format.DateTimeFormatter
// import java.util.Date


class Converters {

    // ❌ XÓA: Loại bỏ các Converter cho java.util.Date (Không cần thiết)
    // ❌ XÓA: Loại bỏ các Converter cho LocalTime <-> String (Entity đã dùng String)
    // ❌ XÓA: Loại bỏ các Converter cho LocalDate <-> Long (Entity đã dùng Long)

    // ================== ✨ HỖ TRỢ KIỂU TẦN SUẤT (Frequency & DayOfWeek) ==================

    // --- Frequency <-> String ---
    @TypeConverter
    fun fromFrequencyString(value: String?): Frequency? {
        return value?.let {
            try {
                Frequency.valueOf(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    @TypeConverter
    fun frequencyToString(frequency: Frequency?): String? {
        return frequency?.name
    }

    // --- List<DayOfWeek> <-> String JSON (Cần cho SPECIFIC_DAYS trong MedicineEntity.scheduleValue) ---
    @TypeConverter
    fun fromDayOfWeekList(value: String?): List<DayOfWeek>? {
        return value?.let {
            val listType = object : TypeToken<List<DayOfWeek>>() {}.type
            Gson().fromJson(it, listType)
        }
    }

    @TypeConverter
    fun dayOfWeekListToString(list: List<DayOfWeek>?): String? {
        return Gson().toJson(list)
    }
}