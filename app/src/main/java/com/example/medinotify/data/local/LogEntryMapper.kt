package com.example.medinotify.data.local

import com.example.medinotify.data.domain.LogEntry
import com.example.medinotify.data.model.LogEntryEntity

// GIẢ ĐỊNH: LogEntryEntity sử dụng tên trường là intakeTime
// VÀ LogEntry Domain Model sử dụng tên trường là intakeTimestamp

/**
 * Ánh xạ từ LogEntryEntity (lớp cho Room Database)
 * sang LogEntry (lớp Domain cho logic và UI).
 */
fun LogEntryEntity.toDomainModel(): LogEntry {
    return LogEntry(
        logId = this.logId,
        medicineId = this.medicineId,
        // ✅ SỬA LỖI: Ánh xạ intakeTime (Entity) sang intakeTimestamp (Domain)
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
        // ✅ SỬA LỖI: Ánh xạ intakeTimestamp (Domain) sang intakeTime (Entity)
        intakeTime = this.intakeTimestamp,
        status = this.status
    )
}