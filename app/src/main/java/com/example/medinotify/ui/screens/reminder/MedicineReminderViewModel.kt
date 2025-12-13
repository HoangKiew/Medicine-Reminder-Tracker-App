package com.example.medinotify.ui.screens.reminder

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.repository.MedicineRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeParseException

class MedicineReminderViewModel(
    private val repository: MedicineRepository
) : ViewModel() {

    // ✨ CẬP NHẬT: Thêm tham số onDone
    fun markAsTaken(medicineId: String, timeString: String, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                val time = try {
                    LocalTime.parse(timeString)
                } catch (e: DateTimeParseException) {
                    e.printStackTrace()
                    null
                }

                if (time != null) {
                    // 1. Lưu vào DB
                    repository.updateScheduleStatus(medicineId, time, true)
                    Log.d("ReminderVM", "Đã cập nhật trạng thái đã uống")

                    // 2. ✨ QUAN TRỌNG: Chờ một chút để DB kịp commit dữ liệu (Fix lỗi chạy đua)
                    delay(300)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // 3. Gọi hàm callback để báo cho UI biết là đã xong
                onDone()
            }
        }
    }
}