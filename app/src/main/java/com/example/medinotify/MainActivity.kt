package com.example.medinotify

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.rememberNavController
import com.example.medinotify.data.api.ApiClient
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("MainActivity", "Notification permission: $granted")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) xin quyền notification (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // 2) lấy token FCM và gửi lên server (tuỳ chọn)
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("FCMService", "Token from MainActivity: $token")
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val body = mapOf("userId" to "U001", "token" to token)
                        val response = ApiClient.api.saveToken(body)
                        if (response.isSuccessful && response.body()?.success == true) {
                            Log.d("FCMService", "Token saved successfully from MainActivity")
                        } else {
                            Log.e("FCMService", "Failed to save token: ${response.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        Log.e("FCMService", "Error saving token from MainActivity", e)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FCMService", "Failed to get FCM token", e)
            }

        // 3) Nhận logId (nếu mở app bằng notification Intent)
        val logIdFromNotification = intent.getStringExtra("logId")
        Log.d("FCMService", "MainActivity nhận logId: $logIdFromNotification")

        // 4) set UI + điều hướng (nếu có logId)
        setContent {
            val navController = rememberNavController()
            MaterialTheme {
                Navigation(navController = navController)
            }

            if (!logIdFromNotification.isNullOrEmpty()) {
                // delay nhỏ để NavHost khởi tạo xong
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        navController.navigate("medicine_reminder/$logIdFromNotification")
                    } catch (e: Exception) {
                        Log.e("FCMService", "Error navigating to reminder", e)
                    }
                }, 150)
            }
        }
    }
}
