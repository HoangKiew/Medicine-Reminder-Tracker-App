package com.example.medinotify.data.local

import com.example.medinotify.data.domain.Schedule
import com.example.medinotify.data.model.ScheduleEntity
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Ánh xạ từ ScheduleEntity (dữ liệu thô từ Room, dùng String)
 * sang Schedule (lớp Domain cho logic và UI, dùng LocalTime).
 */
// Lớp domain Schedule không còn chứa medicineName và medicineDosage.
fun ScheduleEntity.toDomainModel(): Schedule {
    return Schedule(
        scheduleId = this.scheduleId,
        medicineId = this.medicineId,

        specificTime = LocalTime.parse(this.specificTime, DateTimeFormatter.ofPattern("HH:mm")),

        nextScheduledTimestamp = this.nextScheduledTimestamp,
        reminderStatus = this.reminderStatus
    )
}

/**
 * Ánh xạ một đối tượng Schedule (lớp domain, dùng LocalTime)
 * sang một đối tượng ScheduleEntity để lưu vào cơ sở dữ liệu (dùng String).
 */
fun Schedule.toEntity(userId: String): ScheduleEntity {
    return ScheduleEntity(
        scheduleId = this.scheduleId,
        userId = userId, // Giữ nguyên việc truyền userId
        medicineId = this.medicineId,

        specificTime = this.specificTime.format(DateTimeFormatter.ofPattern("HH:mm")),

        nextScheduledTimestamp = this.nextScheduledTimestamp,
        reminderStatus = this.reminderStatus
    )
}
