package com.example.medinotify.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.domain.LogEntry
import com.example.medinotify.data.domain.Medicine
import com.example.medinotify.data.repository.MedicineRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Các hằng số để tránh "magic strings"
object LogStatus {
    const val TAKEN = "TAKEN"
    const val SKIPPED = "SKIPPED"
}

// Data Class để hiển thị trên UI (Giữ nguyên)
data class MedicineHistoryUi(
    val id: String,
    val name: String,
    val dosage: String,
    val time: String, // Giờ uống (HH:mm)
    val isTaken: Boolean // True nếu status là TAKEN
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val repository: MedicineRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Lấy userId một lần và tái sử dụng
    private val userId = repository.getCurrentUserId()

    // Flow chính: Lấy dữ liệu LogEntry dựa trên ngày (LocalDate)
    private val logEntriesForSelectedDate: Flow<List<LogEntry>> =
        _selectedDate.flatMapLatest { date ->
            if (userId == null) {
                return@flatMapLatest flowOf(emptyList())
            }

            // Chuyển LocalDate thành epoch milliseconds
            val dateStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val dateEnd = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

            // Gọi hàm repository với khoảng thời gian đã tính
            repository.getLogHistoryForDateRange(dateStart, dateEnd)
        }

    // Flow trung gian: Ánh xạ LogEntry sang UI Model
    private val mappedHistoryUi: Flow<List<MedicineHistoryUi>> = logEntriesForSelectedDate
        .transformLatest { logList ->
            // Lấy danh sách thuốc 1 lần duy nhất để tối ưu
            val medicineMap = repository.getAllMedicines().first().associateBy { it.medicineId }
            val mappedList = logList.mapNotNull { logEntry ->
                mapLogEntryToUi(logEntry, medicineMap[logEntry.medicineId])
            }
            emit(mappedList.sortedBy { it.time }) // Sắp xếp theo giờ uống
        }

    // Flow cuối cùng để UI lắng nghe: Kết hợp dữ liệu đã map với chuỗi tìm kiếm
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

    /**
     * Hàm Ánh xạ LogEntry sang UI Model.
     * Hàm này không cần 'suspend' nữa vì thông tin Medicine đã được cung cấp.
     */
    private fun mapLogEntryToUi(logEntry: LogEntry, medicine: Medicine?): MedicineHistoryUi? {
        if (medicine == null) return null

        // ✅ SỬA 1: Sử dụng thuộc tính 'intakeTime' đúng từ lớp LogEntry mới
        val timeString = Instant.ofEpochMilli(logEntry.intakeTime)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))

        return MedicineHistoryUi(
            id = logEntry.logId,
            name = medicine.name,
            dosage = medicine.dosage,
            time = timeString,
            // ✅ SỬA 2: Sử dụng hằng số để kiểm tra trạng thái
            isTaken = logEntry.status == LogStatus.TAKEN
        )
    }
}
