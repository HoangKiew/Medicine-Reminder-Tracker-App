package com.example.medinotify.ui.screens.settings.account // ✨ PACKAGE MỚI ✨

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
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

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.splash_screen)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}