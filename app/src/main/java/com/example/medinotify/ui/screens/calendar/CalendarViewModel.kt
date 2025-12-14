package com.example.medinotify.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.domain.Medicine
import com.example.medinotify.data.domain.Schedule
import com.example.medinotify.data.repository.MedicineRepository
import com.example.medinotify.utils.CalendarLogic
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

data class ScheduleWithMedicine(
    val schedule: Schedule,
    val medicine: Medicine?
)

class CalendarViewModel(
    private val repository: MedicineRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // 1. Lấy danh sách Schedule chi tiết cho ngày được chọn
    val schedulesForSelectedDay: StateFlow<List<ScheduleWithMedicine>> = _selectedDate
        .flatMapLatest { selectedDate ->

            val allSchedulesFlow = repository.getAllSchedules()
            val medicinesFlow = repository.getAllMedicines()

            combine(allSchedulesFlow, medicinesFlow) { allSchedules, allMedicines ->
                val medicineMap = allMedicines.associateBy { it.medicineId }

                //Tạo Map để đếm số lần uống cho mỗi thuốc
                val dosesPerDayMap = allSchedules
                    .groupBy { it.medicineId }
                    .mapValues { it.value.size }

                val results = mutableListOf<ScheduleWithMedicine>()

                for (schedule in allSchedules) {
                    val medicine = medicineMap[schedule.medicineId]
                    if (medicine == null || !medicine.isActive) continue

                    val dosesPerDay = dosesPerDayMap[medicine.medicineId] ?: 0

                    //Truyền dosesPerDay vào CalendarLogic
                    val isScheduledToday = CalendarLogic.isScheduledForDate(
                        date = selectedDate,
                        medicine = medicine,
                        dosesPerDay = dosesPerDay //
                    )

                    if (isScheduledToday) {
                        results.add(ScheduleWithMedicine(
                            schedule = schedule,
                            medicine = medicine
                        ))
                    }
                }
                // Sắp xếp theo giờ cụ thể
                results.sortedBy { it.schedule.specificTimeStr }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. Xác định những ngày nào trong tháng có lịch uống thuốc (để hiện chấm đỏ)
    val scheduledDaysInMonth: StateFlow<Set<Int>> = selectedDate
        .map { YearMonth.from(it) }
        .distinctUntilChanged()
        .flatMapLatest { yearMonth ->

            val allSchedulesFlow = repository.getAllSchedules()
            val allMedicinesFlow = repository.getAllMedicines()

            combine(allSchedulesFlow, allMedicinesFlow) { allSchedules, allMedicines ->

                val dosesPerDayMap = allSchedules
                    .groupBy { it.medicineId }
                    .mapValues { it.value.size }

                val daysWithSchedule = mutableSetOf<Int>()

                val monthStart = yearMonth.atDay(1)
                val monthEnd = yearMonth.atEndOfMonth()

                var currentDate = monthStart
                while (currentDate.isBefore(monthEnd.plusDays(1))) {

                    var hasScheduleOnThisDay = false

                    for (medicine in allMedicines) {
                        if (!medicine.isActive) continue

                        val dosesPerDay = dosesPerDayMap[medicine.medicineId] ?: 0

                        if (CalendarLogic.isScheduledForDate(
                                currentDate,
                                medicine,
                                dosesPerDay
                            )) {
                            hasScheduleOnThisDay = true
                            break
                        }
                    }

                    if (hasScheduleOnThisDay) {
                        daysWithSchedule.add(currentDate.dayOfMonth)
                    }

                    // Tiến lên ngày tiếp theo
                    currentDate = currentDate.plusDays(1)
                }

                daysWithSchedule
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    fun changeMonth(offset: Int) {
        _selectedDate.update { currentDate ->
            currentDate.plusMonths(offset.toLong()).withDayOfMonth(1)
        }
    }

    fun onDaySelected(day: Int) {
        _selectedDate.update { currentDate ->
            currentDate.withDayOfMonth(day)
        }
    }
}