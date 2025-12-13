package com.example.medinotify.ui.screens.addmedicine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.domain.Medicine
import com.example.medinotify.data.domain.Schedule
import com.example.medinotify.data.model.Frequency
import com.example.medinotify.data.model.WeekDay
import com.example.medinotify.data.repository.MedicineRepository
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters // ✅ THÊM IMPORT NÀY cho logic Ngày trong tuần
import java.util.UUID

class AddMedicineViewModel(
    private val repository: MedicineRepository
) : ViewModel() {

    // --- State: Dữ liệu đầu vào của người dùng ---

    var name by mutableStateOf("")
        private set
    var medicineType by mutableStateOf("Chọn dạng thuốc")
        private set
    var dosage by mutableStateOf("")
        private set
    var quantity by mutableStateOf("")
        private set
    var enableReminder by mutableStateOf(true) // Mặc định bật nhắc nhở
        private set
    val specificTimes = mutableStateListOf<LocalTime>()
    var uiMessage by mutableStateOf<String?>(null)
        private set

    // --- State cho Tần suất ---
    var frequency by mutableStateOf(Frequency.DAILY)
        private set
    var selectedDays by mutableStateOf(setOf<WeekDay>())
        private set
    var intervalDays by mutableStateOf("2") // Mặc định là cách 2 ngày uống 1 lần
        private set

    // --- Hàm xử lý sự kiện (Giữ nguyên) ---

    fun onNameChange(newName: String) { name = newName }
    fun onTypeChange(newType: String) { medicineType = newType }
    fun onDosageChange(newDosage: String) { dosage = newDosage }
    fun onQuantityChange(newQuantity: String) {
        if (newQuantity.all { it.isDigit() } || newQuantity.isEmpty()) {
            quantity = newQuantity
        }
    }
    fun onEnableReminderChange(isEnabled: Boolean) { enableReminder = isEnabled }
    fun clearUiMessage() { uiMessage = null }

    fun addSpecificTime(time: LocalTime) {
        if (!specificTimes.contains(time)) {
            specificTimes.add(time)
            specificTimes.sortBy { it } // Sắp xếp lại danh sách giờ
        } else {
            uiMessage = "Giờ nhắc nhở này đã tồn tại!"
        }
    }

    fun removeSpecificTime(time: LocalTime) {
        specificTimes.remove(time)
    }

    fun onFrequencyChange(newFrequency: Frequency) {
        frequency = newFrequency
    }

    fun toggleDaySelection(day: WeekDay) {
        selectedDays = if (selectedDays.contains(day)) {
            selectedDays - day
        } else {
            selectedDays + day
        }
    }

    fun onIntervalChange(newInterval: String) {
        if (newInterval.all { it.isDigit() }) {
            intervalDays = newInterval
        }
    }

    // =========================================================================
    // HÀM HỖ TRỢ: TÍNH TOÁN THỜI GIAN NHẮC NHỞ TIẾP THEO
    // (CHỈ DÙNG CHO LẦN ĐẦU TIÊN CỦA SCHEDULE/DÙNG CHO WORKER LẶP LẠI)
    // =========================================================================

    /**
     * Tính toán thời gian (timestamp) nhắc nhở tiếp theo chính xác.
     * Logic này sử dụng Múi giờ hệ thống để đảm bảo tính nhất quán với Repository.
     * @param time Giờ uống thuốc (LocalTime).
     * @return nextScheduledTimestamp (Long - Epoch Millis)
     */
    private fun calculateNextScheduledTimestamp(time: LocalTime): Long {
        val zoneId = ZoneId.systemDefault()
        val nowTime = LocalTime.now(zoneId)
        var dateToSchedule = LocalDate.now(zoneId)

        // 1. Kiểm tra nếu giờ nhắc nhở đã qua trong ngày hôm nay
        if (time.isBefore(nowTime)) {
            dateToSchedule = dateToSchedule.plusDays(1)
        }

        // 2. Kết hợp ngày và giờ để tạo ra Timestamp (Epoch Millis)
        return dateToSchedule.atTime(time)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    // =========================================================================
    // HÀM MỚI: TẠO CÁC BẢN GHI LỊCH TRÌNH LẶP LẠI (GIẢI QUYẾT LỖI)
    // =========================================================================

    /**
     * Tạo ra danh sách các Schedule (lịch trình lặp lại) trong một khoảng thời gian dài
     * dựa trên tần suất và số lượng (180 ngày hoặc đến khi hết thuốc).
     */
    private fun generateRecurringSchedules(medicine: Medicine): List<Schedule> {
        if (!medicine.isActive || medicine.quantity <= 0) return emptyList()

        val schedules = mutableListOf<Schedule>()
        val zoneId = ZoneId.systemDefault()
        val maxDaysToGenerate = 180 // Tạo lịch trong 6 tháng
        val sortedTimes = specificTimes.sorted()

        var generatedCount = 0

        // Loop chính: Kiểm tra từng ngày trong 180 ngày tới
        for (i in 0 until maxDaysToGenerate) {
            val dateToCheck = LocalDate.now(zoneId).plusDays(i.toLong())
            var isIntakeDay = false

            when (medicine.frequency) {
                Frequency.DAILY -> {
                    isIntakeDay = true
                }

                Frequency.SPECIFIC_DAYS -> {
                    // ✅ GIẢI QUYẾT LỖI LỆCH NGÀY (THỨ 2): Kiểm tra DayOfWeek của ngày đó
                    val currentDayOfWeek = dateToCheck.dayOfWeek
                    isIntakeDay = medicine.daysOfWeek.contains(currentDayOfWeek)
                }

                Frequency.INTERVAL -> {
                    // Logic Cách ngày: Kiểm tra xem số ngày đã trôi qua có chia hết cho khoảng cách không
                    val interval = medicine.intervalDays
                    isIntakeDay = i % interval == 0
                }
            }

            if (isIntakeDay) {
                // Nếu là ngày uống thuốc, tạo 1 Schedule cho MỖI giờ đã chọn
                sortedTimes.forEach { time ->
                    // ✅ GIẢI QUYẾT LỖI GIỚI HẠN: Dừng khi đã đạt số lượng tối đa
                    if (generatedCount >= medicine.quantity) return schedules

                    val scheduledDateTime = dateToCheck.atTime(time)
                    val timestamp = scheduledDateTime.atZone(zoneId).toInstant().toEpochMilli()

                    schedules.add(
                        Schedule(
                            scheduleId = UUID.randomUUID().toString(),
                            medicineId = medicine.medicineId,
                            specificTime = time,
                            nextScheduledTimestamp = timestamp,
                            reminderStatus = true
                        )
                    )
                    generatedCount++
                }
            }
        }
        return schedules
    }

    // =========================================================================
    // HÀM CHÍNH: saveMedicine() ĐÃ ĐƯỢC HOÀN THIỆN
    // =========================================================================

    /**
     * Hàm chính để tạo đối tượng Medicine, Schedule và lưu vào Repository.
     */
    fun saveMedicine() {
        // 1. Kiểm tra dữ liệu bắt buộc
        if (name.isBlank() || medicineType == "Chọn dạng thuốc" || dosage.isBlank() || quantity.isBlank()) {
            uiMessage = "Vui lòng nhập đủ thông tin có dấu (*)."
            return
        }
        if (enableReminder) {
            if (specificTimes.isEmpty()) {
                uiMessage = "Vui lòng thêm ít nhất một giờ nhắc nhở."
                return
            }
            when (frequency) {
                Frequency.SPECIFIC_DAYS -> if (selectedDays.isEmpty()) {
                    uiMessage = "Vui lòng chọn ít nhất một ngày trong tuần."
                    return
                }
                Frequency.INTERVAL -> if ((intervalDays.toIntOrNull() ?: 0) < 1) {
                    uiMessage = "Khoảng cách ngày phải lớn hơn hoặc bằng 1."
                    return
                }
                else -> { /* Daily */ }
            }
        }

        viewModelScope.launch {
            try {
                val newMedicineId = UUID.randomUUID().toString()

                // 2. Tạo đối tượng Medicine
                val newMedicine = Medicine(
                    medicineId = newMedicineId,
                    name = name,
                    dosage = dosage,
                    type = medicineType,
                    quantity = quantity.toIntOrNull() ?: 0,
                    notes = "",
                    isActive = enableReminder,
                    frequency = frequency,
                    intervalDays = if (frequency == Frequency.INTERVAL) intervalDays.toIntOrNull() ?: 1 else 0,
                    // Map WeekDay sang Java DayOfWeek để lưu vào Entity
                    daysOfWeek = if (frequency == Frequency.SPECIFIC_DAYS) selectedDays.map { it.javaDayOfWeek }.toSet() else emptySet()
                )

                // 3. ✅ SỬ DỤNG HÀM MỚI: Tạo danh sách Schedule lặp lại
                val schedulesToSave = if (enableReminder) {
                    generateRecurringSchedules(newMedicine)
                } else {
                    emptyList()
                }

                if (schedulesToSave.isEmpty() && enableReminder) {
                    uiMessage = "Lỗi: Không thể tạo lịch trình lặp lại. Kiểm tra số lượng/tần suất."
                    return@launch
                }

                // 4. Gọi Repository để lưu cả Medicine và Schedules
                repository.addMedicineAndSchedules(newMedicine, schedulesToSave)

                uiMessage = "Thêm thuốc ${name} thành công!"

            } catch (e: Exception) {
                uiMessage = "Lỗi: Không thể lưu thuốc. ${e.message}"
            }
        }
    }
}