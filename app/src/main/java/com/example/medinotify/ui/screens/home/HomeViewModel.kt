package com.example.medinotify.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.domain.Schedule
import com.example.medinotify.data.repository.MedicineRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId // Cần thiết cho việc so sánh giờ
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

// Các lớp UI State không thay đổi (Giữ nguyên)
data class HomeUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val medicineSchedules: List<MedicineItem> = emptyList(),
    val isLoading: Boolean = true
)

data class MedicineItem(
    val name: String,
    val description: String,
    val time: String,
    val isTaken: Boolean
)


@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(private val repository: MedicineRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<HomeUiState> = _selectedDate
        .flatMapLatest { date ->
            //Lấy BA luồng dữ liệu cần thiết từ Repository
            val schedulesFlow = repository.getSchedulesForDate(date)
            val medicinesFlow = repository.getAllMedicines()
            val logEntriesFlow = repository.getLogEntriesForDate(date) // ⭐️ BỔ SUNG LOG ENTRIES

            //Kết hợp (combine) BA luồng dữ liệu lại
            combine(schedulesFlow, medicinesFlow, logEntriesFlow) { schedules, medicines, logEntries ->

                // Tạo một Map để tra cứu thông tin Medicine theo ID
                val medicineMap = medicines.associateBy { it.medicineId }

                // Tạo Set các LogEntry Time (Epoch Millis) để tra cứu nhanh hơn
                val loggedTimes = logEntries.map { it.intakeTime }.toSet()


                val medicineItems = schedules
                    .filter { it.reminderStatus } // Chỉ hiển thị các lịch trình đã được bật
                    .mapNotNull { schedule ->
                        val medicine = medicineMap[schedule.medicineId]
                        if (medicine != null) {

                            // Lấy Timestamp của Schedule
                            val scheduleTimeMillis = schedule.nextScheduledTimestamp

                            // ⭐️ KIỂM TRA TRẠNG THÁI ĐÃ UỐNG (isTaken):
                            val isTaken = logEntries.any { logEntry ->
                                val timeDifference = Math.abs(logEntry.intakeTime - scheduleTimeMillis)
                                val maxAcceptableDiff = TimeUnit.HOURS.toMillis(2) // Chấp nhận uống sớm/muộn 2 tiếng

                                logEntry.medicineId == schedule.medicineId && timeDifference <= maxAcceptableDiff
                            }

                            // Chuyển đổi thành đối tượng MedicineItem mà UI cần
                            MedicineItem(
                                name = medicine.name,
                                description = medicine.dosage,
                                time = schedule.specificTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                isTaken = isTaken // Dùng kết quả đối chiếu Log
                            )
                        } else {
                            // Nếu không tìm thấy thuốc, bỏ qua lịch trình này
                            null
                        }
                    }
                HomeUiState(
                    selectedDate = date,
                    medicineSchedules = medicineItems,
                    isLoading = false
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(isLoading = true) // Trạng thái ban đầu
        )

    /**
     * Tải lịch uống thuốc cho một ngày được chọn.
     */
    fun loadSchedulesForDate(date: LocalDate) {
        _selectedDate.value = date
    }
}