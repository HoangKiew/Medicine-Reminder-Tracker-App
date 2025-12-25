package com.example.medinotify.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.domain.Medicine
import com.example.medinotify.data.domain.Schedule
import com.example.medinotify.data.model.Frequency
import com.example.medinotify.data.repository.MedicineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

enum class DayStatus { NONE, UPCOMING, COMPLETED, MISSED }

data class ScheduleWithMedicine(
    val schedule: Schedule,
    val medicine: Medicine?,
    val status: DayStatus
)

class CalendarViewModel(
    private val repository: MedicineRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()


    val schedulesForSelectedDay: StateFlow<List<ScheduleWithMedicine>> = _selectedDate
        .flatMapLatest { selectedDate ->
            combine(repository.getAllSchedules(), repository.getAllMedicines()) { allSchedules, allMedicines ->
                val results = mutableListOf<ScheduleWithMedicine>()
                val today = LocalDate.now()
                val nowTime = LocalTime.now()


                val medicineTimesMap = allSchedules
                    .groupBy { it.medicineId }
                    .mapValues { entry -> entry.value.map { it.specificTimeStr }.toSet() }


                for (medicine in allMedicines) {
                    if (!medicine.isActive) continue


                    if (isMedicineScheduledForDate(selectedDate, medicine)) {


                        val times = medicineTimesMap[medicine.medicineId] ?: emptySet()

                        for (timeStr in times) {

                            val existingSchedule = allSchedules.find { s ->
                                val sDate = Instant.ofEpochMilli(s.nextScheduledTimestamp)
                                    .atZone(ZoneId.systemDefault()).toLocalDate()
                                s.medicineId == medicine.medicineId &&
                                        s.specificTimeStr == timeStr &&
                                        sDate == selectedDate
                            }

                            if (existingSchedule != null) {

                                val status = if (existingSchedule.reminderStatus) DayStatus.COMPLETED else DayStatus.MISSED


                                val finalStatus = if (selectedDate == today && !existingSchedule.reminderStatus) {
                                    val t = try { LocalTime.parse(timeStr) } catch(e:Exception){ LocalTime.MIN }
                                    if (t.isAfter(nowTime)) DayStatus.UPCOMING else DayStatus.MISSED
                                } else status

                                results.add(ScheduleWithMedicine(existingSchedule, medicine, finalStatus))
                            } else {

                                val virtualSchedule = Schedule(
                                    scheduleId = "virtual_${UUID.randomUUID()}",
                                    medicineId = medicine.medicineId,
                                    specificTimeStr = timeStr,
                                    nextScheduledTimestamp = selectedDate.atTime(
                                        try { LocalTime.parse(timeStr) } catch(e:Exception){ LocalTime.MIN }
                                    ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                                    reminderStatus = false
                                )


                                val status = when {
                                    selectedDate.isBefore(today) -> DayStatus.MISSED
                                    selectedDate.isAfter(today) -> DayStatus.UPCOMING
                                    else -> { // Hôm nay
                                        val t = try { LocalTime.parse(timeStr) } catch(e:Exception){ LocalTime.MIN }
                                        if (t.isBefore(nowTime)) DayStatus.MISSED else DayStatus.UPCOMING
                                    }
                                }

                                results.add(ScheduleWithMedicine(virtualSchedule, medicine, status))
                            }
                        }
                    }
                }

                results.sortedBy { it.schedule.specificTimeStr }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    val dayStatusMap: StateFlow<Map<Int, DayStatus>> = selectedDate
        .map { YearMonth.from(it) }
        .distinctUntilChanged()
        .flatMapLatest { yearMonth ->
            combine(repository.getAllMedicines(), repository.getAllSchedules()) { allMedicines, allSchedules ->
                val statusMap = mutableMapOf<Int, DayStatus>()
                val today = LocalDate.now()
                val monthStart = yearMonth.atDay(1)
                val monthEnd = yearMonth.atEndOfMonth()
                var currentDate = monthStart

                while (!currentDate.isAfter(monthEnd)) {

                    val medsForDay = allMedicines.filter { it.isActive && isMedicineScheduledForDate(currentDate, it) }

                    if (medsForDay.isNotEmpty()) {
                        if (currentDate.isAfter(today)) {

                            statusMap[currentDate.dayOfMonth] = DayStatus.UPCOMING
                        } else {

                            val schedulesOnDate = allSchedules.filter { s ->
                                val sDate = Instant.ofEpochMilli(s.nextScheduledTimestamp)
                                    .atZone(ZoneId.systemDefault()).toLocalDate()
                                sDate == currentDate && medsForDay.any { it.medicineId == s.medicineId }
                            }

                            if (schedulesOnDate.isEmpty()) {

                                statusMap[currentDate.dayOfMonth] = DayStatus.MISSED
                            } else {
                                val allTaken = schedulesOnDate.all { it.reminderStatus }
                                statusMap[currentDate.dayOfMonth] = if (allTaken) DayStatus.COMPLETED else DayStatus.MISSED
                            }
                        }
                    }
                    currentDate = currentDate.plusDays(1)
                }
                statusMap
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun changeMonth(offset: Int) { _selectedDate.update { it.plusMonths(offset.toLong()).withDayOfMonth(1) } }
    fun onDaySelected(day: Int) { _selectedDate.update { it.withDayOfMonth(day) } }


    private fun isMedicineScheduledForDate(date: LocalDate, medicine: Medicine): Boolean {
        val startDate = Instant.ofEpochMilli(medicine.startDateTimestamp).atZone(ZoneId.systemDefault()).toLocalDate()


        if (date.isBefore(startDate)) return false



        return when (medicine.frequencyType) {
            Frequency.DAILY -> true
            Frequency.SPECIFIC_DAYS -> {
                val scheduledDays = medicine.scheduleValue?.split(",")?.mapNotNull {
                    try { DayOfWeek.valueOf(it.trim()) } catch (e: Exception) { null }
                } ?: emptyList()
                scheduledDays.contains(date.dayOfWeek)
            }
            Frequency.INTERVAL -> {
                val interval = medicine.scheduleValue?.toIntOrNull() ?: 1
                val daysBetween = ChronoUnit.DAYS.between(startDate, date)
                daysBetween >= 0 && (daysBetween % interval == 0L)
            }
        }
    }
}