package com.example.medinotify.ui.screens.addmedicine

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.medinotify.data.domain.Medicine
import com.example.medinotify.data.domain.Schedule
import com.example.medinotify.data.repository.MedicineRepository
import com.example.medinotify.worker.MedicineReminderWorker
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate // ✅ Dùng LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.TimeUnit

class AddMedicineViewModel(
    private val repository: MedicineRepository,
    private val workManager: WorkManager
) : ViewModel() {

    // --- State UI ---
    var name by mutableStateOf("")
        private set
    var medicineType by mutableStateOf("Chọn dạng thuốc")
        private set
    var dosage by mutableStateOf("")
        private set
    var quantity by mutableStateOf("")
        private set
    val specificTimes = mutableStateListOf<LocalTime>()
    var enableReminder by mutableStateOf(false)
        private set
    var uiMessage by mutableStateOf<String?>(null)
        private set

    // ✨✨✨ THÊM MỚI: State cho ngày bắt đầu và kết thúc ✨✨✨
    var startDate by mutableStateOf(LocalDate.now())
        private set
    var endDate by mutableStateOf(LocalDate.now()) // Mặc định là hôm nay (1 ngày)
        private set

    // --- Helper UI ---
    fun onNameChange(newName: String) { name = newName }
    fun onTypeChange(newType: String) { medicineType = newType }
    fun onDosageChange(newDosage: String) { dosage = newDosage }
    fun onQuantityChange(newQuantity: String) { if (newQuantity.all { it.isDigit() } || newQuantity.isEmpty()) quantity = newQuantity }
    fun onEnableReminderChange(isEnabled: Boolean) { enableReminder = isEnabled }

    // ✨ Các hàm cập nhật ngày
    fun onStartDateChange(date: LocalDate) {
        startDate = date
        // Nếu ngày bắt đầu lớn hơn ngày kết thúc, tự động đẩy ngày kết thúc lên
        if (startDate.isAfter(endDate)) {
            endDate = startDate
        }
    }
    fun onEndDateChange(date: LocalDate) {
        if (!date.isBefore(startDate)) { // Không cho chọn ngày kết thúc trước ngày bắt đầu
            endDate = date
        } else {
            uiMessage = "Ngày kết thúc không thể trước ngày bắt đầu"
        }
    }

    fun addSpecificTime(time: LocalTime) { if (!specificTimes.contains(time)) { specificTimes.add(time); specificTimes.sortBy { it } } else { uiMessage = "Giờ nhắc nhở này đã tồn tại!" } }
    fun removeSpecificTime(time: LocalTime) { specificTimes.remove(time) }
    fun clearUiMessage() { uiMessage = null }

    // --- HÀM LƯU CHÍNH ---
    fun saveMedicine() {
        uiMessage = null
        if (name.isBlank() || medicineType == "Chọn dạng thuốc" || dosage.isBlank() || quantity.isBlank()) {
            uiMessage = "Vui lòng nhập đủ thông tin!"
            return
        }
        if (enableReminder && specificTimes.isEmpty()) {
            uiMessage = "Vui lòng thêm giờ nhắc nhở cụ thể."
            return
        }

        viewModelScope.launch {
            try {
                val newMedicineId = UUID.randomUUID().toString()
                val newMedicine = Medicine(
                    medicineId = newMedicineId,
                    name = name,
                    dosage = dosage,
                    type = medicineType,
                    quantity = quantity.toIntOrNull() ?: 0,
                    notes = "",
                    isActive = enableReminder
                )

                val schedulesToSave = mutableListOf<Schedule>()

                if (enableReminder) {
                    // ✨✨✨ SỬA LOGIC LƯU: Vòng lặp qua từng ngày ✨✨✨
                    var currentDate = startDate

                    // Chạy vòng lặp từ ngày bắt đầu đến ngày kết thúc
                    while (!currentDate.isAfter(endDate)) {

                        // Với mỗi ngày, tạo lịch cho tất cả các giờ đã chọn
                        specificTimes.forEach { time ->
                            val scheduleDateTime = currentDate.atTime(time)
                            val timestamp = scheduleDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                            schedulesToSave.add(Schedule(
                                scheduleId = UUID.randomUUID().toString(), // Tạo ID riêng cho mỗi lịch
                                medicineId = newMedicineId,
                                specificTime = time,
                                nextScheduledTimestamp = timestamp,
                                reminderStatus = false,
                                // userId sẽ được repository tự thêm vào
                            ))
                        }

                        // Tăng thêm 1 ngày
                        currentDate = currentDate.plusDays(1)
                    }

                    // Hẹn giờ (WorkManager) - Chỉ cần hẹn cho lần gần nhất sắp tới
                    if (specificTimes.isNotEmpty()) {
                        scheduleNotification(newMedicineId, name, dosage, specificTimes[0])
                    }
                }

                repository.addMedicine(newMedicine, schedulesToSave)
                uiMessage = "Thêm thuốc ${name} thành công!"
                Log.d("AddMedicineVM", "Đã lưu thuốc vào DB thành công. Tổng số lịch: ${schedulesToSave.size}")

            } catch (e: Exception) {
                uiMessage = "Lỗi: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    private fun scheduleNotification(medId: String, name: String, dose: String, time: LocalTime) {
        // ... (Giữ nguyên logic hẹn giờ của bạn)
        val now = LocalDateTime.now()
        var targetTime = LocalDate.now().atTime(time)

        if (targetTime.isBefore(now)) {
            targetTime = targetTime.plusDays(1)
        }

        val delay = Duration.between(now, targetTime).toMillis()

        val data = Data.Builder()
            .putString("MEDICINE_ID", medId)
            .putString("MEDICINE_NAME", name)
            .putString("MEDICINE_DOSAGE", dose)
            .putString("SCHEDULE_TIME", time.toString())
            .build()

        val workRequest = OneTimeWorkRequestBuilder<MedicineReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("medicine_reminder")
            .build()

        workManager.enqueue(workRequest)
    }
}