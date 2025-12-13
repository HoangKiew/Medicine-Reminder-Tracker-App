package com.example.medinotify.ui.screens.settings.account

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent // ✅ Thêm mới
import android.content.Context
import android.content.Intent // ✅ Thêm mới
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.medinotify.MainActivity // ✅ Import MainActivity để mở app
import com.example.medinotify.R

class StudyReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    private val CHANNEL_ID = "medinotify_reminders"
    private val NOTIFICATION_ID = 101

    override fun doWork(): Result {
        sendNotification(
            applicationContext,
            "Nhắc nhở dùng MediNotify!",
            "Bạn đã không mở ứng dụng trong một thời gian. Hãy kiểm tra lịch thuốc của bạn nhé!"
        )
        return Result.success()
    }

    private fun sendNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Tạo Intent để mở MainActivity khi nhấn thông báo
        val intent = Intent(context, MainActivity::class.java).apply {
            // Cờ này giúp mở lại Activity nếu nó đã tồn tại hoặc tạo mới nếu chưa
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // 2. Tạo PendingIntent (Bắt buộc để xử lý sự kiện click)
        // Lưu ý: FLAG_IMMUTABLE là bắt buộc cho Android 12 (API 31) trở lên để tránh CRASH
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        // 3. Tạo Kênh thông báo (Cho Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nhắc nhở sử dụng",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh thông báo nhắc nhở sử dụng ứng dụng MediNotify."
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 4. Xây dựng thông báo
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.splash_screen) // Đảm bảo icon này tồn tại và phù hợp quy chuẩn (trong suốt)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // ✅ QUAN TRỌNG: Gắn PendingIntent vào đây
            .setAutoCancel(true) // Tự động đóng thông báo khi nhấn vào

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}