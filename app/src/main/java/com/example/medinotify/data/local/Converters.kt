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