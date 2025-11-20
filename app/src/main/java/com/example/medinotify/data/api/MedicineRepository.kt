package com.example.medinotify.data.api

import retrofit2.Response

class MedicineRepository(
    private val api: ApiService = ApiClient.api
) {

    /* ------------------ MEDICINE ------------------ */

    suspend fun getMedicines(userId: String): Response<MedicineListResponse> {
        return api.getMedicines(userId = userId)
    }

    suspend fun getScheduleByDate(userId: String, date: String): Response<LogEntryResponse> {
        return api.getScheduleByDate(
            userId = userId,
            date = date
        )
    }

    /* ------------------ CRUD MEDICINE ------------------ */

    suspend fun addMedicine(dto: MedicineDTO): Response<ApiResponse> {
        return api.addMedicine(dto)
    }

    suspend fun deleteMedicine(id: String): Response<ApiResponse> {
        return api.deleteMedicine(medicineId = id)
    }


    /* ------------------ LOG ENTRY ACTIONS ------------------ */

    // ⭐ ĐÃ UỐNG
    suspend fun markTaken(logId: String): Response<ActionResponse> {
        return api.markTaken(
            body = mapOf(
                "logId" to logId
            )
        )
    }

    // ⭐ LÁT NỮA (X phút)
    suspend fun markLater(logId: String, minutes: Int): Response<ActionResponse> {
        return api.markLater(
            body = mapOf(
                "logId" to logId,
                "minutes" to minutes.toString()   // 🔥 FIX: phải gửi String
            )
        )
    }

    // ⭐ BỎ QUA
    suspend fun markMissed(logId: String): Response<ActionResponse> {
        return api.markMissed(
            body = mapOf(
                "logId" to logId
            )
        )
    }


    /* ------------------ LOG DETAIL ------------------ */

    suspend fun getLogDetail(logId: String): Response<LogDetailResponse> {
        return api.getLogDetail(logId = logId)
    }
}
