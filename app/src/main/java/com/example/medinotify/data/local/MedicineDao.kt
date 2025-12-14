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

    // ✅ HÀM CẦN THIẾT: Cho quá trình đồng bộ (Sync)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<MedicineEntity>)

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Query("SELECT * FROM medicines WHERE userId = :userId")
    fun getAllMedicines(userId: String): Flow<List<MedicineEntity>>

    // ✅ HÀM CẦN THIẾT: Lấy Entity theo ID (để dùng trong logic đồng bộ LogEntry)
    @Query("SELECT * FROM medicines WHERE medicineId = :medicineId")
    suspend fun getMedicineById(medicineId: String): MedicineEntity?

    @Query("DELETE FROM medicines WHERE medicineId = :medicineId")
    suspend fun deleteMedicineById(medicineId: String)

    // ✅ HÀM CẦN THIẾT: Xóa tất cả (cho logout/sync)
    @Query("DELETE FROM medicines")
    suspend fun clearAllMedicines()
}