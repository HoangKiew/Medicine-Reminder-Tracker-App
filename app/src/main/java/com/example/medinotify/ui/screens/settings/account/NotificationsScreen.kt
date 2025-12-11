package com.example.medinotify.ui.screens.settings.account

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.medinotify.R
import com.example.medinotify.ui.screens.settings.account.StudyReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext // Import cần thiết cho Dispatchers.IO
import java.util.concurrent.TimeUnit

// Định nghĩa khóa cho SharedPreferences (để lưu trạng thái)
private const val PREFS_NAME = "MediNotifyPrefs"
private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
private const val WORK_TAG_STUDY_REMINDER = "StudyReminderWorkerTag"

// Lấy SharedPreferences
private fun getPrefs(context: Context): SharedPreferences =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    // Khai báo màu sắc và Context
    val backgroundColor = Color(0xFFF5F5F5)
    val cardColor = Color.White
    val primaryColor = Color(0xFF6395EE)
    val textColor = Color(0xFF2D2D2D)
    val context = LocalContext.current

    // Khởi tạo SharedPreferences object (chưa thực hiện I/O nặng)
    val prefs = remember { getPrefs(context) }

    // TRẠNG THÁI MỚI: Theo dõi trạng thái tải (Loading)
    var isLoadingPrefs by remember { mutableStateOf(true) }

    // TRẠNG THÁI MỚI: Giá trị thông báo (Mặc định là false)
    var isNewNotificationEnabled by remember {
        mutableStateOf(false)
    }

    // KHẮC PHỤC LỖI LAG 5.57s: Đọc SharedPreferences trên Luồng I/O
    LaunchedEffect(key1 = Unit) {
        // Chuyển sang Dispatchers.IO để thực hiện công việc I/O nặng
        withContext(Dispatchers.IO) {
            // Thao tác đọc file I/O xảy ra ở đây
            val isEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)

            // Quay lại Luồng Chính để cập nhật trạng thái Compose
            withContext(Dispatchers.Main) {
                isNewNotificationEnabled = isEnabled
                isLoadingPrefs = false // Đánh dấu đã tải xong
            }
        }
    }
    // Hết Khắc phục lỗi

    var showConfirmationDialog by remember { mutableStateOf(false) }

    val handleToggle: (Boolean) -> Unit = { isEnabled ->
        if (isEnabled) {
            showConfirmationDialog = true
        } else {
            isNewNotificationEnabled = false
            // Ghi dữ liệu: Nên đặt trong Coroutine để đảm bảo tính an toàn (Optional)
            // Hiện tại dùng .apply() vẫn ổn trong trường hợp này.
            prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, false).apply()
            cancelStudyReminder(context)
            Toast.makeText(context, "Đã tắt thông báo nhắc nhở.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            NotificationsTopBar(
                title = "Thông báo",
                textColor = textColor,
                primaryColor = primaryColor,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->

        // KIỂM TRA LOADING: Hiển thị thanh tiến trình nếu đang tải
        if (isLoadingPrefs) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryColor)
            }
        } else {
            // UI Chính chỉ hiển thị khi đã tải xong dữ liệu SharedPreferences
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(paddingValues)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    NotificationToggleItem(
                        title = "Nhận thông báo nhắc nhở",
                        primaryColor = primaryColor,
                        isEnabled = isNewNotificationEnabled,
                        onCheckedChange = handleToggle
                    )
                }
            }
        }
    }

    if (showConfirmationDialog) {
        ConfirmationDialog(
            primaryColor = primaryColor,
            onDismiss = {
                showConfirmationDialog = false
            },
            onConfirm = {
                isNewNotificationEnabled = true
                // Ghi dữ liệu
                prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, true).apply()
                scheduleStudyReminder(context)
                showConfirmationDialog = false
                Toast.makeText(context, "Thông báo nhắc nhở đã được bật.", Toast.LENGTH_SHORT).show()
            },
            titleText = "Xác nhận",
            bodyText = "Bạn có muốn nhận thông báo nhắc nhở nếu không hoạt động trên ứng dụng sau 12 tiếng?"
        )
    }
}

fun scheduleStudyReminder(context: Context) {
    Log.d("NotificationsScreen", "Đang lên lịch thông báo nhắc nhở (12 giờ)...")

    // Thiết lập chu kỳ lặp lại là 12 tiếng
    val workRequest = PeriodicWorkRequestBuilder<StudyReminderWorker>(
        repeatInterval = 12L,
        repeatIntervalTimeUnit = TimeUnit.HOURS
    )
        // Thông báo đầu tiên sau 12 giờ
        .setInitialDelay(12L, TimeUnit.HOURS)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        WORK_TAG_STUDY_REMINDER,
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}

fun cancelStudyReminder(context: Context) {
    Log.d("NotificationsScreen", "Hủy bỏ lịch thông báo nhắc nhở.")
    WorkManager.getInstance(context).cancelUniqueWork(WORK_TAG_STUDY_REMINDER)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsTopBar(
    title: String,
    textColor: Color,
    primaryColor: Color,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "Cài đặt",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C60FF)
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color(0xFF2D2D2D),
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
fun NotificationToggleItem(
    title: String,
    primaryColor: Color,
    isEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val textColor = Color(0xFF2D2D2D)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isEnabled) }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = isEnabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = primaryColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    NotificationsScreen(navController = rememberNavController())
}