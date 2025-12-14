package com.example.medinotify.ui.screens.reminder

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.repository.MedicineRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

class MedicineReminderViewModel(
    private val repository: MedicineRepository
) : ViewModel() {

    // Định nghĩa formatter an toàn
    private val timeFormatter = DateTimeFormatter.ofPattern("H:mm").withResolverStyle(ResolverStyle.STRICT)

    fun markAsTaken(medicineId: String, timeString: String, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                val time = try {
                    // ✅ FIX: Sử dụng formatter an toàn để parse chuỗi (ví dụ: "9:00" hoặc "09:00")
                    LocalTime.parse(timeString, timeFormatter)
                } catch (e: DateTimeParseException) {
                    Log.e("ReminderVM", "Lỗi phân tích cú pháp giờ: $timeString", e)
                    // Thử parse mặc định nếu formatter tùy chỉnh thất bại (ví dụ: WorkManager có thể truyền định dạng khác)
                    try {
                        LocalTime.parse(timeString)
                    } catch (e2: DateTimeParseException) {
                        e2.printStackTrace()
                        null
                    }
                }

                if (time != null) {
                    // 1. Lưu vào DB (Repository sẽ chịu trách nhiệm chuyển LocalTime này thành String HH:mm)
                    repository.updateScheduleStatus(medicineId, time, true) // status: Đã uống (true)
                    Log.d("ReminderVM", "Đã cập nhật trạng thái đã uống cho $timeString")

                    // 2. Chờ một chút để DB/Flow kịp commit dữ liệu
                    delay(300)
                }
            } catch (e: Exception) {
                Log.e("ReminderVM", "Lỗi khi đánh dấu đã uống: ${e.message}", e)
            } finally {
                // 3. Gọi hàm callback để báo cho UI biết là đã xong
                onDone()
            }
        }
    }
}