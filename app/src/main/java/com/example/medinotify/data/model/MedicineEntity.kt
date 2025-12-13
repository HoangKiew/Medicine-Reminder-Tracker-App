package com.example.medinotify.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.medinotify.data.domain.Medicine
import java.time.DayOfWeek

/**
 * Lớp Entity cho bảng 'medicines'.
 * ✅ ĐÃ SỬA: Đã sửa các kiểu dữ liệu và thuộc tính để tương thích với Firebase Deserialize và Room.
 */
@Entity(tableName = "medicines")
data class MedicineEntity(
    // 1. Khóa chính phải là 'var' hoặc có @ColumnInfo(name = "id") nếu muốn Room/Firebase ghi.
    @PrimaryKey
    var medicineId: String = "", // ✅ THAY ĐỔI: Dùng var và giá trị mặc định cho Firebase

    // 2. Các trường khác chuyển sang 'var' và thêm giá trị mặc định
    var userId: String = "",
    var name: String = "",
    var dosage: String = "",
    var type: String = "",
    var quantity: Int = 0,
    var notes: String = "",
    var isActive: Boolean = true, // Lỗi No setter/field cho isActive đã được giải quyết bằng 'var'

    // 3. SỬA LỖI FIREBASE: Đổi Set thành List
    var frequency: Frequency = Frequency.DAILY,
    var intervalDays: Int = 1,
    var daysOfWeek: List<DayOfWeek> = emptyList() // ❌ LỖI GỐC: Phải dùng List thay vì Set cho Firebase
)

/**
 * ✨ HÀM MỞ RỘNG (Extension Function) ✨
 * Chuyển đổi từ lớp Entity (dùng cho Database) sang lớp Domain (dùng trong logic ứng dụng).
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
        // Dùng List của Entity cho Domain
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
        userId = userId,
        name = this.name,
        dosage = this.dosage,
        type = this.type,
        quantity = this.quantity,
        notes = this.notes,
        isActive = this.isActive,
        // Dùng List cho Entity
        frequency = this.frequency,
        intervalDays = this.intervalDays,
        daysOfWeek = this.daysOfWeek
    )
}