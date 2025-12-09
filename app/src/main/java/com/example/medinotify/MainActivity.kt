// MainActivity.kt
package com.example.medinotify

import android.Manifest // Thêm import cho Manifest
import android.content.pm.PackageManager
import android.os.Build // Thêm import cho Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts // Thêm import cho ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.core.content.ContextCompat // Thêm import cho ContextCompat
import com.example.medinotify.ui.navigation.MedinotifyApp

class MainActivity : ComponentActivity() {

    // Khởi tạo ActivityResultLauncher để xử lý kết quả yêu cầu quyền
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Quyền được cấp, có thể gửi thông báo
            } else {
                // Quyền bị từ chối, tính năng thông báo có thể không hoạt động
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✨ GỌI HÀM YÊU CẦU QUYỀN TRƯỚC KHI SET CONTENT ✨
        requestNotificationPermission()

        setContent {
            MaterialTheme {
                MedinotifyApp()
            }
        }
    }

    /**
     * Yêu cầu quyền POST_NOTIFICATIONS nếu thiết bị chạy Android 13 (API 33) trở lên.
     */
    private fun requestNotificationPermission() {
        // Chỉ yêu cầu quyền nếu API >= 33 (Android 13/TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            // 1. Kiểm tra xem quyền đã được cấp chưa
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 2. Nếu chưa cấp, yêu cầu quyền từ người dùng
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}