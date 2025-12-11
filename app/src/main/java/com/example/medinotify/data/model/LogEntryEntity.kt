package com.example.medinotify.data.model // Hoặc tốt hơn là: com.example.medinotify.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "log_entries")
data class LogEntryEntity(
    // ✅ SỬA 1: Dùng 'logId' làm Khóa chính (@PrimaryKey) cho nhất quán.
    @PrimaryKey
    val logId: String,

    // ✅ SỬA 2: Thêm cột 'userId' để biết log này thuộc về người dùng nào.
    // Đây là cột bắt buộc để lọc dữ liệu.
    val userId: String,

    // ID của loại thuốc đã được ghi nhận.
    val medicineId: String,

    // ✅ SỬA 3: Giữ lại 'medicineName' để hiển thị nhanh trên UI mà không cần join bảng.
    val medicineName: String,

    // ✅ SỬA 4: Hợp nhất 'scheduledTime' và 'actualTime' thành một cột duy nhất 'intakeTime'.
    // Một bản ghi lịch sử chỉ cần biết thời gian hành động xảy ra.
    // Kiểu dữ liệu là Date để Room tự động xử lý.
    val intakeTime: Long,

    // ✅ SỬA 5: 'status' không nên có giá trị mặc định trong Entity.
    // Giá trị này sẽ được cung cấp khi tạo LogEntry.
    val status: String // Ví dụ: "TAKEN", "SKIPPED"
)
