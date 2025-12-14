package com.example.medinotify.data.domain

import java.time.LocalTime
import java.util.UUID
import kotlin.jvm.JvmOverloads
import java.time.format.DateTimeFormatter
import com.google.firebase.firestore.PropertyName // ✅ Cần import PropertyName

/**
 * Domain Model cho đối tượng Lịch trình (Schedule).
 */
data class Schedule @JvmOverloads constructor(
    val scheduleId: String = UUID.randomUUID().toString(),

    var medicineId: String = "",

    var specificTimeStr: String = LocalTime.MIN.format(DateTimeFormatter.ofPattern("HH:mm")),

    var nextScheduledTimestamp: Long = 0L,

    var reminderStatus: Boolean = true
)