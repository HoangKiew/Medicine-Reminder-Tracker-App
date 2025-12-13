package com.example.medinotify.data.local

// ✅ XÓA BỎ IMPORT SAI, THÊM IMPORT ĐÚNG
import com.example.medinotify.data.domain.Medicine
import com.example.medinotify.data.model.MedicineEntity

/**
 * Chuyển đổi từ MedicineEntity (dữ liệu thô từ Room)
 * sang Medicine (lớp Domain cho logic và UI).
 * Đã được cập nhật để bao gồm các trường tần suất.
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
 * Chuyển đổi một đối tượng Medicine (lớp domain)
 * sang một đối tượng MedicineEntity để lưu vào cơ sở dữ liệu.
 * Đã được cập nhật để bao gồm các trường tần suất.
 */
fun Medicine.toEntity(userId: String): MedicineEntity {
    return MedicineEntity(
        medicineId = this.medicineId,
        userId = userId, // Luôn cần userId khi lưu vào DB
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
