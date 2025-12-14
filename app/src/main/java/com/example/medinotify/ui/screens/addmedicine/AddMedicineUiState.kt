package com.example.medinotify.ui.screens.addmedicine

import com.example.medinotify.data.model.Frequency
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Data class quản lý trạng thái của màn hình Thêm/Sửa Thuốc (Add/Edit Medicine).
 */
data class AddMedicineUiState(
    // --- Trạng thái cơ bản ---
    val medicineId: String? = null,
    val name: String = "",
    val medicineType: String = "Chọn dạng thuốc",
    val dosage: String = "",
    val quantity: String = "",
    // *******************************************************************
    // GIẢ SỬ AddMedicineUiState CÓ CÁC FIELD SAU
    // *******************************************************************

    // ==========================================================
    // ✅ TRƯỜNG LỊCH TRÌNH
    // ==========================================================

    // 1. Loại Tần suất (Hàng ngày, Cụ thể, Cách ngày)
    val frequencyType: Frequency = Frequency.DAILY,

    // 2. Số ngày lặp lại (Chỉ dùng cho Frequency.INTERVAL)
    val intervalDays: String = "1",

    // 3. Các ngày được chọn (Chỉ dùng cho Frequency.SPECIFIC_DAYS)
    val selectedDays: Set<DayOfWeek> = emptySet(),

    // 4. Ngày bắt đầu (Bắt buộc)
    val startDate: LocalDate = LocalDate.now(),

    // 5. Các giờ nhắc nhở cụ thể trong ngày
    // ✅ FIX: Phải là Set<LocalTime> để nhất quán với logic của ViewModel
    val specificTimes: Set<LocalTime> = emptySet(),

    // ==========================================================

    // --- Trạng thái UI/Hành vi ---
    val enableReminder: Boolean = false,
    val uiMessage: String? = null,
    val isLoading: Boolean = false
)