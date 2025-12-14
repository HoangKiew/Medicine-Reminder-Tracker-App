package com.example.medinotify.data.local

import com.example.medinotify.data.domain.Medicine
import com.example.medinotify.data.model.Frequency
import com.example.medinotify.data.model.MedicineEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Chuyển đổi từ MedicineEntity (dữ liệu thô từ Room)
 * sang Medicine (lớp Domain cho logic và UI).
 */
fun MedicineEntity.toDomainModel(): Medicine {
    // 1. Chuyển đổi Long Timestamp từ Entity thành LocalDate (sử dụng cục bộ)
    val localDate = Instant.ofEpochMilli(this.startDateTimestamp)
        .atZone(ZoneId.systemDefault()).toLocalDate()

    return Medicine(
        medicineId = this.medicineId,
        name = this.name,
        dosage = this.dosage,
        type = this.type,
        quantity = this.quantity,
        notes = this.notes,
        isActive = this.isActive,

        // Domain Model (Medicine.kt) phải được định nghĩa với startDateTimestamp
        startDateTimestamp = this.startDateTimestamp,

        frequencyType = Frequency.valueOf(this.frequencyType),
        scheduleValue = this.scheduleValue,
    )
}

/**
 * Chuyển đổi một đối tượng Medicine (lớp domain)
 * sang một đối tượng MedicineEntity để lưu vào cơ sở dữ liệu.
 */
fun Medicine.toEntity(userId: String): MedicineEntity {

    // 2. Lấy Long Timestamp trực tiếp từ Domain Model
    val startDateTimestamp = this.startDateTimestamp

    return MedicineEntity(
        medicineId = this.medicineId,
        userId = userId,
        name = this.name,
        dosage = this.dosage,
        type = this.type,
        quantity = this.quantity,
        notes = this.notes,
        isActive = this.isActive,

        frequencyType = this.frequencyType.name,
        scheduleValue = this.scheduleValue,
        startDateTimestamp = startDateTimestamp,
    )
}