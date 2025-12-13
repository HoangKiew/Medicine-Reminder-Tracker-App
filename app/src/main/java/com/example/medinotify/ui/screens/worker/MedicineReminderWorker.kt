package com.example.medinotify.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.medinotify.MainActivity
import com.example.medinotify.R

class MedicineReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    private val CHANNEL_ID = "medicine_reminder_channel"
    private val TAG = "MedicineWorker"

    override fun doWork(): Result {
        Log.d(TAG, "🟢 Worker bắt đầu chạy (doWork called)")

        val medicineId = inputData.getString("MEDICINE_ID")
        val medicineName = inputData.getString("MEDICINE_NAME") ?: "Thuốc"
        val dosage = inputData.getString("MEDICINE_DOSAGE") ?: ""
        val scheduleTime = inputData.getString("SCHEDULE_TIME") ?: ""

        Log.d(TAG, "📥 Dữ liệu nhận được: ID=$medicineId, Tên=$medicineName, Giờ=$scheduleTime")

        if (medicineId == null) {
            Log.e(TAG, "🔴 Thất bại: Không tìm thấy MEDICINE_ID")
            return Result.failure()
        }

        try {
            triggerNotification(medicineId, medicineName, dosage, scheduleTime)
            Log.d(TAG, "✅ doWork hoàn tất thành công")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "🔴 Lỗi trong quá trình tạo thông báo: ${e.message}")
            e.printStackTrace()
            return Result.failure()
        }
    }

    private fun triggerNotification(id: String, name: String, dosage: String, time: String) {
        Log.d(TAG, "🔔 Đang tạo thông báo cho: $name")

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tạo Channel cho Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nhắc nhở uống thuốc",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo nhắc nhở lịch uống thuốc"
                enableVibration(true) // Bật rung
            }
            notificationManager.createNotificationChannel(channel)
        }

        // --- CẤU HÌNH INTENT ĐỂ MỞ APP ---
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            // ✨ QUAN TRỌNG: Đã sửa dòng này
            // FLAG_ACTIVITY_NEW_TASK: Bắt buộc khi gọi từ Worker
            // FLAG_ACTIVITY_SINGLE_TOP: Nếu app đang mở, không kill app mà chỉ gọi onNewIntent
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP

            putExtra("NAVIGATE_TO", "reminder_screen")
            putExtra("MEDICINE_ID", id)
            putExtra("MEDICINE_NAME", name)
            putExtra("MEDICINE_DOSAGE", dosage)
            putExtra("SCHEDULE_TIME", time)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            id.hashCode(), // RequestCode khác nhau để không bị đè Intent
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.splash_screen) // Lưu ý: Nên đổi thành icon trong suốt nếu bị hiện ô vuông trắng
            .setContentTitle("Đến giờ uống thuốc: $name")
            .setContentText("$dosage. Nhấn để xác nhận đã uống.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id.hashCode(), notification)

        Log.d(TAG, "🚀 Đã gọi notify() xong. Kiểm tra thanh thông báo!")
    }
}