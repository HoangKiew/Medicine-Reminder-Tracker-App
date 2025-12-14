package com.example.medinotify.utils

import com.example.medinotify.data.domain.Medicine
import com.example.medinotify.data.model.Frequency
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.ceil // Cần import ceil để làm tròn

object CalendarLogic {

    fun isScheduledForDate(
        date: LocalDate,
        medicine: Medicine,
        // ✅ THÊM THAM SỐ: Số lần uống mỗi ngày (Lấy từ Schedules trong ViewModel)
        dosesPerDay: Int
    ): Boolean {

        // 1. CHUẨN HÓA NGÀY BẮT ĐẦU (từ Long sang LocalDate)
        val effectiveStartDate = Instant.ofEpochMilli(medicine.startDateTimestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        // ----------------------------------------------------------------------
        // 2. TÍNH NGÀY KẾT THÚC DỰA TRÊN SỐ LƯỢNG (QUAN TRỌNG)
        // ----------------------------------------------------------------------

        val totalQuantity = medicine.quantity
        val timesPerDay = dosesPerDay

        // Nếu không có lịch uống hoặc không có thuốc, coi như không có lịch trình (Vô hạn)
        val durationDays: Long = if (totalQuantity <= 0) {
            Long.MAX_VALUE // Không có thuốc, coi như không có lịch trình
        } else if (timesPerDay <= 0) {
            // Lỗi dữ liệu: Thuốc có nhưng không có lịch uống. Coi như vô hạn, nhưng sẽ bị lọc ở dưới.
            Long.MAX_VALUE
        } else {
            // Tính số ngày đợt thuốc đủ dùng (Làm tròn lên)
            // Ví dụ: 20 viên, 2 lần/ngày -> 10 ngày
            // Ví dụ: 21 viên, 2 lần/ngày -> 11 ngày (ceil(21/2))
            ceil(totalQuantity.toDouble() / timesPerDay.toDouble()).toLong()
        }

        // Ngày cuối cùng thuốc còn hiệu lực
        val effectiveEndDate = if (durationDays == Long.MAX_VALUE) {
            LocalDate.MAX // Nếu vô hạn (dữ liệu lỗi)
        } else {
            // Ngày cuối cùng là Ngày Bắt đầu + số ngày đợt thuốc - 1 (vì ngày bắt đầu đã tính là 1 ngày)
            effectiveStartDate.plusDays(durationDays).minusDays(1)
        }


        // ----------------------------------------------------------------------
        // 3. KIỂM TRA NGÀY
        // ----------------------------------------------------------------------

        // A. Kiểm tra Ngày Bắt đầu
        if (date.isBefore(effectiveStartDate)) {
            return false
        }

        // ✅ B. KIỂM TRA NGÀY KẾT THÚC (Nếu ngày đã chọn sau ngày kết thúc thì KHÔNG có lịch)
        if (date.isAfter(effectiveEndDate)) {
            return false
        }

        // C. Kiểm tra tần suất (Frequency) - Chỉ chạy nếu nằm trong khoảng [startDate, endDate]
        val daysSinceStart = ChronoUnit.DAYS.between(effectiveStartDate, date)

        return when (medicine.frequencyType) {

            Frequency.DAILY -> true

            Frequency.SPECIFIC_DAYS -> {
                val selectedDays = parseScheduleValueToDays(medicine.scheduleValue)
                selectedDays.contains(date.dayOfWeek)
            }

            Frequency.INTERVAL -> {
                val interval = medicine.scheduleValue?.toIntOrNull() ?: 1
                // Chỉ nhắc nhở nếu số ngày trôi qua chia hết cho khoảng cách (interval)
                daysSinceStart >= 0 && daysSinceStart.toInt() % interval == 0
            }
        }
    }

    // Hàm helper (Giữ nguyên)
    private fun parseScheduleValueToDays(value: String?): Set<DayOfWeek> {
        return if (value != null) {
            value.split(",").mapNotNull {
                try { DayOfWeek.valueOf(it.trim()) } catch (e: Exception) { null }
            }.toSet()
        } else emptySet()
    }
}