package com.example.medinotify.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.medinotify.data.model.LogEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogEntry(logEntry: LogEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogEntries(logEntries: List<LogEntryEntity>)

    @Query("""
        SELECT * FROM log_entries 
        WHERE userId = :userId AND intakeTime BETWEEN :dateStart AND :dateEnd 
        ORDER BY intakeTime DESC
    """)
    fun getLogEntriesByDateRange(userId: String, dateStart: Long, dateEnd: Long): Flow<List<LogEntryEntity>>

    @Query("SELECT * FROM log_entries WHERE medicineId = :medicineId AND userId = :userId ORDER BY intakeTime DESC")
    fun getLogHistoryForMedicine(medicineId: String, userId: String): Flow<List<LogEntryEntity>>

    @Query("""
        UPDATE log_entries 
        SET status = :newStatus, intakeTime = :intakeTime 
        WHERE logId = :logId
    """)
    suspend fun updateLogStatus(logId: String, newStatus: String, intakeTime: Long)

    @Query("DELETE FROM log_entries WHERE medicineId = :medicineId AND userId = :userId")
    suspend fun deleteLogsForMedicine(medicineId: String, userId: String)

    /**
     * ✨✨✨ HÀM CÒN THIẾU: Xóa sạch bảng Log ✨✨✨
     */
    @Query("DELETE FROM log_entries")
    suspend fun clearAllLogs()
}