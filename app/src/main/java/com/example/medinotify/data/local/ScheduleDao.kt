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

    /** Chèn một danh sách lịch */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    /** Lấy một lịch cụ thể bằng ID. */
    @Query("SELECT * FROM schedules WHERE scheduleId = :scheduleId")
    suspend fun getScheduleById(scheduleId: String): ScheduleEntity?

    /**
     * ✨✨✨ THÊM HÀM NÀY ✨✨✨
     * Lấy tất cả lịch trình của user. Dùng để hiển thị danh sách thuốc hằng ngày.
     */
    @Query("SELECT * FROM schedules WHERE userId = :userId")
    fun getAllSchedules(userId: String): Flow<List<ScheduleEntity>>

    // ... (Các hàm updateReminderStatus, updateScheduleStatus, delete... GIỮ NGUYÊN)
    @Query("UPDATE schedules SET reminderStatus = :status WHERE scheduleId = :scheduleId")
    suspend fun updateReminderStatus(scheduleId: String, status: Boolean)

    @Query("UPDATE schedules SET reminderStatus = :status WHERE medicineId = :medicineId AND specificTime = :time")
    suspend fun updateScheduleStatus(medicineId: String, time: LocalTime, status: Boolean)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    @Query("DELETE FROM schedules WHERE medicineId = :medicineId AND userId = :userId")
    suspend fun deleteSchedulesByMedicineId(medicineId: String, userId: String)

    @Query("DELETE FROM schedules")
    suspend fun clearAllSchedules()

    @Query("DELETE FROM schedules WHERE medicineId = :medId")
    suspend fun deleteSchedulesByMedicineId(medId: String)

    // (Các hàm getActiveSchedulesForMedicine, getSchedulesByDateRange có thể giữ lại hoặc bỏ nếu không dùng)
    @Query("SELECT * FROM schedules WHERE medicineId = :medicineId AND userId = :userId AND reminderStatus = 1")
    fun getActiveSchedulesForMedicine(medicineId: String, userId: String): Flow<List<ScheduleEntity>>

    @Query("""
        SELECT * FROM schedules 
        WHERE userId = :userId AND nextScheduledTimestamp BETWEEN :dateStart AND :dateEnd 
        ORDER BY nextScheduledTimestamp ASC
    """)
    fun getSchedulesByDateRange(userId: String, dateStart: Long, dateEnd: Long): Flow<List<ScheduleEntity>>
}