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
    suspend fun insertAll(logEntries: List<LogEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogEntry(logEntry: LogEntryEntity)

    @Query("SELECT * FROM log_entries WHERE userId = :userId AND intakeTime BETWEEN :dateStart AND :dateEnd ORDER BY intakeTime DESC")
    fun getLogEntriesByDateRange(userId: String, dateStart: Long, dateEnd: Long): Flow<List<LogEntryEntity>>

    @Query("DELETE FROM log_entries WHERE medicineId = :medicineId AND userId = :userId")
    suspend fun deleteLogsForMedicine(medicineId: String, userId: String)

    @Query("DELETE FROM log_entries")
    suspend fun clearAllLogs()
}