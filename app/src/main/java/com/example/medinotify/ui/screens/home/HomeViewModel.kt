package com.example.medinotify.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.repository.MedicineRepository
import com.example.medinotify.utils.CalendarLogic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Data class cho UI State (Giữ nguyên)
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

            // Lấy TẤT CẢ Schedules và Medicines
            val allSchedulesFlow = repository.getAllSchedules()
            val allMedicinesFlow = repository.getAllMedicines()

            combine(allSchedulesFlow, allMedicinesFlow) { allSchedules, allMedicines ->

                val medicineMap = allMedicines.associateBy { it.medicineId }
                val medicineItems = mutableListOf<MedicineItem>()

                // ✅ BƯỚC MỚI: Tạo Map để đếm số lần uống cho mỗi thuốc
                val dosesPerDayMap = allSchedules
                    .groupBy { it.medicineId }
                    .mapValues { it.value.size }

                for (schedule in allSchedules) {
                    val medicine = medicineMap[schedule.medicineId]

                    if (medicine == null || !medicine.isActive) continue

                    val dosesPerDay = dosesPerDayMap[medicine.medicineId] ?: 0

                    // SỬ DỤNG LOGIC LẶP LẠI (Frequency)
                    // ✅ FIX 2: Truyền dosesPerDay vào CalendarLogic
                    val isScheduledToday = CalendarLogic.isScheduledForDate(
                        date = selectedDate,
                        medicine = medicine,
                        dosesPerDay = dosesPerDay // <-- Đã thêm tham số
                    )

                    // Chỉ thêm vào danh sách nếu nó thực sự có lịch uống vào ngày này
                    if (isScheduledToday) {
                        medicineItems.add(
                            MedicineItem(
                                id = medicine.medicineId,
                                name = medicine.name,
                                description = medicine.dosage,
                                time = schedule.specificTimeStr,
                                isTaken = schedule.reminderStatus
                            )
                        )
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

    // --- HÀM XÓA THUỐC ---
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
}