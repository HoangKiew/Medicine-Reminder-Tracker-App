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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
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

    private val _uiState = MutableStateFlow(AddMedicineUiState(
        medicineId = savedStateHandle["medicineId"]
    ))
    val uiState: StateFlow<AddMedicineUiState> = _uiState.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    init {
        uiState.value.medicineId?.let { loadMedicineData(it) }
    }

    // --- Helper Functions (Giữ nguyên) ---
    fun onNameChange(newName: String) { _uiState.update { it.copy(name = newName, uiMessage = null) } }
    fun onTypeChange(newType: String) { _uiState.update { it.copy(medicineType = newType, uiMessage = null) } }
    fun onDosageChange(newDosage: String) { _uiState.update { it.copy(dosage = newDosage, uiMessage = null) } }
    fun onQuantityChange(newQuantity: String) {
        if (newQuantity.all { it.isDigit() } || newQuantity.isEmpty()) {
            _uiState.update { it.copy(quantity = newQuantity, uiMessage = null) }
        }
    }
    fun onEnableReminderChange(isEnabled: Boolean) { _uiState.update { it.copy(enableReminder = isEnabled, uiMessage = null) } }
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
        if (interval.all { it.isDigit() } && interval.isNotEmpty() && (interval.toIntOrNull() ?: 0) > 0) {
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
                    .sorted() // -> List
                    .toSet()    // <- Chuyển List về Set
            )
        }
    }
    fun clearUiMessage() { _uiState.update { it.copy(uiMessage = null) } }


    private fun loadMedicineData(id: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val medicine = repository.getMedicineById(id)

            // Lấy Schedules từ Flow (Sử dụng .first() để lấy giá trị suspend)
            val schedules = repository.getSchedulesForMedicine(id).first()

            if (medicine != null) {
                // 1. Chuyển đổi Long Timestamp thành LocalDate cho UI State
                val loadedStartDate = medicine.startDateTimestamp.let {
                    if (it != 0L) LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it), ZoneId.systemDefault()).toLocalDate() else LocalDate.now()
                }

                // 2. Chuyển đổi Schedule.specificTimeStr (String) thành LocalTime cho UI State
                val loadedSpecificTimes: Set<LocalTime> = schedules.mapNotNull { schedule ->
                    try {
                        LocalTime.parse(schedule.specificTimeStr, timeFormatter)
                    } catch (e: Exception) {
                        Log.e("AddMedicineVM", "Error parsing schedule time: ${schedule.specificTimeStr}", e)
                        null
                    }
                }
                    .toSet()


                val daysOfWeekSet = parseScheduleValueToDays(medicine.scheduleValue, medicine.frequencyType)
                val intervalString = parseScheduleValueToInterval(medicine.scheduleValue, medicine.frequencyType)

                _uiState.update {
                    it.copy(
                        name = medicine.name,
                        dosage = medicine.dosage,
                        medicineType = medicine.type,
                        quantity = medicine.quantity.toString(),
                        enableReminder = medicine.isActive,

                        // LOAD DỮ LIỆU LỊCH TRÌNH
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
                try { DayOfWeek.valueOf(it.trim()) } catch (e: Exception) { null }
            }.toSet()
        } else emptySet()
    }

    private fun parseScheduleValueToInterval(value: String?, type: Frequency): String {
        return if (type == Frequency.INTERVAL && value != null && value.toIntOrNull() != null) {
            value
        } else "1"
    }


    // --- HÀM LƯU CHÍNH (ADD HOẶC UPDATE) ---
    fun saveMedicine(onSuccess: () -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(uiMessage = null) }

        // 1. Validation (Giữ nguyên)
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
            if (state.frequencyType == Frequency.INTERVAL && (state.intervalDays.toIntOrNull() ?: 0) <= 0) {
                _uiState.update { it.copy(uiMessage = "Khoảng cách ngày phải lớn hơn 0.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val finalMedicineId = state.medicineId ?: UUID.randomUUID().toString()

                val scheduleValueString = when (state.frequencyType) {
                    Frequency.SPECIFIC_DAYS -> state.selectedDays.map { it.toString() }.joinToString(",")
                    Frequency.INTERVAL -> state.intervalDays
                    else -> null
                }

                // CHUYỂN LocalDate thành Long Timestamp cho Domain Model
                val startDateLong = state.startDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                // 3. Tạo Medicine Domain Model
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

                // 4. Tạo Schedule records
                val schedulesToSave = mutableListOf<Schedule>()
                if (state.enableReminder) {
                    state.specificTimes.forEach { time ->
                        // Lịch trình trong Room/Firebase cần NextScheduledTimestamp là ngày bắt đầu
                        val scheduleDateTime = state.startDate.atTime(time)
                        val timestamp = scheduleDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                        // CHUYỂN LocalTime thành String (HH:mm) cho Domain Model
                        val timeString = time.format(timeFormatter)

                        schedulesToSave.add(Schedule(
                            scheduleId = UUID.randomUUID().toString(),
                            medicineId = finalMedicineId,
                            specificTimeStr = timeString,
                            nextScheduledTimestamp = timestamp, // Lần đầu tiên: Ngày bắt đầu + Giờ
                            reminderStatus = false, // Reset trạng thái chưa uống
                        ))
                    }

                    // 5. Lập lịch WorkManager: Lập lịch cho LỊCH TRÌNH TIẾP THEO GẦN NHẤT
                    val nextScheduleTime = findNextScheduleTime(state.specificTimes)

                    if (state.medicineId != null) cancelExistingWork(state.medicineId)

                    scheduleNotification(
                        medId = finalMedicineId,
                        name = state.name,
                        dose = state.dosage,
                        time = nextScheduleTime.toLocalTime()
                    )
                } else if (state.medicineId != null) {
                    cancelExistingWork(state.medicineId)
                }

                // 6. Lưu vào Repository
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

    // --- HÀM HELPER MỚI: TÌM LỊCH TRÌNH TIẾP THEO GẦN NHẤT ---
    /**
     * Tìm giờ uống gần nhất (hôm nay hoặc ngày mai)
     */
    private fun findNextScheduleTime(specificTimes: Set<LocalTime>): LocalDateTime {
        if (specificTimes.isEmpty()) return LocalDateTime.MAX

        val now = LocalDateTime.now()

        // Sắp xếp giờ uống để tìm giờ gần nhất trong ngày hôm nay
        val sortedTimes = specificTimes.sorted()

        // 1. Kiểm tra ngày hôm nay
        val today = LocalDate.now()
        for (time in sortedTimes) {
            val potentialTime = today.atTime(time)
            if (potentialTime.isAfter(now)) {
                return potentialTime // Giờ gần nhất trong ngày hôm nay (trong tương lai)
            }
        }

        // 2. Nếu không tìm thấy giờ nào trong ngày hôm nay: Lập lịch cho giờ đầu tiên của ngày mai.
        val tomorrow = today.plusDays(1)
        val firstTimeTomorrow = tomorrow.atTime(sortedTimes.first())

        return firstTimeTomorrow
    }


    // --- HÀM LẬP LỊCH WORKER (Giữ nguyên) ---
    private fun scheduleNotification(medId: String, name: String, dose: String, time: LocalTime) {
        val now = LocalDateTime.now()
        var targetTime = LocalDate.now().atTime(time)

        // Logic này đã được xử lý trong findNextScheduleTime, nhưng giữ lại để an toàn
        if (targetTime.isBefore(now)) {
            targetTime = targetTime.plusDays(1)
        }

        val delay = Duration.between(now, targetTime).toMillis()

        val data = Data.Builder()
            .putString("MEDICINE_ID", medId)
            .putString("MEDICINE_NAME", name)
            .putString("MEDICINE_DOSAGE", dose)
            // ✅ FIX: Đảm bảo SCHEDULE_TIME được truyền dưới dạng chuỗi HH:mm
            .putString("SCHEDULE_TIME", time.format(timeFormatter))
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