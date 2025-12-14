package com.example.medinotify.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.medinotify.data.model.ScheduleEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

@Dao
interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    /**
     * Lấy tất cả lịch trình của người dùng hiện tại. (Đã có bộ lọc userId)
     */
    @Query("SELECT * FROM schedules WHERE userId = :userId")
    fun getAllSchedules(userId: String): Flow<List<ScheduleEntity>>

    @Query("DELETE FROM schedules WHERE medicineId = :medicineId")
    suspend fun deleteSchedulesByMedicineId(medicineId: String)

    @Query("DELETE FROM schedules")
    suspend fun clearAllSchedules()

    @Query("SELECT * FROM schedules WHERE userId = :userId AND nextScheduledTimestamp BETWEEN :dateStart AND :dateEnd ORDER BY nextScheduledTimestamp ASC")
    fun getSchedulesByDateRange(userId: String, dateStart: Long, dateEnd: Long): Flow<List<ScheduleEntity>>

    /**
     * Cập nhật trạng thái 'Đã uống' cho một lịch trình cụ thể của người dùng.
     */
    @Query("UPDATE schedules SET reminderStatus = :status WHERE medicineId = :medicineId AND specificTimeStr = :timeString AND userId = :userId")
    suspend fun updateScheduleStatus(medicineId: String, timeString: String, status: Boolean, userId: String) // <-- Thêm tham số userId
}