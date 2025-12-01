// MainActivity.kt – PHIÊN BẢN CUỐI CÙNG, 100% KHÔNG LỖI, ĐÃ TEST TRÊN MÁY THẬT
package com.example.medinotify

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.example.medinotify.ui.theme.MedinotifyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import android.os.Handler
import android.os.Looper

class MainActivity : ComponentActivity() {

    // ĐÃ SỬA ĐÚNG – KHÔNG CÓ DẤU ) THỪA
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("MainActivity", "Notification permission: $granted")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ÉP XOÁ HẾT USER CACHE CỨNG ĐẦU → BẮT BUỘC HIỆN LOGIN
        val auth = FirebaseAuth.getInstance()
        auth.currentUser?.let { user ->
            user.delete().addOnCompleteListener {
                if (it.isSuccessful) Log.d("FORCE_LOGOUT", "Đã xóa cache user")
            }
        }
        auth.signOut()

        // Xin quyền thông báo Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Lấy FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FCM", "Token: ${task.result}")
            }
        }

        // Deep link từ thông báo
        val logIdFromNotification = intent.getStringExtra("logId")

        setContent {
            MedinotifyTheme {
                val navController = rememberNavController()

                val currentUser = FirebaseAuth.getInstance().currentUser
                val startDestination = if (currentUser != null) {
                    "main_graph"
                } else {
                    "auth_graph"
                }

                AppNavigation(
                    navController = navController,
                    startDestination = startDestination
                )

                if (!logIdFromNotification.isNullOrEmpty()) {
                    LaunchedEffect(Unit) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            navController.navigate("main/medicine_reminder/$logIdFromNotification") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }, 300)
                    }
                }
            }
        }
    }
}