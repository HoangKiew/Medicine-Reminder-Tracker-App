package com.example.medinotify.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lớp Entity cho bảng 'log_entries'.
 * ✨ ĐÃ SỬA: Thêm giá trị mặc định cho tất cả các trường để Firebase Deserialize thành công.
 */
@Entity(tableName = "log_entries")
data class LogEntryEntity(
    @PrimaryKey
    var logId: String = "",        // ✅ Giá trị mặc định
    var userId: String = "",       // ✅ Giá trị mặc định
    var medicineId: String = "",   // ✅ Giá trị mặc định
    var medicineName: String = "", // ✅ Giá trị mặc định
    var intakeTime: Long = 0L,     // ✅ Giá trị mặc định
    var status: String = ""        // ✅ Giá trị mặc định (Lưu chuỗi "TAKEN" hoặc "SKIPPED")
)