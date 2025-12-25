package com.example.medinotify.data.domain

import com.example.medinotify.data.model.Frequency
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlin.jvm.JvmOverloads

data class Medicine @JvmOverloads constructor(

    val medicineId: String = UUID.randomUUID().toString(),


    var name: String = "",
    var dosage: String = "",
    var type: String = "",
    var quantity: Int = 0,

    var frequencyType: Frequency = Frequency.DAILY,
    var scheduleValue: String? = null,


    var startDateTimestamp: Long = LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli(),

    var notes: String = "",
    var isActive: Boolean = false
)