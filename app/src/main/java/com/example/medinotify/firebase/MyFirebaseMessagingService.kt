package com.example.medinotify.firebase

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.medinotify.MainActivity
import com.example.medinotify.R
import com.example.medinotify.data.api.ApiClient
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val api = ApiClient.api

    companion object {
        private const val FCM_TAG = "FCMService"
        private const val USER_ID = "U001"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(FCM_TAG, "New FCM Token: $token")
        saveTokenToServer(token)
    }

    private fun saveTokenToServer(token: String) {
        scope.launch {
            try {
                val response = api.saveToken(mapOf("userId" to USER_ID, "token" to token))
                if (!response.isSuccessful) {
                    Log.e(FCM_TAG, "Failed to save token: ${response.errorBody()?.string()}")
                } else {
                    Log.d(FCM_TAG, "Token saved from service")
                }
            } catch (e: Exception) {
                Log.e(FCM_TAG, "Error saving token", e)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(FCM_TAG, "FCM received data: ${remoteMessage.data}")

        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "Nhắc uống thuốc"
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: "Đến giờ uống thuốc!"
        val logId = remoteMessage.data["logId"] ?: ""

        showNotification(title, body, logId)
    }

    private fun showNotification(title: String, body: String, logId: String) {
        val channelId = "medicine_channel"
        val notificationId = System.currentTimeMillis().toInt()

        // Gửi logId sang MainActivity bằng extra
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("logId", logId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            logId.hashCode(), // unique id per logId
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Nhắc nhở uống thuốc",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Thông báo nhắc uống thuốc đúng giờ" }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.medicine_start)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val manager = NotificationManagerCompat.from(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(FCM_TAG, "Missing POST_NOTIFICATIONS permission")
            return
        }

        manager.notify(notificationId, builder.build())
        Log.d(FCM_TAG, "Notification shown with logId: $logId")
    }
}
