package com.example.medinotify.data.model

import com.example.medinotify.data.domain.LogStatus

/**
 * Data class đại diện cho một mục trong danh sách lịch sử để hiển thị trên UI.
 */
data class MedicineHistoryUi(
    val id: String,
    val name: String,    // Tên thuốc
    val dosage: String,  // Liều lượng
    val time: String,    // Giờ uống (HH:mm)
    val isTaken: Boolean, // Trạng thái đã uống (true/false)
    val intakeTime: Long
)