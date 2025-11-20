package com.example.medinotify.data.api

data class ScheduleItem(
    val scheduleId: String?,
    val medicineId: String?,
    val startDate: String?,
    val specificTime: String?,
    val frequency: String?,
    val reminderStatus: Int?,
    val dayOfWeek: Int?
)

data class ScheduleResponse(
    val schedule: List<ScheduleItem>
)
