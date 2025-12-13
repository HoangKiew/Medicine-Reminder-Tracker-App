package com.example.medinotify.data.local

import com.example.medinotify.data.domain.LogEntry
import com.example.medinotify.data.model.LogEntryEntity // <-- DÒNG NÀY SAI, ĐÃ XÓA

/**
 * Ánh xạ từ LogEntryEntity (lớp cho Room Database)
 * sang LogEntry (lớp Domain cho logic và UI).
 */
fun LogEntryEntity.toDomainModel(): LogEntry {
    // Logic này đã đúng, đảm bảo LogEntryEntity có các thuộc tính này
    return LogEntry(
        logId = this.logId,
        medicineId = this.medicineId,
        intakeTime = this.intakeTime,
        status = this.status
    )
}

/**
 * Ánh xạ từ LogEntry (lớp Domain)
 * sang LogEntryEntity (lớp cho Room Database) để lưu trữ.
 */
// ✅ SỬA 2: Đảm bảo hàm này nhận đủ tham số cần thiết
fun LogEntry.toEntity(userId: String, medicineName: String): LogEntryEntity {
    // Logic này đã đúng, đảm bảo LogEntryEntity có các thuộc tính này
    return LogEntryEntity(
        logId = this.logId,
        // ✅ SỬA 3: Gán các giá trị từ tham số và từ chính đối tượng LogEntry
        userId = userId,
        medicineId = this.medicineId,
        medicineName = medicineName,
        intakeTime = this.intakeTime,
        status = this.status
    )
}
