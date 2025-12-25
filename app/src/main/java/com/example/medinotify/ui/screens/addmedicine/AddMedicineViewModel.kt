package com.example.medinotify.ui.screens.addmedicine

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.medinotify.data.domain.Medicine
import com.example.medinotify.data.domain.Schedule
import com.example.medinotify.data.model.Frequency
import com.example.medinotify.data.repository.MedicineRepository
import com.example.medinotify.worker.MedicineReminderWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.TimeUnit

class AddMedicineViewModel(
    private val repository: MedicineRepository,
    private val workManager: WorkManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddMedicineUiState(
            medicineId = savedStateHandle["medicineId"]
        )
    )
    val uiState: StateFlow<AddMedicineUiState> = _uiState.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    init {
        uiState.value.medicineId?.let { loadMedicineData(it) }
    }


    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName, uiMessage = null) }
    }

    fun onTypeChange(newType: String) {
        _uiState.update { it.copy(medicineType = newType, uiMessage = null) }
    }

    fun onDosageChange(newDosage: String) {
        _uiState.update { it.copy(dosage = newDosage, uiMessage = null) }
    }

    fun onQuantityChange(newQuantity: String) {
        if (newQuantity.all { it.isDigit() } || newQuantity.isEmpty()) {
            _uiState.update { it.copy(quantity = newQuantity, uiMessage = null) }
        }
    }

    fun onEnableReminderChange(isEnabled: Boolean) {
        _uiState.update { it.copy(enableReminder = isEnabled, uiMessage = null) }
    }

    fun onStartDateChange(date: LocalDate) {
        _uiState.update { it.copy(startDate = date, uiMessage = null) }
    }

    fun onFrequencyTypeChange(type: Frequency) {
        _uiState.update {
            it.copy(
                frequencyType = type,
                selectedDays = if (type != Frequency.SPECIFIC_DAYS) emptySet() else it.selectedDays,
                intervalDays = if (type != Frequency.INTERVAL) "1" else it.intervalDays,
                uiMessage = null
            )
        }
    }

    fun onIntervalDaysChange(interval: String) {
        if (interval.all { it.isDigit() } && interval.isNotEmpty() && (interval.toIntOrNull()
                ?: 0) > 0
        ) {
            _uiState.update { it.copy(intervalDays = interval, uiMessage = null) }
        }
    }

    fun toggleDaySelection(day: DayOfWeek) {
        _uiState.update {
            val newDays = if (it.selectedDays.contains(day)) {
                it.selectedDays - day
            } else {
                it.selectedDays + day
            }
            it.copy(selectedDays = newDays, uiMessage = null)
        }
    }

    fun addSpecificTime(time: LocalTime) {
        if (!_uiState.value.specificTimes.contains(time)) {
            _uiState.update {
                it.copy(
                    specificTimes = (it.specificTimes + time)
                        .sorted()
                        .toSet()
                )
            }
        } else {
            _uiState.update { it.copy(uiMessage = "Giờ nhắc nhở này đã tồn tại!") }
        }
    }

    //  Đảm bảo kết quả là Set
    fun removeSpecificTime(time: LocalTime) {
        _uiState.update {
            it.copy(
                specificTimes = (it.specificTimes - time)
                    .sorted()
                    .toSet()
            )
        }
    }

    fun clearUiMessage() {
        _uiState.update { it.copy(uiMessage = null) }
    }


    private fun loadMedicineData(id: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val medicine = repository.getMedicineById(id)


            val schedules = repository.getSchedulesForMedicine(id).first()

            if (medicine != null) {

                val loadedStartDate = medicine.startDateTimestamp.let {
                    if (it != 0L) LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(it),
                        ZoneId.systemDefault()
                    ).toLocalDate() else LocalDate.now()
                }


                val loadedSpecificTimes: Set<LocalTime> = schedules.mapNotNull { schedule ->
                    try {
                        LocalTime.parse(schedule.specificTimeStr, timeFormatter)
                    } catch (e: Exception) {
                        Log.e(
                            "AddMedicineVM",
                            "Error parsing schedule time: ${schedule.specificTimeStr}",
                            e
                        )
                        null
                    }
                }
                    .toSet()


                val daysOfWeekSet =
                    parseScheduleValueToDays(medicine.scheduleValue, medicine.frequencyType)
                val intervalString =
                    parseScheduleValueToInterval(medicine.scheduleValue, medicine.frequencyType)

                _uiState.update {
                    it.copy(
                        name = medicine.name,
                        dosage = medicine.dosage,
                        medicineType = medicine.type,
                        quantity = medicine.quantity.toString(),
                        enableReminder = medicine.isActive,


                        startDate = loadedStartDate,
                        frequencyType = medicine.frequencyType,
                        selectedDays = daysOfWeekSet,
                        intervalDays = intervalString,
                        specificTimes = loadedSpecificTimes,

                        uiMessage = if (medicine.isActive) "Đang chỉnh sửa: Vui lòng xác nhận lại giờ nhắc nhở." else null,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun parseScheduleValueToDays(value: String?, type: Frequency): Set<DayOfWeek> {
        return if (type == Frequency.SPECIFIC_DAYS && value != null) {
            value.split(",").mapNotNull {
                try {
                    DayOfWeek.valueOf(it.trim())
                } catch (e: Exception) {
                    null
                }
            }.toSet()
        } else emptySet()
    }

    private fun parseScheduleValueToInterval(value: String?, type: Frequency): String {
        return if (type == Frequency.INTERVAL && value != null && value.toIntOrNull() != null) {
            value
        } else "1"
    }



    fun saveMedicine(onSuccess: () -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(uiMessage = null) }


        if (state.name.isBlank() || state.medicineType == "Chọn dạng thuốc" || state.dosage.isBlank() || state.quantity.isBlank()) {
            _uiState.update { it.copy(uiMessage = "Vui lòng nhập đủ thông tin!") }
            return
        }
        if (state.enableReminder) {
            if (state.specificTimes.isEmpty()) {
                _uiState.update { it.copy(uiMessage = "Vui lòng thêm giờ nhắc nhở cụ thể.") }
                return
            }
            if (state.frequencyType == Frequency.SPECIFIC_DAYS && state.selectedDays.isEmpty()) {
                _uiState.update { it.copy(uiMessage = "Vui lòng chọn ít nhất một ngày trong tuần.") }
                return
            }
            if (state.frequencyType == Frequency.INTERVAL && (state.intervalDays.toIntOrNull()
                    ?: 0) <= 0
            ) {
                _uiState.update { it.copy(uiMessage = "Khoảng cách ngày phải lớn hơn 0.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val finalMedicineId = state.medicineId ?: UUID.randomUUID().toString()

                val scheduleValueString = when (state.frequencyType) {
                    Frequency.SPECIFIC_DAYS -> state.selectedDays.map { it.toString() }
                        .joinToString(",")

                    Frequency.INTERVAL -> state.intervalDays
                    else -> null
                }


                val startDateLong = state.startDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()


                val medicine = Medicine(
                    medicineId = finalMedicineId,
                    name = state.name,
                    dosage = state.dosage,
                    type = state.medicineType,
                    quantity = state.quantity.toIntOrNull() ?: 0,
                    frequencyType = state.frequencyType,
                    scheduleValue = scheduleValueString,
                    startDateTimestamp = startDateLong,
                    notes = "",
                    isActive = state.enableReminder
                )


                val schedulesToSave = mutableListOf<Schedule>()
                if (state.enableReminder) {
                    state.specificTimes.forEach { time ->

                        val scheduleDateTime = state.startDate.atTime(time)
                        val timestamp = scheduleDateTime.atZone(ZoneId.systemDefault()).toInstant()
                            .toEpochMilli()


                        val timeString = time.format(timeFormatter)

                        schedulesToSave.add(
                            Schedule(
                                scheduleId = UUID.randomUUID().toString(),
                                medicineId = finalMedicineId,
                                specificTimeStr = timeString,
                                nextScheduledTimestamp = timestamp,
                                reminderStatus = false,
                            )
                        )
                    }

                    val nextScheduleDateTime = findNextScheduleTime(
                        specificTimes = state.specificTimes,
                        startDate = state.startDate,
                        frequency = state.frequencyType,
                        interval = (state.intervalDays.toIntOrNull() ?: 1),
                        selectedDays = state.selectedDays
                    )

                    if (state.medicineId != null) cancelExistingWork(state.medicineId)

                    scheduleNotification(
                        medId = finalMedicineId,
                        name = state.name,
                        dose = state.dosage,
                        targetDateTime = nextScheduleDateTime
                    )
                } else if (state.medicineId != null) {
                    cancelExistingWork(state.medicineId)
                }

                if (state.medicineId != null) {
                    repository.updateMedicine(medicine, schedulesToSave)
                } else {
                    repository.addMedicine(medicine, schedulesToSave)
                }

                _uiState.update { it.copy(uiMessage = "Lưu thành công!", isLoading = false) }
                onSuccess()

            } catch (e: Exception) {
                _uiState.update { it.copy(uiMessage = "Lỗi: ${e.message}", isLoading = false) }
                Log.e("AddMedicineVM", "Lỗi khi lưu thuốc: ${e.message}", e)
            }
        }
    }

    private fun findNextScheduleTime(
        specificTimes: Set<LocalTime>,
        startDate: LocalDate,
        frequency: Frequency,
        interval: Int,
        selectedDays: Set<DayOfWeek>
    ): LocalDateTime {
        if (specificTimes.isEmpty()) return LocalDateTime.MAX

        val now = LocalDateTime.now()
        val today = LocalDate.now()
        val sortedTimes = specificTimes.sorted()

        val baseDate = if (startDate.isAfter(today)) startDate else today

        var isBaseDateValid = true

        if (frequency == Frequency.SPECIFIC_DAYS) {
            isBaseDateValid = selectedDays.contains(baseDate.dayOfWeek)
        }


        if (isBaseDateValid) {

            if (baseDate == today) {
                for (time in sortedTimes) {
                    val potentialTime = baseDate.atTime(time)
                    if (potentialTime.isAfter(now)) {
                        return potentialTime
                    }
                }
            } else {

                return baseDate.atTime(sortedTimes.first())
            }
        }

        val nextDate = when (frequency) {
            Frequency.DAILY -> baseDate.plusDays(1)
            Frequency.INTERVAL -> baseDate.plusDays(interval.toLong())
            Frequency.SPECIFIC_DAYS -> {
                var d = baseDate.plusDays(1)
                while (!selectedDays.contains(d.dayOfWeek)) {
                    d = d.plusDays(1)
                    if (Duration.between(baseDate.atStartOfDay(), d.atStartOfDay())
                            .toDays() > 365
                    ) break
                }
                d
            }
        }

        return nextDate.atTime(sortedTimes.first())
    }


    private fun scheduleNotification(
        medId: String,
        name: String,
        dose: String,
        targetDateTime: LocalDateTime
    ) {
        val now = LocalDateTime.now()

        val delay = Duration.between(now, targetDateTime).toMillis()

        if (delay < 0) {
            Log.w("AddMedicineVM", "Target time is in the past: $targetDateTime")
            return
        }

        val data = Data.Builder()
            .putString("MEDICINE_ID", medId)
            .putString("MEDICINE_NAME", name)
            .putString("MEDICINE_DOSAGE", dose)
            .putString("SCHEDULE_TIME", targetDateTime.format(timeFormatter))
            .build()

        val workRequest = OneTimeWorkRequestBuilder<MedicineReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("med_id_$medId")
            .build()

        workManager.enqueue(workRequest)
    }

    private fun cancelExistingWork(medicineId: String) {
        workManager.cancelAllWorkByTag("med_id_$medicineId")
    }
}