package com.example.medinotify.data.local

import com.example.medinotify.data.domain.Schedule
import com.example.medinotify.data.model.ScheduleEntity
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException // ✅ Import cần thiết cho parsing

/**
 * Ánh xạ từ ScheduleEntity (dữ liệu thô từ Room)
 * sang Schedule (lớp Domain cho logic và UI).
 */
fun ScheduleEntity.toDomainModel(): Schedule {



    return Schedule(
        scheduleId = this.scheduleId,
        medicineId = this.medicineId,

        // ✅ FIX: SỬ DỤNG specificTimeStr (Giả định Entity đã đổi tên)
        specificTimeStr = this.specificTimeStr,

        nextScheduledTimestamp = this.nextScheduledTimestamp,
        reminderStatus = this.reminderStatus
    )
}

/**
 * Ánh xạ một đối tượng Schedule (lớp domain)
 * sang một đối tượng ScheduleEntity để lưu vào cơ sở dữ liệu.
 */
fun Schedule.toEntity(userId: String): ScheduleEntity {
    return ScheduleEntity(
        scheduleId = this.scheduleId,
        userId = userId,
        medicineId = this.medicineId,

        specificTimeStr = this.specificTimeStr,

        nextScheduledTimestamp = this.nextScheduledTimestamp,
        reminderStatus = this.reminderStatus
    )
}