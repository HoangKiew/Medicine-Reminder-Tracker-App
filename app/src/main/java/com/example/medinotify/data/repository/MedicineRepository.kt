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
import kotlinx.coroutines.flow.first // Import cần thiết cho việc lấy dữ liệu từ Flow

class MedicineRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val medicineDao: MedicineDao,
    private val scheduleDao: ScheduleDao,
    private val logEntryDao: LogEntryDao
) {
    private val userId: String?
        get() = auth.currentUser?.uid

    // Định dạng giờ chuẩn (đã được định nghĩa trong ViewModel, nhưng cần dùng ở đây nếu cần chuyển đổi)
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

    suspend fun getMedicineById(medicineId: String): Medicine? {
        return medicineDao.getMedicineById(medicineId)?.toDomainModel()
    }

    fun getAllSchedules(): Flow<List<Schedule>> {
        return scheduleDao.getAllSchedules(userId ?: "").map { scheduleEntityList ->
            scheduleEntityList.map { it.toDomainModel() }
        }
    }

    /**
     * Lấy Schedules theo Medicine ID. Dùng .first() ở ViewModel để lấy giá trị suspend.
     */
    fun getSchedulesForMedicine(medicineId: String): Flow<List<Schedule>> {
        // Lấy tất cả schedules của user và lọc trên client (Flow)
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
                // Xóa dữ liệu local khi đăng xuất để chuẩn bị cho tài khoản mới
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

            // Bước 2: Ghi vào Room
            medicineDao.insertMedicine(medicine.toEntity(currentUserId))
            scheduleDao.insertSchedules(schedules.map { it.toEntity(currentUserId) })
        }
    }

    suspend fun updateMedicine(medicine: Medicine, newSchedules: List<Schedule>) {
        val currentUserId = userId ?: throw IllegalStateException("User not logged in.")

        withContext(Dispatchers.IO) {
            // 1. CẬP NHẬT FIREBASE
            firestore.collection("users").document(currentUserId)
                .collection("medicines").document(medicine.medicineId)
                .set(medicine).await()

            // B. Xóa lịch cũ trên Firebase (Rất quan trọng khi cập nhật giờ uống)
            val oldSchedulesSnapshot = firestore.collection("users").document(currentUserId)
                .collection("schedules")
                .whereEqualTo("medicineId", medicine.medicineId)
                .get().await()
            for (doc in oldSchedulesSnapshot.documents) {
                doc.reference.delete()
            }

            // C. Thêm lịch mới lên Firebase
            newSchedules.forEach { schedule ->
                val scheduleRef = firestore.collection("users").document(currentUserId)
                    .collection("schedules").document(schedule.scheduleId)
                scheduleRef.set(schedule).await()
            }

            // 2. CẬP NHẬT LOCAL ROOM
            medicineDao.updateMedicine(medicine.toEntity(currentUserId))
            scheduleDao.deleteSchedulesByMedicineId(medicine.medicineId) // Xóa lịch cũ trong Room
            scheduleDao.insertSchedules(newSchedules.map { it.toEntity(currentUserId) }) // Thêm lịch mới
        }
    }

    suspend fun deleteMedicine(medicineId: String) {
        val currentUserId = userId ?: return

        withContext(Dispatchers.IO) {
            try {
                // 1. Xóa trên Firebase (Thuốc & Lịch)
                firestore.collection("users").document(currentUserId)
                    .collection("medicines").document(medicineId).delete().await()

                val schedulesSnapshot = firestore.collection("users").document(currentUserId)
                    .collection("schedules")
                    .whereEqualTo("medicineId", medicineId).get().await()
                for (doc in schedulesSnapshot.documents) {
                    doc.reference.delete()
                }

                // 2. Xóa trong Local DB (Room)
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
            // Chuyển LocalTime thành String (HH:mm) để tham chiếu
            val timeString = time.format(timeFormatter)

            // 1. Cập nhật Room
            scheduleDao.updateScheduleStatus(medicineId, timeString, status)

            // 2. Cập nhật Firebase
            try {
                // Query Firebase dựa trên medicineId và specificTimeStr
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
                // 1. XÓA DỮ LIỆU LOCAL CŨ (quan trọng để tránh trùng lặp sau đồng bộ)
                medicineDao.clearAllMedicines()
                scheduleDao.clearAllSchedules()
                logEntryDao.clearAllLogs()
                Log.d("Repository", "Local data cleared successfully.")

                // 2. Đồng bộ Medicines
                val medicinesSnapshot = firestore.collection("users").document(currentUserId)
                    .collection("medicines").get().await()

                val firestoreMedicines = medicinesSnapshot.documents.mapNotNull { doc ->
                    // toObject<Medicine>() sẽ thành công nhờ việc chuyển val -> var và @JvmOverloads
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

                // Lặp qua Log Entries, tìm tên thuốc, và chèn vào Room
                val firestoreLogEntities = firestoreLogs.mapNotNull { logEntry ->
                    // Lấy lại medicine name từ Room (sau khi đã đồng bộ)
                    val medicineEntity = medicineDao.getMedicineById(logEntry.medicineId)
                    val medicineName = medicineEntity?.name ?: "Unknown Medicine"
                    logEntry.toEntity(currentUserId, medicineName)
                }

                logEntryDao.insertAll(firestoreLogEntities)
                Log.d("Repository", "Synced ${firestoreLogEntities.size} log entries.")

            } catch (e: Exception) {
                // Báo cáo lỗi nghiêm trọng (ví dụ: mất kết nối, lỗi permissions)
                Log.e("Repository", "Critical error during Firebase sync process: ${e.message}", e)
            }
        }
    }
}