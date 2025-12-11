package com.example.medinotify.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.domain.Medicine
import com.example.medinotify.data.domain.Schedule
import com.example.medinotify.data.repository.MedicineRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

// Lớp data để kết hợp thông tin Schedule và Medicine cho UI
data class ScheduleWithMedicine(
    val schedule: Schedule,
    val medicine: Medicine?
)

class CalendarViewModel(
    private val repository: MedicineRepository
) : ViewModel() {

    // --- State: Dùng LocalDate thay vì Calendar ---
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // --- Dữ liệu từ Repository ---

    // Flow để lấy thông tin chi tiết (Schedule + Medicine) cho ngày được chọn
    val schedulesForSelectedDay: StateFlow<List<ScheduleWithMedicine>> = _selectedDate
        .flatMapLatest { date ->
            // Lấy danh sách Schedule cho ngày được chọn
            val schedulesFlow = repository.getSchedulesForDate(date)

            // Lấy tất cả các loại thuốc
            val medicinesFlow = repository.getAllMedicines()

            // Kết hợp hai luồng dữ liệu
            combine(schedulesFlow, medicinesFlow) { schedules, medicines ->
                // Tạo một Map để tra cứu Medicine theo ID cho hiệu quả
                val medicineMap = medicines.associateBy { it.medicineId }

                // Ghép mỗi Schedule với Medicine tương ứng
                schedules.map { schedule ->
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

    // Flow chứa một Set các ngày (kiểu Int) có lịch uống thuốc trong tháng
    val scheduledDaysInMonth: StateFlow<Set<Int>> = selectedDate
        .map { YearMonth.from(it) } // Chỉ tính toán lại khi tháng thay đổi
        .distinctUntilChanged()
        .flatMapLatest { yearMonth ->
            // Lặp qua từng ngày trong tháng để lấy lịch
            val daysInMonth = (1..yearMonth.lengthOfMonth()).map { day ->
                repository.getSchedulesForDate(yearMonth.atDay(day))
                    .map { schedules ->
                        // Nếu có lịch, trả về ngày đó. Nếu không, trả về null.
                        if (schedules.isNotEmpty()) day else null
                    }
            }

            // Kết hợp kết quả của tất cả các ngày
            combine(daysInMonth) { dailyResults ->
                dailyResults.filterNotNull().toSet()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )


    // --- Hàm xử lý sự kiện từ UI ---

    fun changeMonth(offset: Int) {
        _selectedDate.update { currentDate ->
            val newDate = currentDate.plusMonths(offset.toLong())
            // Khi chuyển tháng, mặc định chọn ngày 1 để tránh lỗi
            newDate.withDayOfMonth(1)
        }
    }

    fun onDaySelected(day: Int) {
        _selectedDate.update { currentDate ->
            // Giữ nguyên tháng và năm, chỉ thay đổi ngày
            currentDate.withDayOfMonth(day)
        }
    }
}
