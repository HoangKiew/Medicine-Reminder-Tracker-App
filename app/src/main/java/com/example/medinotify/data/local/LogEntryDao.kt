package com.example.medinotify.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.medinotify.data.model.LogEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogEntryDao {

    /**
     * Chèn hoặc cập nhật một mục LogEntry.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogEntry(logEntry: LogEntryEntity)

    /**
     * ✅ ĐỔI TÊN/SỬA: Chèn một danh sách LogEntry (Đảm bảo tên khớp với Repository).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logEntries: List<LogEntryEntity>) // Đổi tên thành insertAll (hoặc giữ insertLogEntries nếu Repository gọi là thế)

    /**
     * Lấy tất cả lịch sử uống thuốc trong một khoảng thời gian CHO MỘT NGƯỜI DÙNG.
     */
    @Query("""
        SELECT * FROM log_entries 
        WHERE userId = :userId AND intakeTime BETWEEN :dateStart AND :dateEnd 
        ORDER BY intakeTime DESC
    """)
    fun getLogEntriesByDateRange(userId: String, dateStart: Long, dateEnd: Long): Flow<List<LogEntryEntity>>

    /**
     * Lấy lịch sử log cho một loại thuốc cụ thể CỦA MỘT NGƯỜI DÙNG.
     */
    @Query("SELECT * FROM log_entries WHERE medicineId = :medicineId AND userId = :userId ORDER BY intakeTime DESC")
    fun getLogHistoryForMedicine(medicineId: String, userId: String): Flow<List<LogEntryEntity>>

    /**
     * Cập nhật trạng thái uống thuốc và thời gian thực tế đã uống.
     * (Hàm này ít dùng nếu bạn luôn chèn bản ghi mới, nhưng giữ lại)
     */
    @Query("""
        UPDATE log_entries 
        SET status = :newStatus, intakeTime = :intakeTime 
        WHERE logId = :logId
    """)
    suspend fun updateLogStatus(logId: String, newStatus: String, intakeTime: Long)

    @Query("DELETE FROM log_entries WHERE medicineId = :medicineId AND userId = :userId")
    suspend fun deleteLogsForMedicine(medicineId: String, userId: String)


    @Query("SELECT * FROM log_entries WHERE intakeTime >= :startTime AND intakeTime < :endTime AND userId = :userId ORDER BY intakeTime DESC")
    fun getLogEntriesBetween(startTime: Long, endTime: Long, userId: String): Flow<List<LogEntryEntity>>

    /** ✅ BỔ SUNG: Xóa tất cả bản ghi (Dùng để dọn dẹp khi Logout/trước khi Sync) */
    @Query("DELETE FROM log_entries")
    suspend fun clearAll()
}