package com.example.medinotify.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.domain.Schedule
import com.example.medinotify.data.repository.MedicineRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Các data class giữ nguyên
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

    // ✨ THÊM MỚI: Biến này dùng để kích hoạt việc tải lại dữ liệu
    private val _refreshTrigger = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = combine(_selectedDate, _refreshTrigger) { date, _ ->
        date // Khi ngày đổi HOẶC trigger đổi -> trả về ngày để flatMap xử lý tiếp
    }
        .flatMapLatest { date ->
            val schedulesFlow = repository.getSchedulesForDate(date)
            val medicinesFlow = repository.getAllMedicines()

            combine(schedulesFlow, medicinesFlow) { schedules, medicines ->
                val medicineMap = medicines.associateBy { it.medicineId }

                val medicineItems = schedules.mapNotNull { schedule ->
                    val medicine = medicineMap[schedule.medicineId]
                    if (medicine != null) {
                        MedicineItem(
                            name = medicine.name,
                            description = medicine.dosage,
                            time = schedule.specificTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                            isTaken = schedule.reminderStatus // Trạng thái xanh/đỏ lấy từ đây
                        )
                    } else {
                        null
                    }
                }

                // Sắp xếp theo giờ để danh sách đẹp hơn
                val sortedItems = medicineItems.sortedBy { it.time }

                HomeUiState(
                    selectedDate = date,
                    medicineSchedules = sortedItems,
                    isLoading = false
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(isLoading = true)
        )

    fun loadSchedulesForDate(date: LocalDate) {
        _selectedDate.value = date
    }

    // ✨ THÊM MỚI: Hàm này gọi để ép ViewModel tải lại dữ liệu
    fun refreshData() {
        _refreshTrigger.value += 1
    }
}