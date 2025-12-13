package com.example.medinotify.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.domain.LogEntry
import com.example.medinotify.data.domain.Medicine
import com.example.medinotify.data.repository.MedicineRepository
import com.example.medinotify.data.model.MedicineHistoryUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Giả định LogStatus là object trong file này
object LogStatus {
    const val TAKEN = "TAKEN"
    const val SKIPPED = "SKIPPED"
}

// Data class cho UI (được cung cấp trước đó)
data class MedicineHistoryUi(
    val id: String,
    val name: String,
    val dosage: String,
    val time: String,
    val isTaken: Boolean,
    val intakeTime: Long
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val repository: MedicineRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val userId = repository.getCurrentUserId()

    // 1. Flow: Lấy LogEntry cho ngày được chọn (sử dụng logic múi giờ đã sửa trong Repository)
    private val logEntriesForSelectedDate: Flow<List<LogEntry>> =
        _selectedDate.flatMapLatest { date ->
            if (userId == null) {
                return@flatMapLatest flowOf(emptyList())
            }
            repository.getLogEntriesForDate(date) // Sử dụng hàm đã đúng logic thời gian
        }

    // 2. Flow: Mapping và Kết hợp LogEntry với Medicine (đã tối ưu hóa bằng combine)
    private val mappedHistoryUi: Flow<List<MedicineHistoryUi>> = logEntriesForSelectedDate
        .combine(repository.getAllMedicines()) { logList, medicineList ->
            val medicineMap = medicineList.associateBy { it.medicineId }

            val mappedList = logList.mapNotNull { logEntry ->
                mapLogEntryToUi(logEntry, medicineMap[logEntry.medicineId])
            }
            // Sắp xếp theo intakeTime giảm dần (mới nhất lên đầu)
            mappedList.sortedByDescending { it.intakeTime }
        }

    // 3. Flow: Lọc theo tìm kiếm
    val filteredHistory: StateFlow<List<MedicineHistoryUi>> =
        combine(mappedHistoryUi, _searchQuery) { historyList, query ->
            if (query.isBlank()) {
                historyList
            } else {
                historyList.filter { uiItem ->
                    uiItem.name.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onDateSelected(newDate: LocalDate) {
        _selectedDate.value = newDate
    }

    private fun mapLogEntryToUi(logEntry: LogEntry, medicine: Medicine?): MedicineHistoryUi? {
        if (medicine == null) return null

        val timeString = Instant.ofEpochMilli(logEntry.intakeTime)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))

        return MedicineHistoryUi(
            id = logEntry.logId,
            name = medicine.name,
            dosage = medicine.dosage,
            time = timeString,
            intakeTime = logEntry.intakeTime,
            isTaken = logEntry.status == LogStatus.TAKEN // Dùng hằng số String
        )
    }
}