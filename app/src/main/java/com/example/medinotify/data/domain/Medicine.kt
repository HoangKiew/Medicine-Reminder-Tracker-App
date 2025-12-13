package com.example.medinotify.data.domain

import java.util.UUID

data class Medicine(
    // ✅ SỬA 1: Cung cấp giá trị mặc định và đổi tên thuộc tính cho nhất quán
    val medicineId: String = UUID.randomUUID().toString(),

    val name: String,
    val dosage: String,      // Liều lượng, ví dụ: "500mg"

    // ✅ SỬA 2: Bổ sung các thuộc tính quan trọng còn thiếu
    val type: String,        // Dạng thuốc: "Viên", "Nước", "Gói", v.v.
    val quantity: Int,       // Số lượng thuốc còn lại

    // ✅ SỬA 3: Loại bỏ các thuộc tính của Schedule
    // `timesPerDay` và `specificTimes` đã được xóa khỏi đây.

    val notes: String,       // Ghi chú thêm về thuốc (ví dụ: "Uống sau khi ăn")
    val isActive: Boolean    // Trạng thái của thuốc (có đang trong lịch trình uống hay không)
)
