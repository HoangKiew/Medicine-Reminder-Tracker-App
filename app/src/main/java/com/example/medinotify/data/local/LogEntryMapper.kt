package com.example.medinotify.data.local

import com.example.medinotify.data.domain.LogEntry
import com.example.medinotify.data.model.LogEntryEntity


/**
 * Ánh xạ từ LogEntryEntity (lớp cho Room Database)
 * sang LogEntry (lớp Domain cho logic và UI).
 */
fun LogEntryEntity.toDomainModel(): LogEntry {
    return LogEntry(
        logId = this.logId,
        medicineId = this.medicineId,
        intakeTimestamp = this.intakeTime,
        status = this.status
    )
}

/**
 * Ánh xạ từ LogEntry (lớp Domain)
 * sang LogEntryEntity (lớp cho Room Database) để lưu trữ.
 */
fun LogEntry.toEntity(userId: String, medicineName: String): LogEntryEntity {
    return LogEntryEntity(
        logId = this.logId,
        userId = userId,
        medicineId = this.medicineId,
        medicineName = medicineName,
        intakeTime = this.intakeTimestamp,
        status = this.status
    )
}