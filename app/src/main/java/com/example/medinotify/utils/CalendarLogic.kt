package com.example.medinotify.utils

import com.example.medinotify.data.domain.Medicine
import com.example.medinotify.data.model.Frequency
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.ceil // Cần import ceil để làm tròn

object CalendarLogic {

    fun isScheduledForDate(
        date: LocalDate,
        medicine: Medicine,

        dosesPerDay: Int
    ): Boolean {


        val effectiveStartDate = Instant.ofEpochMilli(medicine.startDateTimestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()



        val totalQuantity = medicine.quantity
        val timesPerDay = dosesPerDay


        val durationDays: Long = if (totalQuantity <= 0) {
            Long.MAX_VALUE
        } else if (timesPerDay <= 0) {

            Long.MAX_VALUE
        } else {

            ceil(totalQuantity.toDouble() / timesPerDay.toDouble()).toLong()
        }


        val effectiveEndDate = if (durationDays == Long.MAX_VALUE) {
            LocalDate.MAX
        } else {

            effectiveStartDate.plusDays(durationDays).minusDays(1)
        }





        if (date.isBefore(effectiveStartDate)) {
            return false
        }


        if (date.isAfter(effectiveEndDate)) {
            return false
        }


        val daysSinceStart = ChronoUnit.DAYS.between(effectiveStartDate, date)

        return when (medicine.frequencyType) {

            Frequency.DAILY -> true

            Frequency.SPECIFIC_DAYS -> {
                val selectedDays = parseScheduleValueToDays(medicine.scheduleValue)
                selectedDays.contains(date.dayOfWeek)
            }

            Frequency.INTERVAL -> {
                val interval = medicine.scheduleValue?.toIntOrNull() ?: 1

                daysSinceStart >= 0 && daysSinceStart.toInt() % interval == 0
            }
        }
    }


    private fun parseScheduleValueToDays(value: String?): Set<DayOfWeek> {
        return if (value != null) {
            value.split(",").mapNotNull {
                try { DayOfWeek.valueOf(it.trim()) } catch (e: Exception) { null }
            }.toSet()
        } else emptySet()
    }
}