package com.example.medinotify.ui.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.domain.LogEntry
import com.example.medinotify.data.repository.MedicineRepository
import com.example.medinotify.data.model.MedicineHistoryUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Giả định LogStatus là object trong file này
object LogStatusDetail {
    const val TAKEN = "TAKEN"
}

class MedicineHistoryDetailViewModel(
    private val repository: MedicineRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 1. Lấy ngày từ tham số điều hướng
    private val dateString: String = savedStateHandle.get<String>("date") ?: LocalDate.now().toString()
    val selectedDate: LocalDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)

    // 2. Khởi tạo StateFlow bằng cách gọi Repository chỉ cho ngày đã chọn
    val historyUiState: StateFlow<List<MedicineHistoryUi>> =
        repository.getLogEntriesForDate(selectedDate)
            .map { logEntries ->
                // Lấy bản đồ tên và liều lượng của thuốc (cần chạy trong IO Dispatcher vì là hàm suspend)
                val medicineNameMap = withContext(Dispatchers.IO) { repository.getMedicineNameMap() }
                val medicineDosageMap = withContext(Dispatchers.IO) { repository.getMedicineDosageMap() }

                logEntries.mapNotNull { logEntry ->
                    mapLogEntryToUi(logEntry, medicineNameMap, medicineDosageMap)
                }.sortedByDescending { it.intakeTime }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private fun mapLogEntryToUi(
        logEntry: LogEntry,
        nameMap: Map<String, String>,
        dosageMap: Map<String, String>
    ): MedicineHistoryUi? {
        val medicineName = nameMap[logEntry.medicineId] ?: return null
        val medicineDosage = dosageMap[logEntry.medicineId] ?: "N/A"

        val timeString = logEntry.intakeTime.let {
            val localTime = Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
            localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        }

        return MedicineHistoryUi(
            id = logEntry.logId,
            name = medicineName,
            dosage = medicineDosage,
            time = timeString,
            isTaken = (logEntry.status == LogStatusDetail.TAKEN), // Dùng hằng số String
            intakeTime = logEntry.intakeTime
        )
    }
}