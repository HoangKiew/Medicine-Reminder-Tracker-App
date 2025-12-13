package com.example.medinotify.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.medinotify.data.model.MedicineEntity
// ✅ SỬA 1: Import ScheduleEntity thay vì Schedule
import com.example.medinotify.data.model.ScheduleEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

@Dao
interface MedicineDao {
    // ================== PHẦN THUỐC (MEDICINES) ==================

    /** Chèn/Cập nhật thuốc */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity)

    /** Chèn danh sách thuốc (đồng bộ) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<MedicineEntity>)

    /** Lấy tất cả thuốc của User */
    @Query("SELECT * FROM medicines WHERE userId = :userId ORDER BY name ASC")
    fun getAllMedicines(userId: String): Flow<List<MedicineEntity>>

    /** Lấy chi tiết thuốc theo ID */
    @Query("SELECT * FROM medicines WHERE medicineId = :medicineId")
    suspend fun getMedicineById(medicineId: String): MedicineEntity?

    /** Xóa thuốc bằng đối tượng */
    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)

    /** Xóa thuốc theo ID */
    @Query("DELETE FROM medicines WHERE medicineId = :medicineId")
    suspend fun deleteMedicineById(medicineId: String)


    // ================== PHẦN LỊCH (SCHEDULES) ==================
    // Lưu ý: Các hàm này nên dùng ScheduleEntity thay vì Schedule

    /** 1. Chèn danh sách lịch nhắc nhở */
    // ✅ SỬA 2: Thay List<Schedule> bằng List<ScheduleEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    /** 2. Lấy lịch uống thuốc theo ngày cụ thể */
    // ✅ SỬA 3: Trả về Flow<List<ScheduleEntity>>
    @Query("SELECT * FROM schedules WHERE date(specificTime) = date(:date)")
    fun getSchedulesForDate(date: LocalDate): Flow<List<ScheduleEntity>>

    /**
     * Cập nhật trạng thái Đã uống/Chưa uống
     */
    @Query("UPDATE schedules SET reminderStatus = :status WHERE medicineId = :medicineId AND specificTime = :time")
    suspend fun updateScheduleStatus(medicineId: String, time: LocalTime, status: Boolean)
}