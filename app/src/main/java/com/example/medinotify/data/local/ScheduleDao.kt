package com.example.medinotify.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.medinotify.data.model.ScheduleEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

@Dao
interface ScheduleDao {
    /** Chèn/Cập nhật một lịch nhắc nhở. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity)

    /** Chèn một danh sách lịch (hữu ích khi đồng bộ từ Firebase). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    /** Lấy một lịch cụ thể bằng ID. */
    @Query("SELECT * FROM schedules WHERE scheduleId = :scheduleId")
    suspend fun getScheduleById(scheduleId: String): ScheduleEntity?

    /** Lấy các lịch đang hoạt động cho một loại thuốc của một người dùng. */
    @Query("SELECT * FROM schedules WHERE medicineId = :medicineId AND userId = :userId AND reminderStatus = 1")
    fun getActiveSchedulesForMedicine(medicineId: String, userId: String): Flow<List<ScheduleEntity>>

    /**
     * Lấy các lịch trong một khoảng thời gian cho một người dùng cụ thể.
     */
    @Query("""
        SELECT * FROM schedules 
        WHERE userId = :userId AND nextScheduledTimestamp BETWEEN :dateStart AND :dateEnd 
        ORDER BY nextScheduledTimestamp ASC
    """)
    fun getSchedulesByDateRange(userId: String, dateStart: Long, dateEnd: Long): Flow<List<ScheduleEntity>>

    /** Cập nhật trạng thái nhắc nhở (ON/OFF) cho một lịch bằng ID. */
    @Query("UPDATE schedules SET reminderStatus = :status WHERE scheduleId = :scheduleId")
    suspend fun updateReminderStatus(scheduleId: String, status: Boolean)

    /** * ✨✨✨ ĐÃ SỬA LẠI: BỎ userId ✨✨✨
     * Cập nhật trạng thái "Đã uống" chỉ dựa trên MedicineID và Giờ.
     * Điều này giúp lệnh update dễ thành công hơn.
     */
    @Query("UPDATE schedules SET reminderStatus = :status WHERE medicineId = :medicineId AND specificTime = :time")
    suspend fun updateScheduleStatus(medicineId: String, time: LocalTime, status: Boolean)

    /** Xóa một lịch nhắc nhở bằng đối tượng. */
    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    /** Xóa tất cả lịch liên quan đến một loại thuốc của một người dùng. */
    @Query("DELETE FROM schedules WHERE medicineId = :medicineId AND userId = :userId")
    suspend fun deleteSchedulesByMedicineId(medicineId: String, userId: String)
}