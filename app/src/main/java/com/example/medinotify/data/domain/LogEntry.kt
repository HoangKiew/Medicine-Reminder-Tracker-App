package com.example.medinotify.data.domain

import java.util.Date
import java.util.UUID

/**
 * Domain Model cho Lịch sử uống thuốc (Log Entry).
 * Đại diện cho một lần uống thuốc đã được ghi lại.
 */
data class LogEntry(
    // ✅ SỬA 1: Đổi tên 'id' thành 'logId' cho nhất quán
    val logId: String = UUID.randomUUID().toString(),

    // ID của loại thuốc đã uống
    val medicineId: String,

    // ✅ SỬA 2: Đơn giản hóa thành một trường thời gian duy nhất
    // Thời gian thực tế mà người dùng ghi lại việc uống thuốc
    val intakeTime: Long,

    // Trạng thái của lần uống thuốc này: "TAKEN", "SKIPPED", v.v.
    val status: String
)
