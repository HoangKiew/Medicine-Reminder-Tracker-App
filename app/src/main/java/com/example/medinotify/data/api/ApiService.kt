package com.example.medinotify.data.api

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    /* ------------------ 1) MEDICINES ------------------ */

    @GET("index.php")  // ĐÃ ĐỔI TỪ api.php → index.php
    suspend fun getMedicines(
        @Query("action") action: String = "getMedicines",
        @Query("userId") userId: String
    ): Response<MedicineListResponse>

    @POST("index.php?action=addMedicine")  // ĐÃ ĐỔI
    suspend fun addMedicine(
        @Body body: MedicineDTO
    ): Response<ApiResponse>

    @DELETE("index.php")  // ĐÃ ĐỔI
    suspend fun deleteMedicine(
        @Query("action") action: String = "deleteMedicine",
        @Query("medicineId") medicineId: String
    ): Response<ApiResponse>



    /* ------------------ 2) SCHEDULE ------------------ */

    @GET("index.php")  // ĐÃ ĐỔI
    suspend fun getScheduleByDate(
        @Query("action") action: String = "getScheduleByDate",
        @Query("userId") userId: String,
        @Query("date") date: String
    ): Response<LogEntryResponse>



    /* ------------------ 3) MARK TAKEN ------------------ */

    @POST("index.php?action=markTaken")  // ĐÃ ĐỔI
    suspend fun markTaken(
        @Body body: Map<String, String>
    ): Response<ActionResponse>



    /* ------------------ 4) MARK LATER ------------------ */

    @POST("index.php?action=markLater")  // ĐÃ ĐỔI
    suspend fun markLater(
        @Body body: Map<String, String>
    ): Response<ActionResponse>



    /* ------------------ 5) MARK MISSED ------------------ */

    @POST("index.php?action=markMissed")  // ĐÃ ĐỔI
    suspend fun markMissed(
        @Body body: Map<String, String>
    ): Response<ActionResponse>



    /* ------------------ 6) GET LOG DETAIL ------------------ */

    @GET("index.php")  // ĐÃ ĐỔI
    suspend fun getLogDetail(
        @Query("action") action: String = "getLogDetail",
        @Query("logId") logId: String
    ): Response<LogDetailResponse>



    /* ------------------ 7) SAVE FCM TOKEN ------------------ */

    @POST("index.php?action=saveToken")  // ĐÃ ĐỔI
    suspend fun saveToken(
        @Body body: Map<String, String>
    ): Response<ApiResponse>



    /* ------------------ 8) CHECK REMINDER ------------------ */

    @GET("index.php")  // ĐÃ ĐỔI
    suspend fun checkReminder(
        @Query("action") action: String = "checkReminder",
        @Query("userId") userId: String
    ): Response<ApiResponse>
}