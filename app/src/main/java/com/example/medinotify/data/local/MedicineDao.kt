package com.example.medinotify.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.medinotify.data.model.MedicineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<MedicineEntity>)

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    /**
     * Lấy tất cả thuốc của người dùng hiện tại.
     */
    @Query("SELECT * FROM medicines WHERE userId = :userId")
    fun getAllMedicines(userId: String): Flow<List<MedicineEntity>>

    /**
     * Lấy Entity theo ID thuốc và ID người dùng.
     */
    @Query("SELECT * FROM medicines WHERE medicineId = :medicineId AND userId = :userId")
    suspend fun getMedicineById(medicineId: String, userId: String): MedicineEntity? // <-- Thêm tham số userId

    @Query("DELETE FROM medicines WHERE medicineId = :medicineId")
    suspend fun deleteMedicineById(medicineId: String)

    // HÀM CẦN THIẾT: Xóa tất cả (cho logout/sync)
    @Query("DELETE FROM medicines")
    suspend fun clearAllMedicines()
}