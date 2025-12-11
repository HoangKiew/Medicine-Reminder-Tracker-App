package com.example.medinotify.ui.screens.addmedicine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// ✅ SỬA 1: Import các lớp cần thiết
import com.example.medinotify.data.domain.Medicine
import com.example.medinotify.data.domain.Schedule
import com.example.medinotify.data.repository.MedicineRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

class AddMedicineViewModel(
    private val repository: MedicineRepository
) : ViewModel() {

    // --- State: Dữ liệu đầu vào của người dùng (Giữ nguyên) ---

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

    // --- Hàm xử lý sự kiện (Giữ nguyên) ---

    fun onNameChange(newName: String) {
        name = newName
    }

    fun onTypeChange(newType: String) {
        medicineType = newType
    }

    fun onDosageChange(newDosage: String) {
        dosage = newDosage
    }

    fun onQuantityChange(newQuantity: String) {
        if (newQuantity.all { it.isDigit() } || newQuantity.isEmpty()) {
            quantity = newQuantity
        }
    }

    fun onEnableReminderChange(isEnabled: Boolean) {
        enableReminder = isEnabled
    }

    fun addSpecificTime(time: LocalTime) {
        if (!specificTimes.contains(time)) {
            specificTimes.add(time)
            specificTimes.sortBy { it }
        } else {
            uiMessage = "Giờ nhắc nhở này đã tồn tại!"
        }
    }

    fun removeSpecificTime(time: LocalTime) {
        specificTimes.remove(time)
    }

    fun clearUiMessage() {
        uiMessage = null
    }

    /**
     * Hàm chính để tạo đối tượng Medicine, Schedule và lưu vào Repository.
     * ĐÃ ĐƯỢC VIẾT LẠI HOÀN CHỈNH.
     */
    fun saveMedicine() {
        uiMessage = null

        // 1. Kiểm tra dữ liệu bắt buộc
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
                // --- Bắt đầu logic mới ---
                val newMedicineId = UUID.randomUUID().toString()

                // 2. Tạo đối tượng Medicine (chỉ chứa thông tin thuốc)
                val newMedicine = Medicine(
                    medicineId = newMedicineId,
                    name = name,
                    dosage = dosage,
                    type = medicineType,
                    quantity = quantity.toIntOrNull() ?: 0,
                    notes = "", // Ghi chú thêm (hiện để trống)
                    isActive = enableReminder
                )

                // ✅ SỬA 2: Thêm đoạn code bị thiếu để khai báo và tạo `schedulesToSave`
                val schedulesToSave = mutableListOf<Schedule>()
                if (enableReminder) {
                    specificTimes.forEach { time ->
                        // Tính toán timestamp cho lần nhắc nhở tiếp theo
                        val now = LocalDate.now()
                        val scheduleDateTime = now.atTime(time)
                        val timestamp = scheduleDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                        schedulesToSave.add(
                            Schedule(
                                medicineId = newMedicineId, // Liên kết với thuốc vừa tạo
                                specificTime = time,        // Truyền thẳng đối tượng LocalTime
                                nextScheduledTimestamp = timestamp,
                                reminderStatus = true
                                // `scheduleId` được tự tạo trong constructor của Schedule
                            )
                        )
                    }
                }

                // 3. Gọi Repository để lưu cả Medicine và Schedules
                // Lỗi Unresolved reference đã được giải quyết vì `schedulesToSave` đã tồn tại
                repository.addMedicine(newMedicine, schedulesToSave)

                uiMessage = "Thêm thuốc ${name} thành công!"

            } catch (e: IllegalStateException) {
                uiMessage = "Lỗi: Người dùng chưa đăng nhập."
            } catch (e: Exception) {
                uiMessage = "Lỗi hệ thống: Không thể lưu thuốc. ${e.message}"
            }
        }
    }
}
