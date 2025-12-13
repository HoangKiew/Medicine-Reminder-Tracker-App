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
import java.time.LocalTime
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

    fun getSchedulesForDate(date: LocalDate): Flow<List<Schedule>> {
        return scheduleDao.getAllSchedules(userId ?: "").map { scheduleEntityList ->
            scheduleEntityList.map { it.toDomainModel() }
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

    // ✨✨✨ HÀM CẬP NHẬT THUỐC (SỬA THUỐC) - QUAN TRỌNG ✨✨✨
    suspend fun updateMedicine(medicine: Medicine, newSchedules: List<Schedule>) {
        val currentUserId = userId ?: throw IllegalStateException("User not logged in.")

        withContext(Dispatchers.IO) {
            // 1. CẬP NHẬT FIREBASE
            // A. Cập nhật thông tin thuốc (ghi đè)
            firestore.collection("users").document(currentUserId)
                .collection("medicines").document(medicine.medicineId)
                .set(medicine).await()

            // B. Xóa lịch cũ trên Firebase (để tránh trùng lặp giờ cũ)
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
            // A. Cập nhật thuốc
            medicineDao.updateMedicine(medicine.toEntity(currentUserId))

            // B. Xóa lịch cũ trong máy & Thêm lịch mới
            scheduleDao.deleteSchedulesByMedicineId(medicine.medicineId)
            scheduleDao.insertSchedules(newSchedules.map { it.toEntity(currentUserId) })

            Log.d("Repository", "Đã cập nhật thuốc ${medicine.name} thành công")
        }
    }

    // ✨✨✨ HÀM XÓA THUỐC (ĐÃ FIX LỖI USER ID) ✨✨✨
    suspend fun deleteMedicine(medicineId: String) {
        val currentUserId = userId ?: return

        withContext(Dispatchers.IO) {
            try {
                // 1. Xóa thuốc trên Firebase
                firestore.collection("users").document(currentUserId)
                    .collection("medicines").document(medicineId)
                    .delete()
                    .await()

                // 2. Tìm và xóa tất cả lịch liên quan trên Firebase
                val schedulesSnapshot = firestore.collection("users").document(currentUserId)
                    .collection("schedules")
                    .whereEqualTo("medicineId", medicineId)
                    .get()
                    .await()

                for (doc in schedulesSnapshot.documents) {
                    doc.reference.delete()
                }

                // 3. Xóa trong Local DB (Room)
                scheduleDao.deleteSchedulesByMedicineId(medicineId)

                // ✅ Đã thêm currentUserId vào đây
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

            logEntryDao.insertLogEntry(logEntry.toEntity(currentUserId, medicineName))
        }
    }

    suspend fun updateScheduleStatus(medicineId: String, time: LocalTime, status: Boolean) {
        val currentUserId = userId ?: return

        withContext(Dispatchers.IO) {
            scheduleDao.updateScheduleStatus(medicineId, time, status)

            try {
                val timeString = time.format(DateTimeFormatter.ofPattern("HH:mm"))

                val snapshot = firestore.collection("users").document(currentUserId)
                    .collection("schedules")
                    .whereEqualTo("medicineId", medicineId)
                    .whereEqualTo("specificTime", timeString)
                    .get().await()

                for (document in snapshot.documents) {
                    document.reference.update("reminderStatus", status)
                }
            } catch (e: Exception) {
                Log.e("MedicineRepository", "Error updating Firebase status", e)
            }
        }
    }

    suspend fun syncDataFromFirebase() {
        val currentUserId = userId ?: return
        Log.d("Repository", "Starting sync for user: $currentUserId")
        withContext(Dispatchers.IO) {
            try {
                val medicinesSnapshot = firestore.collection("users").document(currentUserId)
                    .collection("medicines").get().await()
                val firestoreMedicines = medicinesSnapshot.documents.mapNotNull { it.toObject<Medicine>() }
                medicineDao.insertMedicines(firestoreMedicines.map { it.toEntity(currentUserId) })

                val schedulesSnapshot = firestore.collection("users").document(currentUserId)
                    .collection("schedules").get().await()
                val firestoreSchedules = schedulesSnapshot.documents.mapNotNull { it.toObject<Schedule>() }
                scheduleDao.insertSchedules(firestoreSchedules.map { it.toEntity(currentUserId) })

            } catch (e: Exception) {
                Log.e("Repository", "Error syncing data from Firebase", e)
            }
        }
    }
}