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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.first

class MedicineRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val medicineDao: MedicineDao,
    private val scheduleDao: ScheduleDao,
    private val logEntryDao: LogEntryDao
) {
    private val userId: String?
        get() = auth.currentUser?.uid

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // =========================================================================
    // I. CÁC HÀM ĐỌC DỮ LIỆU (READ OPERATIONS)
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

    // ✅ FIX 1: Thêm userId vào hàm DAO để đảm bảo chỉ truy vấn thuốc của người dùng hiện tại
    suspend fun getMedicineById(medicineId: String): Medicine? {
        val currentUserId = userId ?: return null
        return medicineDao.getMedicineById(medicineId, currentUserId)?.toDomainModel() // <-- Truyền userId
    }

    fun getAllSchedules(): Flow<List<Schedule>> {
        return scheduleDao.getAllSchedules(userId ?: "").map { scheduleEntityList ->
            scheduleEntityList.map { it.toDomainModel() }
        }
    }

    fun getSchedulesForMedicine(medicineId: String): Flow<List<Schedule>> {
        return scheduleDao.getAllSchedules(userId ?: "").map { entities ->
            entities.map { it.toDomainModel() }
                .filter { it.medicineId == medicineId }
        }
    }

    fun getLogHistoryForDateRange(dateStart: Long, dateEnd: Long): Flow<List<LogEntry>> {
        return logEntryDao.getLogEntriesByDateRange(userId ?: "", dateStart, dateEnd).map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }

    // =========================================================================
    // II. CÁC HÀM GHI DỮ LIỆU (WRITE OPERATIONS)
    // =========================================================================

    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            try {
                scheduleDao.clearAllSchedules()
                medicineDao.clearAllMedicines()
                logEntryDao.clearAllLogs()
                Log.d("Repository", "✅ Đã dọn sạch dữ liệu cũ trong máy")
            } catch (e: Exception) {
                Log.e("Repository", "Lỗi khi dọn dữ liệu: ${e.message}")
                e.printStackTrace()
            }
        }
        auth.signOut()
    }

    suspend fun addMedicine(medicine: Medicine, schedules: List<Schedule>) {
        val currentUserId = userId ?: throw IllegalStateException("User not logged in.")
        //... (Logic ghi Firebase/Room không thay đổi)
        withContext(Dispatchers.IO) {
            val medicineRef = firestore.collection("users").document(currentUserId)
                .collection("medicines").document(medicine.medicineId)
            medicineRef.set(medicine).await()

            schedules.forEach { schedule ->
                val scheduleRef = firestore.collection("users").document(currentUserId)
                    .collection("schedules").document(schedule.scheduleId)
                scheduleRef.set(schedule).await()
            }

            medicineDao.insertMedicine(medicine.toEntity(currentUserId))
            scheduleDao.insertSchedules(schedules.map { it.toEntity(currentUserId) })
        }
    }

    suspend fun updateMedicine(medicine: Medicine, newSchedules: List<Schedule>) {
        val currentUserId = userId ?: throw IllegalStateException("User not logged in.")
        //... (Logic cập nhật không thay đổi)
        withContext(Dispatchers.IO) {
            firestore.collection("users").document(currentUserId)
                .collection("medicines").document(medicine.medicineId)
                .set(medicine).await()

            val oldSchedulesSnapshot = firestore.collection("users").document(currentUserId)
                .collection("schedules")
                .whereEqualTo("medicineId", medicine.medicineId)
                .get().await()
            for (doc in oldSchedulesSnapshot.documents) {
                doc.reference.delete()
            }

            newSchedules.forEach { schedule ->
                val scheduleRef = firestore.collection("users").document(currentUserId)
                    .collection("schedules").document(schedule.scheduleId)
                scheduleRef.set(schedule).await()
            }

            medicineDao.updateMedicine(medicine.toEntity(currentUserId))
            scheduleDao.deleteSchedulesByMedicineId(medicine.medicineId)
            scheduleDao.insertSchedules(newSchedules.map { it.toEntity(currentUserId) })
        }
    }

    suspend fun deleteMedicine(medicineId: String) {
        val currentUserId = userId ?: return
        //... (Logic xóa không thay đổi)
        withContext(Dispatchers.IO) {
            try {
                firestore.collection("users").document(currentUserId)
                    .collection("medicines").document(medicineId).delete().await()

                val schedulesSnapshot = firestore.collection("users").document(currentUserId)
                    .collection("schedules")
                    .whereEqualTo("medicineId", medicineId).get().await()
                for (doc in schedulesSnapshot.documents) {
                    doc.reference.delete()
                }

                scheduleDao.deleteSchedulesByMedicineId(medicineId)
                logEntryDao.deleteLogsForMedicine(medicineId, currentUserId)
                medicineDao.deleteMedicineById(medicineId)

                Log.d("Repository", "Đã xóa thuốc $medicineId thành công")
            } catch (e: Exception) {
                Log.e("Repository", "Lỗi khi xóa thuốc: ${e.message}")
            }
        }
    }

    suspend fun recordMedicineIntake(logEntry: LogEntry) {
        val currentUserId = userId ?: throw IllegalStateException("User not logged in.")

        withContext(Dispatchers.IO) {
            // ✅ FIX 2: Truyền userId vào getMedicineById() để lấy tên thuốc an toàn
            val medicine = getMedicineById(logEntry.medicineId)
            val medicineName = medicine?.name ?: "Unknown"

            // Ghi vào Firebase (Firestore)
            val logRef = firestore.collection("users").document(currentUserId)
                .collection("log_entries").document()
            logRef.set(logEntry).await()

            // Ghi vào Local DB (Room)
            logEntryDao.insertLogEntry(logEntry.toEntity(currentUserId, medicineName))
        }
    }

    /**
     * Cập nhật trạng thái reminderStatus của Schedule cả trên Room và Firebase.
     */
    suspend fun updateScheduleStatus(medicineId: String, time: LocalTime, status: Boolean) {
        val currentUserId = userId ?: return

        withContext(Dispatchers.IO) {
            val timeString = time.format(timeFormatter)

            // 1. Cập nhật Room
            // ✅ FIX 3: Truyền userId vào DAO để chỉ cập nhật lịch trình của người dùng hiện tại
            scheduleDao.updateScheduleStatus(medicineId, timeString, status, currentUserId)

            // 2. Cập nhật Firebase
            try {
                val snapshot = firestore.collection("users").document(currentUserId)
                    .collection("schedules")
                    .whereEqualTo("medicineId", medicineId)
                    .whereEqualTo("specificTimeStr", timeString)
                    .get().await()

                for (document in snapshot.documents) {
                    document.reference.update("reminderStatus", status)
                }
            } catch (e: Exception) {
                Log.e("MedicineRepository", "Error updating Firebase status", e)
            }
        }
    }

    /**
     * Đồng bộ hóa dữ liệu từ Firebase xuống Room (chạy khi đăng nhập).
     */
    suspend fun syncDataFromFirebase() {
        val currentUserId = userId ?: return
        Log.d("Repository", "Starting sync for user: $currentUserId")

        withContext(Dispatchers.IO) {
            try {
                // ... (Logic xóa dữ liệu cũ và đồng bộ Medicines/Schedules/Logs không thay đổi)
                medicineDao.clearAllMedicines()
                scheduleDao.clearAllSchedules()
                logEntryDao.clearAllLogs()
                Log.d("Repository", "Local data cleared successfully.")

                // 2. Đồng bộ Medicines
                val medicinesSnapshot = firestore.collection("users").document(currentUserId)
                    .collection("medicines").get().await()

                val firestoreMedicines = medicinesSnapshot.documents.mapNotNull { doc ->
                    doc.toObject<Medicine>()?.toEntity(currentUserId)
                }
                medicineDao.insertMedicines(firestoreMedicines)
                Log.d("Repository", "Synced ${firestoreMedicines.size} medicines.")

                // 3. Đồng bộ Schedules
                val schedulesSnapshot = firestore.collection("users").document(currentUserId)
                    .collection("schedules").get().await()
                val firestoreSchedules = schedulesSnapshot.documents.mapNotNull { doc ->
                    doc.toObject<Schedule>()?.toEntity(currentUserId)
                }
                scheduleDao.insertSchedules(firestoreSchedules)
                Log.d("Repository", "Synced ${firestoreSchedules.size} schedules.")

                // 4. Đồng bộ Log Entries
                val logSnapshot = firestore.collection("users").document(currentUserId)
                    .collection("log_entries").get().await()
                val firestoreLogs = logSnapshot.documents.mapNotNull { doc ->
                    doc.toObject<LogEntry>()
                }

                val firestoreLogEntities = firestoreLogs.mapNotNull { logEntry ->
                    // ✅ FIX 4: Truyền userId vào getMedicineById()
                    val medicineEntity = medicineDao.getMedicineById(logEntry.medicineId, currentUserId)
                    val medicineName = medicineEntity?.name ?: "Unknown Medicine"
                    logEntry.toEntity(currentUserId, medicineName)
                }

                logEntryDao.insertAll(firestoreLogEntities)
                Log.d("Repository", "Synced ${firestoreLogEntities.size} log entries.")

            } catch (e: Exception) {
                Log.e("Repository", "Critical error during Firebase sync process: ${e.message}", e)
            }
        }
    }
}