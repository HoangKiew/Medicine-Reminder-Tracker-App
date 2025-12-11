package com.example.medinotify.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Lớp Entity cho bảng 'schedules' trong cơ sở dữ liệu Room.
 * Cấu trúc này đã được tái cấu trúc để chỉ chứa thông tin về lịch trình.
 */
@Entity(
    tableName = "schedules",
    // ✅ SỬA 1: Thêm ForeignKey để đảm bảo tính toàn vẹn dữ liệu
    // Nếu một Medicine bị xóa, tất cả các Schedule liên quan cũng sẽ bị xóa.
    foreignKeys = [
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["medicineId"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // ✅ SỬA 2: Thêm Index để tăng tốc độ truy vấn theo medicineId
    indices = [Index(value = ["medicineId"])]
)
data class ScheduleEntity(
    @PrimaryKey
    val scheduleId: String,

    val userId: String,
    val medicineId: String,

    // ✅ SỬA 3: Sử dụng String để lưu trữ thời gian trong database
    // Kiểu dữ liệu LocalTime không thể lưu trực tiếp.
    // Mapper sẽ đảm nhiệm việc chuyển đổi giữa String và LocalTime.
    val specificTime: String, // Định dạng "HH:mm"

    val nextScheduledTimestamp: Long,
    val reminderStatus: Boolean
)
