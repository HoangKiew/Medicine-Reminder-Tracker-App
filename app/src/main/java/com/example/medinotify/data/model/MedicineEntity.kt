package com.example.medinotify.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.medinotify.data.domain.Medicine // ✅ THÊM IMPORT
import java.time.DayOfWeek // ✅ THÊM IMPORT

/**
 * Lớp Entity cho bảng 'medicines' trong cơ sở dữ liệu Room.
 * Đã được cập nhật để bao gồm các cột lưu trữ thông tin về tần suất uống thuốc.
 */
@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey
    val medicineId: String,
    val userId: String,
    val name: String,
    val dosage: String,
    val type: String,
    val quantity: Int,
    val notes: String,
    val isActive: Boolean,

    // ✅ BƯỚC QUAN TRỌNG: Thêm các cột tương ứng với data class Medicine
    // Điều này sẽ giải quyết lỗi khi Mapper và Room hoạt động.
    val frequency: Frequency,
    val intervalDays: Int, // Chỉ dùng khi frequency là INTERVAL
    val daysOfWeek: Set<DayOfWeek> // Chỉ dùng khi frequency là SPECIFIC_DAYS
)

/**
 * ✨ HÀM MỞ RỘNG (Extension Function) ✨
 * Chuyển đổi từ lớp Entity (dùng cho Database) sang lớp Domain (dùng trong logic ứng dụng).
 * Đây là một cách làm rất tốt để tách biệt tầng dữ liệu và tầng domain.
 */
fun MedicineEntity.toDomainModel(): Medicine {
    return Medicine(
        medicineId = this.medicineId,
        name = this.name,
        dosage = this.dosage,
        type = this.type,
        quantity = this.quantity,
        notes = this.notes,
        isActive = this.isActive,
        frequency = this.frequency,
        intervalDays = this.intervalDays,
        daysOfWeek = this.daysOfWeek
    )
}

/**
 * ✨ HÀM MỞ RỘNG (Extension Function) ✨
 * Chuyển đổi từ lớp Domain (dùng trong logic) sang lớp Entity (để lưu vào Database).
 */
fun Medicine.toEntity(userId: String): MedicineEntity {
    return MedicineEntity(
        medicineId = this.medicineId,
        userId = userId, // Cần userId để lưu vào database
        name = this.name,
        dosage = this.dosage,
        type = this.type,
        quantity = this.quantity,
        notes = this.notes,
        isActive = this.isActive,
        frequency = this.frequency,
        intervalDays = this.intervalDays,
        daysOfWeek = this.daysOfWeek
    )
}
