package com.example.medinotify.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.domain.LogEntry
import com.example.medinotify.data.domain.Medicine
import com.example.medinotify.data.repository.MedicineRepository
import com.example.medinotify.data.model.MedicineHistoryUi
import com.example.medinotify.data.domain.LogStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val repository: MedicineRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Sử dụng flow để lấy userId một cách an toàn
    private val userId: Flow<String?> = flow { emit(repository.getCurrentUserId()) }
        .shareIn(viewModelScope, SharingStarted.Lazily, 1)

    // 1. Flow: Lấy LogEntry cho ngày được chọn
    private val logEntriesForSelectedDate: Flow<List<LogEntry>> =
        combine(_selectedDate, userId) { date, uid -> date to uid }
            .flatMapLatest { (date, uid) ->
                if (uid == null) {
                    return@flatMapLatest flowOf(emptyList())
                }
                repository.getLogEntriesForDate(date)
            }

    // 2. Flow: Mapping và Kết hợp LogEntry với Medicine Map
    val filteredHistory: StateFlow<List<MedicineHistoryUi>> =
        combine(logEntriesForSelectedDate, _searchQuery) { logList, query ->

            // ✅ BƯỚC 1: Lấy Map từ Repository (suspend call)
            // Khối này đảm bảo các Map được cập nhật/tải nhanh nhất có thể.
            val nameMap = repository.getMedicineNameMap()
            val dosageMap = repository.getMedicineDosageMap()

            val mappedList = logList.mapNotNull { logEntry ->
                // ✅ BƯỚC 2: Mapping LogEntry sang UI Model bằng Map
                mapLogEntryToUi(
                    logEntry = logEntry,
                    name = nameMap[logEntry.medicineId],
                    dosage = dosageMap[logEntry.medicineId]
                )
            }
            // Sắp xếp theo intakeTime giảm dần (mới nhất lên đầu)
            val sortedList = mappedList.sortedByDescending { it.intakeTime }

            // ✅ BƯỚC 3: Lọc theo tìm kiếm
            if (query.isBlank()) {
                sortedList
            } else {
                sortedList.filter { uiItem ->
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
     * Hàm mapping LogEntry sang MedicineHistoryUi
     */
    private fun mapLogEntryToUi(logEntry: LogEntry, name: String?, dosage: String?): MedicineHistoryUi? {
        if (name == null || dosage == null) return null

        // Định dạng thời gian uống thuốc thực tế
        val timeString = Instant.ofEpochMilli(logEntry.intakeTime)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))

        return MedicineHistoryUi(
            id = logEntry.logId,
            name = name,
            dosage = dosage,
            time = timeString,
            intakeTime = logEntry.intakeTime,
            isTaken = logEntry.status == LogStatus.TAKEN.name        )
    }
}