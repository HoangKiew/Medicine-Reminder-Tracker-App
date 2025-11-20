package com.example.medinotify.ui.screens.medicine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medinotify.R
import com.example.medinotify.data.api.ApiClient
import com.example.medinotify.data.api.LogDetailResponse
import com.example.medinotify.data.api.MedicineRepository
import kotlinx.coroutines.launch

@Composable
fun MedicineReminderScreen(
    logId: String,
    onTake: () -> Unit,
    onLater: () -> Unit,
    onMissed: () -> Unit,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    val repo = remember { MedicineRepository(ApiClient.api) }
    val scope = rememberCoroutineScope()

    var data by remember { mutableStateOf<LogDetailResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(logId) {
        try {
            val res = repo.getLogDetail(logId)
            if (res.isSuccessful) {
                data = res.body()
            } else {
                errorMsg = "Không lấy được thông tin: ${res.errorBody()?.string()}"
            }
        } catch (e: Exception) {
            errorMsg = "Lỗi khi gọi API: ${e.message}"
            e.printStackTrace()
        }
        loading = false
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Nếu lỗi thì hiển thị thông báo và nút quay về
    if (errorMsg != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = errorMsg ?: "Lỗi", color = Color.Red)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBack) { Text("Quay lại") }
        }
        return
    }

    val medicine = data ?: run {
        // không có data — show fallback
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Không tìm thấy thông tin thuốc")
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBack) { Text("Quay lại") }
        }
        return
    }

    val timeOnly = if (medicine.scheduledTime.length >= 16) medicine.scheduledTime.substring(11, 16) else ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(18.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }

        Spacer(Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFFF3F9FF))
                .padding(26.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFE375)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.White)
                    }

                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFBDBD))
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Image(
                    painter = painterResource(id = R.drawable.medicine_start),
                    contentDescription = null,
                    modifier = Modifier.size(85.dp)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    medicine.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C60FF)
                )

                Spacer(Modifier.height(22.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF5E80FF))
                    Spacer(Modifier.width(12.dp))
                    Text("Đã lên lịch vào $timeOnly", fontSize = 15.sp)
                }

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Medication, contentDescription = null, tint = Color(0xFF5E80FF))
                    Spacer(Modifier.width(12.dp))
                    Text(medicine.dosage ?: "Không rõ liều lượng", fontSize = 15.sp)
                }

                Spacer(Modifier.height(32.dp))

                Column(Modifier.fillMaxWidth()) {
                    // ĐÃ UỐNG
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val res = repo.markTaken(logId)
                                    if (res.isSuccessful) {
                                        println("✔ markTaken body: ${res.body()}")
                                        onTake()
                                    } else {
                                        println("❌ markTaken error: ${res.errorBody()?.string()}")
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Đã uống")
                    }

                    Spacer(Modifier.height(12.dp))

                    // LÁT NỮA
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val res = repo.markLater(logId, 5)
                                    if (res.isSuccessful) {
                                        println("✔ markLater body: ${res.body()}")
                                        onLater()
                                    } else {
                                        println("❌ markLater error: ${res.errorBody()?.string()}")
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB9CCFF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Lát nữa (5 phút)", color = Color(0xFF2C60FF))
                    }

                    Spacer(Modifier.height(12.dp))

                    // BỎ QUA
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val res = repo.markMissed(logId)
                                    if (res.isSuccessful) {
                                        println("✔ markMissed body: ${res.body()}")
                                        onMissed()
                                    } else {
                                        println("❌ markMissed error: ${res.errorBody()?.string()}")
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB2B2)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Bỏ qua", color = Color.White)
                    }
                }
            }
        }
    }
}