package com.example.medinotify.data.domain

import java.time.LocalTime
import java.util.UUID

/**
 * Domain Model cho đối tượng Lịch trình (Schedule).
 * Chỉ chứa các thông tin CỐT LÕI của một lịch trình.
 * Phiên bản này đã được hoàn thiện để tương thích với AddMedicineViewModel.
 */
data class Schedule(
    val scheduleId: String = UUID.randomUUID().toString(),

    // ID của thuốc mà lịch trình này áp dụng đến
    val medicineId: String,

    // Sử dụng kiểu dữ liệu LocalTime thay vì String để xử lý thời gian chính xác hơn.
    val specificTime: LocalTime,

    // ✅ ĐÃ THÊM GIÁ TRỊ MẶC ĐỊNH
    // Điều này giải quyết lỗi "No value passed for parameter" trong ViewModel.
    // Timestamp thực tế sẽ được tính toán bởi một logic nền.
    val nextScheduledTimestamp: Long = 0,

    // Trạng thái bật/tắt của riêng lịch trình này (đã có giá trị mặc định, rất tốt).
    val reminderStatus: Boolean = true
)
