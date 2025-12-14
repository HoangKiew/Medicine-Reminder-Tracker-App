package com.example.medinotify.data.domain

import com.example.medinotify.data.model.Frequency
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlin.jvm.JvmOverloads

data class Medicine @JvmOverloads constructor(
    // ID nên là val, nhưng nếu bạn cần reset nó (không nên), hãy dùng var.
    // Tạm thời giữ val, nhưng cần có constructor không tham số cho Firestore.
    // @JvmOverloads ở đây giúp tạo constructor không tham số.
    val medicineId: String = UUID.randomUUID().toString(),

    // ✅ FIX: Chuyển sang var để Firestore có thể set giá trị
    var name: String = "",
    var dosage: String = "",
    var type: String = "",
    var quantity: Int = 0,

    var frequencyType: Frequency = Frequency.DAILY,
    var scheduleValue: String? = null,

    // ✅ FIX: Chuyển sang var
    var startDateTimestamp: Long = LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli(),

    var notes: String = "",
    var isActive: Boolean = false // ✅ FIX: Chuyển sang var để khắc phục lỗi setter
)