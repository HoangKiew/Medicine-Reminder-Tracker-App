package com.example.medinotify.ui.screens.addmedicine

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle // ✅ Import quan trọng để nhận tham số Navigation
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.TimeUnit

class AddMedicineViewModel(
    private val repository: MedicineRepository,
    private val workManager: WorkManager,
    private val savedStateHandle: SavedStateHandle // ✅ Nhận SavedStateHandle từ Koin/Navigation
) : ViewModel() {

    // ✅ Lấy ID thuốc từ Navigation (Nếu có -> Chế độ Sửa, Nếu null -> Chế độ Thêm)
    private val medicineId: String? = savedStateHandle["medicineId"]

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

    // State cho ngày bắt đầu và kết thúc
    var startDate by mutableStateOf(LocalDate.now())
        private set
    var endDate by mutableStateOf(LocalDate.now())
        private set

    // ✨✨✨ INIT BLOCK: Kiểm tra xem có đang sửa thuốc không ✨✨✨
    init {
        if (medicineId != null) {
            loadMedicineData(medicineId)
        }
    }

    // --- Helper UI ---
    fun onNameChange(newName: String) { name = newName }
    fun onTypeChange(newType: String) { medicineType = newType }
    fun onDosageChange(newDosage: String) { dosage = newDosage }
    fun onQuantityChange(newQuantity: String) { if (newQuantity.all { it.isDigit() } || newQuantity.isEmpty()) quantity = newQuantity }
    fun onEnableReminderChange(isEnabled: Boolean) { enableReminder = isEnabled }

    fun onStartDateChange(date: LocalDate) {
        startDate = date
        if (startDate.isAfter(endDate)) {
            endDate = startDate
        }
    }
    fun onEndDateChange(date: LocalDate) {
        if (!date.isBefore(startDate)) {
            endDate = date
        } else {
            uiMessage = "Ngày kết thúc không thể trước ngày bắt đầu"
        }
    }

    fun addSpecificTime(time: LocalTime) { if (!specificTimes.contains(time)) { specificTimes.add(time); specificTimes.sortBy { it } } else { uiMessage = "Giờ nhắc nhở này đã tồn tại!" } }
    fun removeSpecificTime(time: LocalTime) { specificTimes.remove(time) }
    fun clearUiMessage() { uiMessage = null }

    // ✨✨✨ HÀM TẢI DỮ LIỆU CŨ (KHI SỬA) ✨✨✨
    private fun loadMedicineData(id: String) {
        viewModelScope.launch {
            val medicine = repository.getMedicineById(id)
            if (medicine != null) {
                name = medicine.name
                dosage = medicine.dosage
                medicineType = medicine.type
                quantity = medicine.quantity.toString()
                enableReminder = medicine.isActive

                // Lưu ý: Vì logic Schedule phức tạp (lưu từng ngày), việc load lại chính xác
                // ngày bắt đầu/kết thúc và giờ từ hàng trăm record Schedule là rất khó và tốn kém.
                // UX tốt nhất: Load thông tin cơ bản, còn lịch nhắc nhở người dùng sẽ đặt lại mới.
                if (enableReminder) {
                    uiMessage = "Đang chỉnh sửa: Vui lòng đặt lại lịch nhắc nhở."
                }
            }
        }
    }

    // --- HÀM LƯU CHÍNH (ADD HOẶC UPDATE) ---
    fun saveMedicine(onSuccess: () -> Unit) { // ✅ Thêm callback onSuccess để đóng màn hình
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
                // QUAN TRỌNG: Nếu đang sửa thì dùng ID cũ, nếu thêm mới thì tạo ID mới
                val finalMedicineId = medicineId ?: UUID.randomUUID().toString()

                val medicine = Medicine(
                    medicineId = finalMedicineId,
                    name = name,
                    dosage = dosage,
                    type = medicineType,
                    quantity = quantity.toIntOrNull() ?: 0,
                    notes = "",
                    isActive = enableReminder
                )

                val schedulesToSave = mutableListOf<Schedule>()

                if (enableReminder) {
                    var currentDate = startDate
                    while (!currentDate.isAfter(endDate)) {
                        specificTimes.forEach { time ->
                            val scheduleDateTime = currentDate.atTime(time)
                            val timestamp = scheduleDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                            schedulesToSave.add(Schedule(
                                scheduleId = UUID.randomUUID().toString(),
                                medicineId = finalMedicineId, // Dùng finalMedicineId
                                specificTime = time,
                                nextScheduledTimestamp = timestamp,
                                reminderStatus = false,
                            ))
                        }
                        currentDate = currentDate.plusDays(1)
                    }

                    if (specificTimes.isNotEmpty()) {
                        scheduleNotification(finalMedicineId, name, dosage, specificTimes[0])
                    }
                }

                // ✨✨✨ PHÂN BIỆT ADD VÀ UPDATE ✨✨✨
                if (medicineId != null) {
                    // Chế độ SỬA: Gọi hàm update (đã thêm vào Repo ở bước trước)
                    repository.updateMedicine(medicine, schedulesToSave)
                    Log.d("AddMedicineVM", "Đã CẬP NHẬT thuốc: $name")
                } else {
                    // Chế độ THÊM: Gọi hàm add
                    repository.addMedicine(medicine, schedulesToSave)
                    Log.d("AddMedicineVM", "Đã THÊM MỚI thuốc: $name")
                }

                uiMessage = "Lưu thành công!"
                onSuccess() // Gọi callback để quay về màn hình trước

            } catch (e: Exception) {
                uiMessage = "Lỗi: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    private fun scheduleNotification(medId: String, name: String, dose: String, time: LocalTime) {
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