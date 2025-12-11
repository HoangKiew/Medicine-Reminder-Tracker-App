package com.example.medinotify.data.repository

import android.util.Log
import com.example.medinotify.data.domain.LogEntry
import com.example.medinotify.data.domain.Medicine
import com.example.medinotify.data.domain.Schedule
import com.example.medinotify.data.local.LogEntryDao
import com.example.medinotify.data.local.MedicineDao
import com.example.medinotify.data.local.ScheduleDao
import com.example.medinotify.data.local.toDomainModel
import com.example.medinotify.data.local.toEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneOffset

class MedicineRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val medicineDao: MedicineDao,
    private val scheduleDao: ScheduleDao,
    private val logEntryDao: LogEntryDao
) {
    private val userId: String?
        get() = auth.currentUser?.uid

    // =========================================================================
    // I. CÁC HÀM ĐỌC DỮ LIỆU (READ OPERATIONS)
    // Luôn lấy dữ liệu từ Room (Single Source of Truth).
    // Giao diện sẽ lắng nghe các Flow này.
    // =========================================================================

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    fun getCurrentUserId(): String? {
        return userId
    }
    fun getAllMedicines(): Flow<List<Medicine>> {
        return medicineDao.getAllMedicines(userId ?: "").map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getMedicineById(medicineId: String): Medicine? {
        // Lấy trực tiếp từ DAO, không cần thông qua Flow
        return medicineDao.getMedicineById(medicineId)?.toDomainModel()
    }

    fun getSchedulesForDate(date: LocalDate): Flow<List<Schedule>> {
        val startOfDay = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
        val endOfDay = date.plusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000 - 1

        return scheduleDao.getSchedulesByDateRange(userId ?: "", startOfDay, endOfDay).map { scheduleEntityList ->
            scheduleEntityList.map { it.toDomainModel() }
        }
    }

    fun getLogHistoryForDateRange(dateStart: Long, dateEnd: Long): Flow<List<LogEntry>> {
        // ✅ SỬA 1: Truyền `userId` vào làm tham số đầu tiên.
        // Hàm getLogEntriesByDateRange trong DAO yêu cầu 3 tham số: (userId, dateStart, dateEnd).
        return logEntryDao.getLogEntriesByDateRange(userId ?: "", dateStart, dateEnd).map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }

    // =========================================================================
    // II. CÁC HÀM GHI DỮ LIỆU (WRITE OPERATIONS)
    // Thực hiện trên cả Firebase và Room.
    // =========================================================================

    fun signOut() {
        auth.signOut()
    }

    /**
     * ✅ HÀM THÊM THUỐC HOÀN CHỈNH
     * Thêm thuốc và lịch trình vào cả Firebase và Room.
     */
    suspend fun addMedicine(medicine: Medicine, schedules: List<Schedule>) {
        val currentUserId = userId ?: throw IllegalStateException("User not logged in.")

        withContext(Dispatchers.IO) {
            // Bước 1: Ghi lên Firebase
            val medicineRef = firestore.collection("users").document(currentUserId)
                .collection("medicines").document(medicine.medicineId)
            medicineRef.set(medicine).await()

            schedules.forEach { schedule ->
                val scheduleRef = firestore.collection("users").document(currentUserId)
                    .collection("schedules").document(schedule.scheduleId)
                scheduleRef.set(schedule).await()
            }

            // 🔥 Bước 2: Ghi vào Room để cập nhật giao diện ngay lập tức
            medicineDao.insertMedicine(medicine.toEntity(currentUserId))
            scheduleDao.insertSchedules(schedules.map { it.toEntity(currentUserId) })
        }
    }

    suspend fun deleteMedicine(medicineId: String) {
        val currentUserId = userId ?: throw IllegalStateException("User not logged in.")

        withContext(Dispatchers.IO) {
            // Bước 1: Xóa trên Firebase trước
            firestore.collection("users").document(currentUserId)
                .collection("medicines").document(medicineId).delete().await()
            // (Bạn cũng nên có logic xóa schedules và logs liên quan trên Firebase)

            // Bước 2: Xóa trên Room
            // Thao tác này sẽ tự động cập nhật UI qua Flow
            // ✅ SỬA 2: Truyền `currentUserId` vào các hàm xóa để đảm bảo chỉ xóa đúng dữ liệu của người dùng.
            scheduleDao.deleteSchedulesByMedicineId(medicineId, currentUserId)
            logEntryDao.deleteLogsForMedicine(medicineId, currentUserId)
            medicineDao.deleteMedicineById(medicineId)
        }
    }

    suspend fun recordMedicineIntake(logEntry: LogEntry) {
        val currentUserId = userId ?: throw IllegalStateException("User not logged in.")

        withContext(Dispatchers.IO) {
            val medicine = getMedicineById(logEntry.medicineId)
            val medicineName = medicine?.name ?: "Unknown"

            // Ghi lên Firebase (bạn có thể phát triển logic này)
            // firestore.collection("users")...

            // Ghi vào Room để cập nhật UI
            logEntryDao.insertLogEntry(logEntry.toEntity(currentUserId, medicineName))
        }
    }

    // =========================================================================
    // III. ĐỒNG BỘ DỮ LIỆU TỪ FIREBASE (Khi khởi động app)
    // =========================================================================

    suspend fun syncDataFromFirebase() {
        val currentUserId = userId ?: return
        Log.d("Repository", "Starting sync for user: $currentUserId")
        withContext(Dispatchers.IO) {
            try {
                // Đồng bộ Medicines
                val medicinesSnapshot = firestore.collection("users").document(currentUserId)
                    .collection("medicines").get().await()
                val firestoreMedicines = medicinesSnapshot.documents.mapNotNull { it.toObject<Medicine>() }
                medicineDao.insertMedicines(firestoreMedicines.map { it.toEntity(currentUserId) })
                Log.d("Repository", "Synced ${firestoreMedicines.size} medicines.")

                // Đồng bộ Schedules
                val schedulesSnapshot = firestore.collection("users").document(currentUserId)
                    .collection("schedules").get().await()
                val firestoreSchedules = schedulesSnapshot.documents.mapNotNull { it.toObject<Schedule>() }
                scheduleDao.insertSchedules(firestoreSchedules.map { it.toEntity(currentUserId) })
                Log.d("Repository", "Synced ${firestoreSchedules.size} schedules.")

                // (Tương tự cho LogEntries nếu cần)

            } catch (e: Exception) {
                Log.e("Repository", "Error syncing data from Firebase", e)
            }
        }
    }
}
