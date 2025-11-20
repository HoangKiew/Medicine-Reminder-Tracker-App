package com.example.medinotify.data.api

data class ActionResponse(
    val success: Boolean? = null,
    val affectedRows: Int? = null,
    val logId: String? = null,
    val status: String? = null,
    val newScheduledTime: String? = null,  // dùng cho markLater
    val error: String? = null
)
