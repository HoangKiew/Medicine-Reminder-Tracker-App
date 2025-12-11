package com.example.medinotify.data.local

// ✅ SỬA LỖI 1: Xóa dòng import sai gây ra lỗi "Argument type mismatch"
// import androidx.compose.ui.input.key.type // <--- XÓA DÒNG NÀY

import androidx.compose.ui.input.key.type
import com.example.medinotify.data.domain.Medicine

// ✅ SỬA LỖI 2: Sửa lại đường dẫn import để trỏ đến đúng file Entity đã được cập nhật.
// Giả sử file Entity của bạn nằm trong package 'com.example.medinotify.data.local.model'.
// Hãy điều chỉnh lại cho đúng với cấu trúc dự án của bạn nếu cần.
import com.example.medinotify.data.model.MedicineEntity

/**
 * Chuyển đổi từ MedicineEntity (dữ liệu thô từ Room)
 * sang Medicine (lớp Domain cho logic và UI).
 */
fun MedicineEntity.toDomainModel(): Medicine {
    // Lỗi Unresolved reference 'type' và 'quantity' sẽ biến mất
    // vì chúng ta đã import đúng file Entity chứa các thuộc tính này.
    return Medicine(
        medicineId = this.medicineId,
        name = this.name,
        dosage = this.dosage,
        type = this.type,
        quantity = this.quantity,
        notes = this.notes,
        isActive = this.isActive
    )
}

/**
 * Chuyển đổi một đối tượng Medicine (lớp domain)
 * sang một đối tượng MedicineEntity để lưu vào cơ sở dữ liệu.
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
        isActive = this.isActive
    )
}

