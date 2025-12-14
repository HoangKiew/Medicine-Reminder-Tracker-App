package com.example.medinotify.data.model

import java.time.DayOfWeek

enum class Frequency(val displayText: String) {
    DAILY("Hàng ngày"),
    SPECIFIC_DAYS("Ngày cụ thể trong tuần"),
    INTERVAL("Cách ngày (ví dụ: 2 ngày/lần)")
}

enum class WeekDay(val shortName: String, val fullName: String, val javaDayOfWeek: DayOfWeek) {
    MONDAY("T2", "Thứ Hai", DayOfWeek.MONDAY),
    TUESDAY("T3", "Thứ Ba", DayOfWeek.TUESDAY),
    WEDNESDAY("T4", "Thứ Tư", DayOfWeek.WEDNESDAY),
    THURSDAY("T5", "Thứ Năm", DayOfWeek.THURSDAY),
    FRIDAY("T6", "Thứ Sáu", DayOfWeek.FRIDAY),
    SATURDAY("T7", "Thứ Bảy", DayOfWeek.SATURDAY),
    SUNDAY("CN", "Chủ Nhật", DayOfWeek.SUNDAY)
}