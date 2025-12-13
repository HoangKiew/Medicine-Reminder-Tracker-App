package com.example.medinotify.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.medinotify.data.model.MedicineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    /** Chèn/Cập nhật thuốc (Dùng cho CREATE và UPDATE) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity)

    /** ✨ HÀM MỚI: Chèn một danh sách thuốc (Dùng để đồng bộ từ Firebase) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<MedicineEntity>)

    /** Lấy tất cả thuốc của một User */
    @Query("SELECT * FROM medicines WHERE userId = :userId ORDER BY name ASC") // Thêm ORDER BY để danh sách luôn được sắp xếp
    fun getAllMedicines(userId: String): Flow<List<MedicineEntity>>

    /** Lấy chi tiết thuốc theo ID */
    @Query("SELECT * FROM medicines WHERE medicineId = :medicineId")
    suspend fun getMedicineById(medicineId: String): MedicineEntity? // Trả về Entity (Nullable)

    /** Xóa một loại thuốc bằng đối tượng */
    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)

    /** ✨ HÀM MỚI: Xóa thuốc trực tiếp bằng ID (Tối ưu hơn) */
    @Query("DELETE FROM medicines WHERE medicineId = :medicineId")
    suspend fun deleteMedicineById(medicineId: String)

    @Query("SELECT * FROM medicines WHERE userId = :userId")
    suspend fun getAllMedicinesList(userId: String): List<MedicineEntity>

}
