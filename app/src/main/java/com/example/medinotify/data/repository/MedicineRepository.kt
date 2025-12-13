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
// Import các Entity để sử dụng trong Firebase Mapping
import com.example.medinotify.data.model.MedicineEntity
import com.example.medinotify.data.model.ScheduleEntity
import com.example.medinotify.data.model.LogEntryEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MedicineRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val medicineDao: MedicineDao,
    private val scheduleDao: ScheduleDao,
    private val logEntryDao: LogEntryDao
) {
    // Vẫn cần giữ getter này để biết ID của người dùng hiện tại
    private val userId: String?
        get() = auth.currentUser?.uid

    // Reference cho Log Entries
    private val logCollection
        get() = firestore.collection("users").document(userId!!).collection("log_entries")

    private val medicineCollection
        get() = firestore.collection("users").document(userId!!).collection("medicines")

    private val schedulesCollection
        get() = firestore.collection("users").document(userId!!).collection("schedules")

    // =========================================================================
    // I. CÁC HÀM ĐỌC DỮ LIỆU (READ OPERATIONS)
    // =========================================================================

    fun getAllMedicines(): Flow<List<Medicine>> {
        return medicineDao.getAllMedicines(userId ?: "").map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getMedicineById(medicineId: String): Medicine? {
        return medicineDao.getMedicineById(medicineId)?.toDomainModel()
    }

    /**
     * ✅ ĐÃ SỬA: Hàm tiện ích để lấy ID người dùng hiện tại cho các ViewModel
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getSchedulesForDate(date: LocalDate): Flow<List<Schedule>> {
        val zoneId = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfNextDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = endOfNextDay - 1

        Log.d("Repository", "Fetching schedules for $date. Start: $startOfDay, End: $endOfDay (using $zoneId)")

        // Giả định getSchedulesByDateRange lọc theo userId
        return scheduleDao.getSchedulesByDateRange(userId ?: "", startOfDay, endOfDay).map { scheduleEntityList ->
            scheduleEntityList.map { it.toDomainModel() }
        }
    }

    fun getLogHistoryForDateRange(dateStart: Long, dateEnd: Long): Flow<List<LogEntry>> {
        // Giả định getLogEntriesByDateRange lọc theo userId
        return logEntryDao.getLogEntriesByDateRange(userId ?: "", dateStart, dateEnd).map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }

    fun getLogEntriesForDate(date: LocalDate): Flow<List<LogEntry>> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Giả định getLogEntriesBetween lọc theo userId
        return logEntryDao.getLogEntriesBetween(startOfDay, endOfDay, userId ?: "")
            .map { entities ->
                entities.map { it.toDomainModel() }
            }
    }

    suspend fun getMedicineNameMap(): Map<String, String> {
        return medicineDao.getAllMedicinesList(userId ?: "").associate { it.medicineId to it.name }
    }

    suspend fun getMedicineDosageMap(): Map<String, String> {
        return medicineDao.getAllMedicinesList(userId ?: "").associate { it.medicineId to it.dosage }
    }

    // =========================================================================
    // II. CÁC HÀM GHI DỮ LIỆU (WRITE OPERATIONS)
    // =========================================================================

    /**
     * Hàm này lưu Medicine và Schedules vào cả Firebase và Room.
     */
    suspend fun addMedicineAndSchedules(medicine: Medicine, schedules: List<Schedule>) {
        val currentUserId = userId ?: throw IllegalStateException("User not logged in")

        withContext(Dispatchers.IO) {
            // ... (Logic tạo Entity và Data Map giữ nguyên) ...

            // --- 2. LƯU VÀO ROOM DATABASE TRƯỚC ---
            val medicineEntity = medicine.toEntity(currentUserId)
            val scheduleEntities = schedules.map { it.toEntity(currentUserId) }

            medicineDao.insertMedicine(medicineEntity)
            scheduleDao.insertSchedules(scheduleEntities)
            Log.d("Repository", "Saved to Room: Medicine ${medicine.medicineId} and ${scheduleEntities.size} schedules.")


            // --- 3. LƯU LÊN FIREBASE SỬ DỤNG BATCH WRITE ---
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm") // Khởi tạo lại TimeFormatter
            val userDocRef = firestore.collection("users").document(currentUserId)
            val medicineDocRef = userDocRef.collection("medicines").document(medicine.medicineId)
            val schedulesCollectionRef = userDocRef.collection("schedules")
            val batch = firestore.batch()

            // Tạo medicineData (cần định nghĩa lại ở đây hoặc chuyển ra ngoài scope)
            val medicineData = mapOf(
                "medicineId" to medicine.medicineId,
                "name" to medicine.name,
                "dosage" to medicine.dosage,
                "type" to medicine.type,
                "quantity" to medicine.quantity,
                "notes" to medicine.notes,
                "isActive" to medicine.isActive,
                "frequency" to medicine.frequency.name,
                "intervalDays" to medicine.intervalDays,
                "daysOfWeek" to medicine.daysOfWeek.map { it.name }
            )
            batch.set(medicineDocRef, medicineData)

            schedules.forEach { schedule ->
                val specificTimeString = schedule.specificTime.format(timeFormatter)
                val scheduleData = mapOf(
                    "scheduleId" to schedule.scheduleId,
                    "medicineId" to schedule.medicineId,
                    "specificTime" to specificTimeString,
                    "nextScheduledTimestamp" to schedule.nextScheduledTimestamp,
                    "reminderStatus" to schedule.reminderStatus
                )
                val scheduleDocRef = schedulesCollectionRef.document(schedule.scheduleId)
                batch.set(scheduleDocRef, scheduleData)
            }

            try {
                batch.commit().await()
                Log.d("Repository", "Successfully added medicine and schedules to Firebase.")
            } catch (e: Exception) {
                Log.e("Repository", "Error adding medicine to Firebase. Attempting rollback...", e)
                // Giữ nguyên TODO: Cân nhắc thêm logic Rollback
            }
        }
    }

    suspend fun deleteMedicine(medicineId: String) {
        val currentUserId = userId ?: throw IllegalStateException("User not logged in.")
        // ... (Logic delete giữ nguyên) ...
        withContext(Dispatchers.IO) {
            // Xóa khỏi Firebase
            firestore.collection("users").document(currentUserId)
                .collection("medicines").document(medicineId).delete().await()
            firestore.collection("users").document(currentUserId)
                .collection("schedules").whereEqualTo("medicineId", medicineId).get().await()
                .documents.forEach { it.reference.delete() }
            firestore.collection("users").document(currentUserId)
                .collection("log_entries").whereEqualTo("medicineId", medicineId).get().await()
                .documents.forEach { it.reference.delete() }


            // Xóa khỏi Room
            scheduleDao.deleteSchedulesByMedicineId(medicineId, currentUserId)
            logEntryDao.deleteLogsForMedicine(medicineId, currentUserId)
            medicineDao.deleteMedicineById(medicineId)
            Log.d("Repository", "Deleted medicine $medicineId from Room and Firebase.")
        }
    }

    /**
     * Ghi lại một lần uống thuốc đã được xác nhận và đồng bộ lên Firebase.
     */
    suspend fun recordMedicineIntake(logEntry: LogEntry) {
        val currentUserId = userId ?: throw IllegalStateException("User not logged in.")
        // ... (Logic record giữ nguyên) ...
        withContext(Dispatchers.IO) {
            val medicine = getMedicineById(logEntry.medicineId)
            val medicineName = medicine?.name ?: "Unknown"

            val logEntity = logEntry.toEntity(currentUserId, medicineName)

            // 1. Ghi Log Entry vào Room
            logEntryDao.insertLogEntry(logEntity)
            Log.d("Repository", "Recorded intake for ${logEntry.medicineId}.")

            // 2. Ghi Log Entry lên Firebase
            logCollection.document(logEntry.logId).set(logEntity).await()
            Log.d("Repository", "Synced Log Entry ${logEntry.logId} to Firebase.")
        }
    }

    // =========================================================================
    // III. ĐỒNG BỘ DỮ LIỆU VÀ XỬ LÝ PHIÊN (SESSION)
    // =========================================================================

    /**
     * Hàm này chịu trách nhiệm đồng bộ dữ liệu Medicine, Schedule và Log Entries từ Firebase vào Room.
     * ✅ ĐÃ SỬA: Thêm try/catch an toàn trong quá trình deserialize để tránh thất bại đồng bộ toàn bộ.
     */
    suspend fun syncDataFromFirebase() {
        val currentUserId = userId ?: return
        Log.d("Repository", "Starting sync for user: $currentUserId")
        withContext(Dispatchers.IO) {
            try {
                // Xóa dữ liệu cũ trong Room để đồng bộ sạch (Clear before sync)
                medicineDao.clearAll()
                scheduleDao.clearAll()
                logEntryDao.clearAll()

                // 1. Đồng bộ Medicines
                val medicinesSnapshot = medicineCollection.get().await()
                val firestoreMedicineEntities = medicinesSnapshot.documents
                    .mapNotNull { doc ->
                        try {
                            doc.toObject<MedicineEntity>()?.also { entity ->
                                // Đảm bảo userId được set, đề phòng trường hợp Firebase data cũ thiếu
                                entity.userId = currentUserId
                            }
                        } catch (e: Exception) {
                            Log.e("Repository", "Failed to deserialize Medicine: ${doc.id}", e)
                            null
                        }
                    }
                medicineDao.insertMedicines(firestoreMedicineEntities)
                Log.d("Repository", "Synced ${firestoreMedicineEntities.size} medicines.")

                // 2. Đồng bộ Schedules
                val schedulesSnapshot = schedulesCollection.get().await()
                val firestoreScheduleEntities = schedulesSnapshot.documents
                    .mapNotNull { doc ->
                        try {
                            doc.toObject<ScheduleEntity>()?.also { entity ->
                                entity.userId = currentUserId
                            }
                        } catch (e: Exception) {
                            Log.e("Repository", "Failed to deserialize Schedule: ${doc.id}", e)
                            null
                        }
                    }
                scheduleDao.insertSchedules(firestoreScheduleEntities)
                Log.d("Repository", "Synced ${firestoreScheduleEntities.size} schedules.")

                // 3. Đồng bộ Log Entries
                val logSnapshot = logCollection.get().await()
                val firestoreLogEntities = logSnapshot.documents
                    .mapNotNull { doc ->
                        try {
                            doc.toObject<LogEntryEntity>()?.also { entity ->
                                entity.userId = currentUserId
                            }
                        } catch (e: Exception) {
                            Log.e("Repository", "Failed to deserialize Log Entry: ${doc.id}", e)
                            null
                        }
                    }
                logEntryDao.insertAll(firestoreLogEntities)
                Log.d("Repository", "Synced ${firestoreLogEntities.size} log entries.")

            } catch (e: Exception) {
                // Khối catch này chỉ bắt các lỗi truy cập Firebase chung (ví dụ: lỗi mạng, quyền truy cập)
                Log.e("Repository", "Critical error during Firebase sync process.", e)
            }
        }
    }

    /**
     * HÀM QUAN TRỌNG: Xóa tất cả dữ liệu cục bộ của người dùng hiện tại (sử dụng khi đăng xuất).
     * Hàm này đảm bảo tính bảo mật và nhất quán.
     */
    suspend fun clearLocalData() = withContext(Dispatchers.IO) {
        Log.d("Repository", "Clearing all local data for user: $userId")
        // Xóa tất cả dữ liệu khỏi Room Database
        medicineDao.clearAll()
        scheduleDao.clearAll()
        logEntryDao.clearAll()
    }
}