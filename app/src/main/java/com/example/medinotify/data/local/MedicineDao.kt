package com.example.medinotify.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update // ✅ Đã thêm import này
import com.example.medinotify.data.model.MedicineEntity // (Giữ nguyên import của bạn)
// Nếu Entity của bạn nằm ở package khác (vd: data.local.entity), hãy sửa lại dòng trên
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    // ================== CÁC HÀM THÊM (CREATE) ==================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<MedicineEntity>)

    // ================== CÁC HÀM ĐỌC (READ) ==================

    @Query("SELECT * FROM medicines WHERE userId = :userId ORDER BY name ASC")
    fun getAllMedicines(userId: String): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE medicineId = :medicineId")
    suspend fun getMedicineById(medicineId: String): MedicineEntity?

    // ================== CÁC HÀM CẬP NHẬT (UPDATE) ==================

    // ✨✨✨ ĐÂY LÀ HÀM BẠN ĐANG THIẾU ✨✨✨
    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    // ================== CÁC HÀM XÓA (DELETE) ==================

    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)

    @Query("DELETE FROM medicines WHERE medicineId = :medicineId")
    suspend fun deleteMedicineById(medicineId: String)

    @Query("DELETE FROM medicines")
    suspend fun clearAllMedicines()
}