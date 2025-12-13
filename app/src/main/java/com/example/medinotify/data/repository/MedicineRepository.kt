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
import java.time.ZoneOffset
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

    // ✨✨✨ ĐÃ SỬA HÀM NÀY ✨✨✨
    // Thay vì lọc theo khoảng thời gian (dễ bị mất thuốc nếu quá giờ),
    // ta lấy TOÀN BỘ lịch trình vì đây là thuốc uống hàng ngày.
    fun getSchedulesForDate(date: LocalDate): Flow<List<Schedule>> {
        // Logic cũ (Bị lỗi ẩn thuốc):
        // val startOfDay = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
        // val endOfDay = date.plusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000 - 1
        // return scheduleDao.getSchedulesByDateRange(...)

        // Logic Mới (Hiện tất cả thuốc hàng ngày):
        // ⚠️ Lưu ý: Đảm bảo bạn đã thêm hàm getAllSchedules vào ScheduleDao như hướng dẫn trước đó
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

    // ✨✨✨ SỬA: Hàm signOut chuẩn để xóa sạch dữ liệu ✨✨✨
    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            try {
                // 1. Xóa sạch dữ liệu trong máy trước khi thoát
                scheduleDao.clearAllSchedules()
                medicineDao.clearAllMedicines()
                logEntryDao.clearAllLogs()
                Log.d("Repository", "✅ Đã dọn sạch dữ liệu cũ trong máy")
            } catch (e: Exception) {
                Log.e("Repository", "Lỗi khi dọn dữ liệu: ${e.message}")
                e.printStackTrace()
            }
        }
        // 2. Sau đó mới đăng xuất Firebase
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

    suspend fun deleteMedicine(medicineId: String) {
        val currentUserId = userId ?: throw IllegalStateException("User not logged in.")

        withContext(Dispatchers.IO) {
            firestore.collection("users").document(currentUserId)
                .collection("medicines").document(medicineId).delete().await()

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

            logEntryDao.insertLogEntry(logEntry.toEntity(currentUserId, medicineName))
        }
    }

    suspend fun updateScheduleStatus(medicineId: String, time: LocalTime, status: Boolean) {
        val currentUserId = userId ?: return

        withContext(Dispatchers.IO) {
            // 1. Cập nhật vào Room
            scheduleDao.updateScheduleStatus(medicineId, time, status)

            // 2. Cập nhật Firebase
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

    // =========================================================================
    // III. ĐỒNG BỘ DỮ LIỆU TỪ FIREBASE
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

                // Đồng bộ Schedules
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