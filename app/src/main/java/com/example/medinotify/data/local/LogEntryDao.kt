package com.example.medinotify.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.medinotify.data.model.LogEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogEntryDao {

    // ✅ FIX LỖI: Thêm hàm insertAll để hỗ trợ đồng bộ dữ liệu
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logEntries: List<LogEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogEntry(logEntry: LogEntryEntity)

    // Lấy log entry trong một khoảng thời gian
    @Query("SELECT * FROM log_entries WHERE userId = :userId AND intakeTime BETWEEN :dateStart AND :dateEnd ORDER BY intakeTime DESC")
    fun getLogEntriesByDateRange(userId: String, dateStart: Long, dateEnd: Long): Flow<List<LogEntryEntity>>

    // Xóa log entry của một loại thuốc (khi thuốc bị xóa)
    @Query("DELETE FROM log_entries WHERE medicineId = :medicineId AND userId = :userId")
    suspend fun deleteLogsForMedicine(medicineId: String, userId: String)

    // ✅ HÀM CẦN THIẾT: Xóa tất cả log entry (cần cho logout/sync)
    @Query("DELETE FROM log_entries")
    suspend fun clearAllLogs()
}