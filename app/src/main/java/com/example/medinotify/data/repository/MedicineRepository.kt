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
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

    /**
     * Lấy Schedules cho ngày được chọn. Logic múi giờ và khoảng thời gian là chính xác.
     * Nếu lịch Thứ 2 vẫn hiện vào Chủ Nhật, hãy kiểm tra lại code tính toán
     * giá trị 'nextScheduledTimestamp' trong AddMedicineViewModel/ScheduleWorker.
     */
    fun getSchedulesForDate(date: LocalDate): Flow<List<Schedule>> {
        // Lấy múi giờ hệ thống của thiết bị
        val zoneId = ZoneId.systemDefault()

        // 00:00:00 của ngày được chọn (theo giờ địa phương)
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()

        // 00:00:00 của ngày tiếp theo (theo giờ địa phương)
        val endOfNextDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        // Tính mili giây cuối cùng của ngày được chọn (endOfNextDay - 1ms)
        val endOfDay = endOfNextDay - 1

        Log.d("Repository", "Fetching schedules for $date. Start: $startOfDay, End: $endOfDay (using $zoneId)")

        return scheduleDao.getSchedulesByDateRange(userId ?: "", startOfDay, endOfDay).map { scheduleEntityList ->
            scheduleEntityList.map { it.toDomainModel() }
        }
    }

    fun getLogHistoryForDateRange(dateStart: Long, dateEnd: Long): Flow<List<LogEntry>> {
        return logEntryDao.getLogEntriesByDateRange(userId ?: "", dateStart, dateEnd).map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }

    fun getLogEntriesForDate(date: LocalDate): Flow<List<LogEntry>> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

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

    fun signOut() {
        auth.signOut()
    }

    /**
     * Hàm này lưu Medicine và Schedules vào cả Firebase và Room.
     */
    suspend fun addMedicineAndSchedules(medicine: Medicine, schedules: List<Schedule>) {
        val currentUserId = userId ?: throw IllegalStateException("User not logged in")

        withContext(Dispatchers.IO) {
            // --- 1. CHUẨN BỊ DỮ LIỆU ---
            val medicineEntity = medicine.toEntity(currentUserId)
            val scheduleEntities = schedules.map { it.toEntity(currentUserId) }
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

            // Chuẩn bị dữ liệu (dạng Map) để lưu lên Firebase
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

            // --- 2. LƯU VÀO ROOM DATABASE TRƯỚC ---
            medicineDao.insertMedicine(medicineEntity)
            scheduleDao.insertSchedules(scheduleEntities)
            Log.d("Repository", "Saved to Room: Medicine ${medicine.medicineId} and ${scheduleEntities.size} schedules.")


            // --- 3. LƯU LÊN FIREBASE SỬ DỤNG BATCH WRITE ---
            val userDocRef = firestore.collection("users").document(currentUserId)
            val medicineDocRef = userDocRef.collection("medicines").document(medicine.medicineId)
            val schedulesCollectionRef = userDocRef.collection("schedules")

            val batch = firestore.batch()

            batch.set(medicineDocRef, medicineData)

            schedules.forEach { schedule ->
                // ĐÃ SỬA: Định dạng LocalTime thành HH:mm để lưu Firebase an toàn
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
                // TODO: Cân nhắc thêm logic xóa dữ liệu đã lưu trong Room nếu Firebase thất bại (Rollback)
            }
        }
    }

    suspend fun deleteMedicine(medicineId: String) {
        val currentUserId = userId ?: throw IllegalStateException("User not logged in.")

        withContext(Dispatchers.IO) {
            firestore.collection("users").document(currentUserId)
                .collection("medicines").document(medicineId).delete().await()

            // Xóa khỏi Room
            scheduleDao.deleteSchedulesByMedicineId(medicineId, currentUserId)
            logEntryDao.deleteLogsForMedicine(medicineId, currentUserId)
            medicineDao.deleteMedicineById(medicineId)
            Log.d("Repository", "Deleted medicine $medicineId from Room and Firebase.")
        }
    }

    suspend fun recordMedicineIntake(logEntry: LogEntry) {
        val currentUserId = userId ?: throw IllegalStateException("User not logged in.")

        withContext(Dispatchers.IO) {
            val medicine = getMedicineById(logEntry.medicineId)
            val medicineName = medicine?.name ?: "Unknown"

            // Ghi Log Entry vào Room
            logEntryDao.insertLogEntry(logEntry.toEntity(currentUserId, medicineName))
            Log.d("Repository", "Recorded intake for ${logEntry.medicineId}.")
        }
    }

    // =========================================================================
    // III. ĐỒNG BỘ DỮ LIỆU TỪ FIREBASE (Khi khởi động app)
    // =========================================================================

    /**
     * Hàm này chịu trách nhiệm đồng bộ dữ liệu Medicine và Schedule từ Firebase vào Room.
     */
    suspend fun syncDataFromFirebase() {
        val currentUserId = userId ?: return
        Log.d("Repository", "Starting sync for user: $currentUserId")
        withContext(Dispatchers.IO) {
            try {
                // Đồng bộ Medicines
                val medicinesSnapshot = firestore.collection("users").document(currentUserId)
                    .collection("medicines").get().await()

                // Map từ Firebase sang MedicineEntity
                val firestoreMedicineEntities = medicinesSnapshot.documents.mapNotNull { it.toObject<MedicineEntity>() }
                medicineDao.insertMedicines(firestoreMedicineEntities)
                Log.d("Repository", "Synced ${firestoreMedicineEntities.size} medicines.")

                // Đồng bộ Schedules
                val schedulesSnapshot = firestore.collection("users").document(currentUserId)
                    .collection("schedules").get().await()

                // Map từ Firebase sang ScheduleEntity
                val firestoreScheduleEntities = schedulesSnapshot.documents.mapNotNull { it.toObject<ScheduleEntity>() }
                scheduleDao.insertSchedules(firestoreScheduleEntities)
                Log.d("Repository", "Synced ${firestoreScheduleEntities.size} schedules.")

            } catch (e: Exception) {
                Log.e("Repository", "Error syncing data from Firebase", e)
            }
        }
    }
}