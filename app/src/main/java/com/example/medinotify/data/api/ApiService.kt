package com.example.medinotify.data.api

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    /* ------------------ 1) MEDICINES ------------------ */

    @GET("api.php")
    suspend fun getMedicines(
        @Query("action") action: String = "getMedicines",
        @Query("userId") userId: String
    ): Response<MedicineListResponse>

    @POST("api.php?action=addMedicine")
    suspend fun addMedicine(
        @Body body: MedicineDTO
    ): Response<ApiResponse>

    @DELETE("api.php")
    suspend fun deleteMedicine(
        @Query("action") action: String = "deleteMedicine",
        @Query("medicineId") medicineId: String
    ): Response<ApiResponse>



    /* ------------------ 2) SCHEDULE ------------------ */

    @GET("api.php")
    suspend fun getScheduleByDate(
        @Query("action") action: String = "getScheduleByDate",
        @Query("userId") userId: String,
        @Query("date") date: String
    ): Response<LogEntryResponse>



    /* ------------------ 3) MARK TAKEN ------------------ */

    // ⭐ FIX: dùng Map<String, String>
    @POST("api.php?action=markTaken")
    suspend fun markTaken(
        @Body body: Map<String, String>
    ): Response<ActionResponse>



    /* ------------------ 4) MARK LATER ------------------ */

    @POST("api.php?action=markLater")
    suspend fun markLater(
        @Body body: Map<String, String>
    ): Response<ActionResponse>



    /* ------------------ 5) MARK MISSED ------------------ */

    @POST("api.php?action=markMissed")
    suspend fun markMissed(
        @Body body: Map<String, String>
    ): Response<ActionResponse>



    /* ------------------ 6) GET LOG DETAIL ------------------ */

    @GET("api.php")
    suspend fun getLogDetail(
        @Query("action") action: String = "getLogDetail",
        @Query("logId") logId: String
    ): Response<LogDetailResponse>



    /* ------------------ 7) SAVE FCM TOKEN ------------------ */

    @POST("api.php?action=saveToken")
    suspend fun saveToken(
        @Body body: Map<String, String>
    ): Response<ApiResponse>



    /* ------------------ 8) CHECK REMINDER ------------------ */

    @GET("api.php")
    suspend fun checkReminder(
        @Query("action") action: String = "checkReminder",
        @Query("userId") userId: String
    ): Response<ApiResponse>
}
