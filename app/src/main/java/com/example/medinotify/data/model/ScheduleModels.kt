package com.example.medinotify.data.model

import java.time.DayOfWeek // ✅ BƯỚC 1: THÊM IMPORT QUAN TRỌNG NÀY

enum class Frequency(val displayText: String) {
    DAILY("Hàng ngày"),
    SPECIFIC_DAYS("Ngày cụ thể trong tuần"), // Sửa lại text cho rõ ràng hơn
    INTERVAL("Cách ngày (ví dụ: 2 ngày/lần)") // Sửa lại text cho rõ ràng hơn
}

// ✅ BƯỚC 2: THÊM THUỘC TÍNH `javaDayOfWeek`
enum class WeekDay(val shortName: String, val fullName: String, val javaDayOfWeek: DayOfWeek) {
    MONDAY("T2", "Thứ Hai", DayOfWeek.MONDAY),
    TUESDAY("T3", "Thứ Ba", DayOfWeek.TUESDAY),
    WEDNESDAY("T4", "Thứ Tư", DayOfWeek.WEDNESDAY),
    THURSDAY("T5", "Thứ Năm", DayOfWeek.THURSDAY),
    FRIDAY("T6", "Thứ Sáu", DayOfWeek.FRIDAY),
    SATURDAY("T7", "Thứ Bảy", DayOfWeek.SATURDAY),
    SUNDAY("CN", "Chủ Nhật", DayOfWeek.SUNDAY)
}
