package com.example.medinotify.data.domain

import java.util.Date
import java.util.UUID
import kotlin.jvm.JvmOverloads // ✅ Cần import JvmOverloads

/**
 * Domain Model cho Lịch sử uống thuốc (Log Entry).
 * Đại diện cho một lần uống thuốc đã được ghi lại.
 */
data class LogEntry @JvmOverloads constructor( // ✅ Thêm @JvmOverloads
    // ✅ SỬA 1: Đổi tên 'id' thành 'logId' cho nhất quán
    var logId: String = UUID.randomUUID().toString(), // ✅ Chuyển sang var, thêm giá trị mặc định

    // ✅ Thêm var, giá trị mặc định
    var medicineId: String = "",

    // ✅ Đổi tên thành Timestamp, thêm var và giá trị mặc định
    var intakeTimestamp: Long = 0L,

    // ✅ Thêm trường doseTaken bị thiếu trong định nghĩa bạn cung cấp
    var doseTaken: String = "",

    // Trạng thái của lần uống thuốc này: "TAKEN", "SKIPPED", v.v.
    var status: String = "" // ✅ Thêm var, giá trị mặc định
)