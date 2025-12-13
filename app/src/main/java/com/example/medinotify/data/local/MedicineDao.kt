package com.example.medinotify.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.medinotify.data.model.MedicineEntity
import com.example.medinotify.data.model.ScheduleEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

@Dao
interface MedicineDao {
    // ================== PHẦN THUỐC (MEDICINES) ==================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<MedicineEntity>)

    @Query("SELECT * FROM medicines WHERE userId = :userId ORDER BY name ASC")
    fun getAllMedicines(userId: String): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE medicineId = :medicineId")
    suspend fun getMedicineById(medicineId: String): MedicineEntity?

    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)

    @Query("DELETE FROM medicines WHERE medicineId = :medicineId")
    suspend fun deleteMedicineById(medicineId: String)

    /**
     * ✨✨✨ HÀM CÒN THIẾU: Xóa sạch bảng thuốc ✨✨✨
     */
    @Query("DELETE FROM medicines")
    suspend fun clearAllMedicines()


    // ================== PHẦN LỊCH (SCHEDULES) ==================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Query("SELECT * FROM schedules WHERE date(specificTime) = date(:date)")
    fun getSchedulesForDate(date: LocalDate): Flow<List<ScheduleEntity>>

    @Query("UPDATE schedules SET reminderStatus = :status WHERE medicineId = :medicineId AND specificTime = :time")
    suspend fun updateScheduleStatus(medicineId: String, time: LocalTime, status: Boolean)
}