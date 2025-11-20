package com.example.medinotify.data.api

data class MedicineDTO(
    val medicineId: String? = null,
    val userId: String? = null,
    val name: String? = null,
    val medicineType: String? = null,
    val dosage: String? = null,
    val timesPerDay: Int? = null,
    val specificTimes: String? = null,      // CSV "08:00,20:00"
    val notes: String? = null,
    val isActive: Boolean = true,

    // ⭐ API trả về times dưới dạng CSV hoặc 1 time
    val times: String? = null,              // "08:00,14:00,20:00"

    // Client-only (dùng khi gửi lên)
    val scheduleTimes: List<String>? = null,
    val startDate: String? = null,
    val frequency: String? = null,
    val dayOfWeek: Int? = null,

    // ⭐ THÊM TRƯỜNG NÀY ĐỂ UI HOẠT ĐỘNG KHÔNG BỊ LỖI
    val isTaken: Boolean? = false           // mặc định chưa uống
)

data class MedicineListResponse(
    val medicines: List<MedicineDTO>? = null
)

data class ApiResponse(
    val success: Boolean? = null,
    val error: String? = null,
    val medicineId: String? = null
)
