package com.example.medinotify.data.domain

import java.util.Date
import java.util.UUID
import kotlin.jvm.JvmOverloads // ✅ Cần import JvmOverloads

/**
 * Domain Model cho Lịch sử uống thuốc (Log Entry).
 * Đại diện cho một lần uống thuốc đã được ghi lại.
 */
data class LogEntry @JvmOverloads constructor(
    var logId: String = UUID.randomUUID().toString(),

    var medicineId: String = "",

    var intakeTimestamp: Long = 0L,

    var doseTaken: String = "",

    var status: String = ""
)