package com.example.medinotify.data.domain

import java.time.LocalTime // ✅ SỬA 1: Import đúng lớp LocalTime
import java.util.UUID

/**
 * Domain Model cho đối tượng Lịch trình (Schedule).
 * Chỉ chứa các thông tin CỐT LÕI của một lịch trình.
 */
data class Schedule(
    // ✅ SỬA 2: Cung cấp giá trị mặc định để dễ dàng tạo đối tượng mới. [2, 3]
    val scheduleId: String = UUID.randomUUID().toString(),

    // ID của thuốc mà lịch trình này áp dụng đến
    val medicineId: String,

    // ✅ SỬA 3: Loại bỏ các thuộc tính của Medicine.
    // 'medicineName' và 'medicineDosage' không thuộc về trách nhiệm của lớp này.
    // Chúng sẽ được lấy từ Repository khi cần hiển thị lên giao diện.

    // ✅ SỬA 4: Sử dụng kiểu dữ liệu LocalTime thay vì String. [1, 6]
    // Điều này giúp xử lý thời gian chính xác hơn và khớp với ViewModel.
    val specificTime: LocalTime,

    // Timestamp của lần nhắc nhở tiếp theo (không thay đổi)
    val nextScheduledTimestamp: Long,

    // Trạng thái bật/tắt của riêng lịch trình này
    val reminderStatus: Boolean = true
)
