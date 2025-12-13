package com.example.medinotify.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.domain.Medicine
import com.example.medinotify.data.domain.Schedule
import com.example.medinotify.data.repository.MedicineRepository
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class ScheduleWithMedicine(
    val schedule: Schedule,
    val medicine: Medicine?
)

class CalendarViewModel(
    private val repository: MedicineRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // 1. Lấy danh sách thuốc chi tiết cho ngày được chọn
    val schedulesForSelectedDay: StateFlow<List<ScheduleWithMedicine>> = _selectedDate
        .flatMapLatest { date ->
            // Lấy TOÀN BỘ lịch
            val schedulesFlow = repository.getSchedulesForDate(date)
            val medicinesFlow = repository.getAllMedicines()

            combine(schedulesFlow, medicinesFlow) { schedules, medicines ->
                val medicineMap = medicines.associateBy { it.medicineId }

                schedules.filter { schedule ->
                    // ✨ BỘ LỌC: Chỉ lấy lịch của ngày được chọn ✨
                    val scheduleDate = Instant.ofEpochMilli(schedule.nextScheduledTimestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    scheduleDate.isEqual(date)
                }.map { schedule ->
                    ScheduleWithMedicine(
                        schedule = schedule,
                        medicine = medicineMap[schedule.medicineId]
                    )
                }
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
            // Lấy TOÀN BỘ lịch trình một lần duy nhất
            // Lưu ý: repository.getSchedulesForDate giờ trả về tất cả, nên ta truyền ngày nào cũng được
            repository.getSchedulesForDate(LocalDate.now())
                .map { allSchedules ->
                    val daysWithSchedule = mutableSetOf<Int>()

                    allSchedules.forEach { schedule ->
                        // Đổi timestamp ra ngày
                        val date = Instant.ofEpochMilli(schedule.nextScheduledTimestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        // Nếu lịch này thuộc tháng đang xem -> Thêm ngày đó vào danh sách
                        if (YearMonth.from(date) == yearMonth) {
                            daysWithSchedule.add(date.dayOfMonth)
                        }
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