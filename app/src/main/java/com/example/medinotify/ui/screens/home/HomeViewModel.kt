package com.example.medinotify.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// ✅ BƯỚC 1: IMPORT CÁC LỚP CẦN THIẾT
import com.example.medinotify.data.domain.Schedule
import com.example.medinotify.data.repository.MedicineRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Các lớp UI State không thay đổi, nhưng giữ ở đây để tham khảo
data class HomeUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val medicineSchedules: List<MedicineItem> = emptyList(),
    val isLoading: Boolean = true
)

data class MedicineItem(
    val name: String,
    val description: String,
    val time: String, // Đã chuyển thành String để UI hiển thị trực tiếp
    val isTaken: Boolean
)


@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(private val repository: MedicineRepository) : ViewModel() {

    // _selectedDate là nguồn sự thật (source of truth) cho ngày được chọn.
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<HomeUiState> = _selectedDate
        .flatMapLatest { date ->
            // ✅ SỬA 2: Lấy hai luồng dữ liệu riêng biệt từ Repository
            val schedulesFlow = repository.getSchedulesForDate(date)
            val medicinesFlow = repository.getAllMedicines()

            // ✅ SỬA 3: Kết hợp (combine) hai luồng dữ liệu lại
            combine(schedulesFlow, medicinesFlow) { schedules, medicines ->
                // Tạo một Map để tra cứu thông tin Medicine theo ID cho hiệu quả
                val medicineMap = medicines.associateBy { it.medicineId }

                val medicineItems = schedules.mapNotNull { schedule ->
                    // Tìm thuốc tương ứng với lịch trình
                    val medicine = medicineMap[schedule.medicineId]
                    if (medicine != null) {
                        // Chuyển đổi thành đối tượng MedicineItem mà UI cần
                        MedicineItem(
                            name = medicine.name,
                            description = medicine.dosage,
                            time = schedule.specificTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                            isTaken = schedule.reminderStatus
                        )
                    } else {
                        // Nếu không tìm thấy thuốc (dữ liệu không nhất quán), bỏ qua lịch trình này
                        null
                    }
                }
                // Tạo ra một HomeUiState hoàn chỉnh
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
