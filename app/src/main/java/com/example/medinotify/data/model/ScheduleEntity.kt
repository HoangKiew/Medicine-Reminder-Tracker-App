package com.example.medinotify.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Lớp Entity cho bảng 'schedules'.
 * ✨ ĐÃ SỬA: Thêm giá trị mặc định cho tất cả các trường để Firebase Deserialize thành công.
 */
@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["medicineId"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicineId"])]
)
data class ScheduleEntity(
    @PrimaryKey
    var scheduleId: String = "",            // ✅ Giá trị mặc định
    var userId: String = "",                // ✅ Giá trị mặc định
    var medicineId: String = "",            // ✅ Giá trị mặc định
    var specificTime: String = "",          // ✅ Giá trị mặc định (e.g., "10:00")
    var nextScheduledTimestamp: Long = 0L,  // ✅ Giá trị mặc định
    var reminderStatus: Boolean = false     // ✅ Giá trị mặc định
)
