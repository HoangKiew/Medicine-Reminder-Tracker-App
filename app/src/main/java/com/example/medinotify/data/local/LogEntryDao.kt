package com.example.medinotify.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
// ✅ SỬA 1: Xóa dòng import sai.
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
     * Chèn một danh sách LogEntry (hữu ích khi đồng bộ từ Firebase).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogEntries(logEntries: List<LogEntryEntity>)

    /**
     * Lấy tất cả lịch sử uống thuốc trong một khoảng thời gian CHO MỘT NGƯỜI DÙNG.
     */
    // ✅ SỬA 2: Thêm `userId` và sửa tên cột thời gian
    @Query("""
        SELECT * FROM log_entries 
        WHERE userId = :userId AND intakeTime BETWEEN :dateStart AND :dateEnd 
        ORDER BY intakeTime DESC
    """)
    fun getLogEntriesByDateRange(userId: String, dateStart: Long, dateEnd: Long): Flow<List<LogEntryEntity>>

    /**
     * Lấy lịch sử log cho một loại thuốc cụ thể CỦA MỘT NGƯỜI DÙNG.
     */
    // ✅ SỬA 3: Thêm `userId` và sửa tên cột thời gian
    @Query("SELECT * FROM log_entries WHERE medicineId = :medicineId AND userId = :userId ORDER BY intakeTime DESC")
    fun getLogHistoryForMedicine(medicineId: String, userId: String): Flow<List<LogEntryEntity>>

    /**
     * Cập nhật trạng thái uống thuốc và thời gian thực tế đã uống.
     * Hàm này có thể không cần thiết nếu bạn luôn chèn bản ghi mới.
     * Nếu giữ lại, nó cần được sửa cho đúng.
     */
    // ✅ SỬA 4: Sửa lại tên cột và tham số cho nhất quán
    @Query("""
        UPDATE log_entries 
        SET status = :newStatus, intakeTime = :intakeTime 
        WHERE logId = :logId
    """)
    suspend fun updateLogStatus(logId: String, newStatus: String, intakeTime: Long)

    /**
     * Xóa tất cả các log liên quan đến một loại thuốc CỦA MỘT NGƯỜI DÙNG.
     * ❗️QUAN TRỌNG: Cần thêm userId để tránh xóa nhầm dữ liệu người dùng khác.
     */
    // ✅ SỬA 5: Thêm `userId` vào điều kiện xóa
    @Query("DELETE FROM log_entries WHERE medicineId = :medicineId AND userId = :userId")
    suspend fun deleteLogsForMedicine(medicineId: String, userId: String)
}

