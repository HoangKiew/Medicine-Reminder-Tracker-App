package com.example.medinotify.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lớp Entity cho bảng 'medicines' trong cơ sở dữ liệu Room.
 * Cấu trúc này đã được tái cấu trúc để chỉ chứa thông tin về thuốc.
 * Thông tin lịch trình đã được tách sang bảng 'schedules'.
 */
@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey
    val medicineId: String,

    val userId: String,
    val name: String,
    val dosage: String,

    // ✅ SỬA 1: Bổ sung các cột mới cho thông tin thuốc.
    // Đây là nguyên nhân gây ra lỗi 'Unresolved reference' trong Mapper.
    val type: String,        // Dạng thuốc: "Viên", "Nước", "Gói", v.v.
    val quantity: Int,       // Số lượng thuốc còn lại

    // ✅ SỬA 2: Loại bỏ các cột thuộc về lịch trình (Schedule).
    // val timesPerDay: Int,      // <-- ĐÃ XÓA
    // val specificTimes: String, // <-- ĐÃ XÓA

    val notes: String,
    val isActive: Boolean
)
