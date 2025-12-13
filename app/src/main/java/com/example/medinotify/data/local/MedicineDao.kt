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

    /** Chèn một danh sách thuốc (Dùng để đồng bộ từ Firebase) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<MedicineEntity>)

    /** ✅ SỬA: Thêm hàm chèn nhiều bản ghi (đổi tên để phù hợp với logic sync trong Repository) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medicines: List<MedicineEntity>)

    /** Lấy tất cả thuốc của một User */
    @Query("SELECT * FROM medicines WHERE userId = :userId ORDER BY name ASC")
    fun getAllMedicines(userId: String): Flow<List<MedicineEntity>>

    /** Lấy chi tiết thuốc theo ID */
    @Query("SELECT * FROM medicines WHERE medicineId = :medicineId")
    suspend fun getMedicineById(medicineId: String): MedicineEntity?

    /** Xóa một loại thuốc bằng đối tượng */
    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)

    /** Xóa thuốc trực tiếp bằng ID (Tối ưu hơn) */
    @Query("DELETE FROM medicines WHERE medicineId = :medicineId")
    suspend fun deleteMedicineById(medicineId: String)

    @Query("SELECT * FROM medicines WHERE userId = :userId")
    suspend fun getAllMedicinesList(userId: String): List<MedicineEntity>

    /** ✅ BỔ SUNG: Xóa tất cả bản ghi (Dùng để dọn dẹp khi Logout/trước khi Sync) */
    @Query("DELETE FROM medicines")
    suspend fun clearAll()
}