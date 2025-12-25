package com.example.medinotify.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.domain.Medicine
import com.example.medinotify.data.model.Frequency
import com.example.medinotify.data.repository.MedicineRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class HomeUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val medicineSchedules: List<MedicineItem> = emptyList(),
    val isLoading: Boolean = true
)

data class MedicineItem(
    val id: String,
    val name: String,
    val description: String,
    val time: String,
    val isTaken: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(private val repository: MedicineRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _refreshTrigger = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = combine(_selectedDate, _refreshTrigger) { date, _ ->
        date
    }
        .flatMapLatest { selectedDate ->

            combine(repository.getAllSchedules(), repository.getAllMedicines()) { allSchedules, allMedicines ->
                val medicineItems = mutableListOf<MedicineItem>()


                val medicineTimesMap = allSchedules
                    .groupBy { it.medicineId }
                    .mapValues { entry -> entry.value.map { it.specificTimeStr }.toSet() }

                // 2. Duyệt qua TẤT CẢ các thuốc (thay vì duyệt Schedule)
                for (medicine in allMedicines) {
                    if (!medicine.isActive) continue

                    // 3. Kiểm tra thuốc có lịch vào ngày 'selectedDate' không? (Quy tắc lặp)
                    if (isMedicineScheduledForDate(selectedDate, medicine)) {

                        // Lấy các giờ uống của thuốc này
                        val times = medicineTimesMap[medicine.medicineId] ?: emptySet()

                        for (timeStr in times) {
                            // 4. Tìm xem đã có bản ghi trong DB chưa
                            val existingSchedule = allSchedules.find { s ->
                                val sDate = Instant.ofEpochMilli(s.nextScheduledTimestamp)
                                    .atZone(ZoneId.systemDefault()).toLocalDate()
                                s.medicineId == medicine.medicineId &&
                                        s.specificTimeStr == timeStr &&
                                        sDate == selectedDate
                            }

                            if (existingSchedule != null) {
                                // A. ĐÃ CÓ TRONG DB (Lịch thực)
                                medicineItems.add(
                                    MedicineItem(
                                        id = medicine.medicineId,
                                        name = medicine.name,
                                        description = medicine.dosage,
                                        time = existingSchedule.specificTimeStr,
                                        isTaken = existingSchedule.reminderStatus
                                    )
                                )
                            } else {
                                // B. CHƯA CÓ TRONG DB (Lịch ảo - Tương lai)
                                // Hiển thị item mặc định là chưa uống
                                medicineItems.add(
                                    MedicineItem(
                                        id = medicine.medicineId,
                                        name = medicine.name,
                                        description = medicine.dosage,
                                        time = timeStr,
                                        isTaken = false // Mặc định tương lai là chưa uống
                                    )
                                )
                            }
                        }
                    }
                }

                // Sắp xếp theo giờ
                val sortedItems = medicineItems.sortedBy { it.time }

                HomeUiState(
                    selectedDate = selectedDate,
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

    fun refreshData() {
        _refreshTrigger.value += 1
    }

    fun deleteMedicine(medicineId: String) {
        viewModelScope.launch {
            try {
                repository.deleteMedicine(medicineId)
                refreshData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- HÀM KIỂM TRA QUY TẮC LẶP (Logic cốt lõi) ---
    private fun isMedicineScheduledForDate(date: LocalDate, medicine: Medicine): Boolean {
        // 1. Không hiện trước ngày bắt đầu
        val startDate = Instant.ofEpochMilli(medicine.startDateTimestamp)
            .atZone(ZoneId.systemDefault()).toLocalDate()
        if (date.isBefore(startDate)) return false

        // 2. Kiểm tra tần suất
        return when (medicine.frequencyType) {
            Frequency.DAILY -> true
            Frequency.SPECIFIC_DAYS -> {
                // Ví dụ: "MONDAY,WEDNESDAY"
                val scheduledDays = medicine.scheduleValue?.split(",")?.mapNotNull {
                    try { DayOfWeek.valueOf(it.trim()) } catch (e: Exception) { null }
                } ?: emptyList()
                scheduledDays.contains(date.dayOfWeek)
            }
            Frequency.INTERVAL -> {
                // Ví dụ: "2" (Cách 2 ngày)
                val interval = medicine.scheduleValue?.toIntOrNull() ?: 1
                val daysBetween = ChronoUnit.DAYS.between(startDate, date)
                daysBetween >= 0 && (daysBetween % interval == 0L)
            }
        }
    }
}