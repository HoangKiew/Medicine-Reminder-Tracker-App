package com.example.medinotify.ui.screens.history

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import com.example.medinotify.data.model.MedicineHistoryUi
import java.time.LocalDate // Sử dụng API mới
import java.time.format.DateTimeFormatter // Sử dụng API mới
import java.util.Locale
import java.time.ZoneId
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineHistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val context = LocalContext.current

    val historyList by viewModel.filteredHistory.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newDate = LocalDate.of(year, month + 1, dayOfMonth)
            viewModel.onDateSelected(newDate)
        },
        selectedDate.year,
        selectedDate.monthValue - 1,
        selectedDate.dayOfMonth
    )

    Scaffold(
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.DateRange,
                    contentDescription = "Calendar",
                    tint = Color(0xFFFF5A5A),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { datePickerDialog.show() }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color(0xFF355CFF),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(18.dp))
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Lịch sử uống thuốc",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C60FF),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Nhóm các item theo ngày
                val groupedByDate = historyList.groupBy {
                    // ✅ SỬA LỖI: Bây giờ `it.intakeTime` đã tồn tại
                    Instant.ofEpochMilli(it.intakeTime).atZone(ZoneId.systemDefault()).toLocalDate()
                }

                if (groupedByDate.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Không có lịch sử uống thuốc.", color = Color.Gray)
                        }
                    }
                } else {
                    // ✅ SỬA LỖI: Chỉ định rõ kiểu dữ liệu cho trình biên dịch
                    groupedByDate.forEach { (date: LocalDate, itemsForDate: List<MedicineHistoryUi>) ->
                        // Header cho mỗi ngày
                        item {
                            val dateText = date.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM, yyyy", Locale("vi")))
                            Text(
                                text = dateText.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("vi")) else it.toString() },
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Danh sách thuốc cho ngày đó
                        items(itemsForDate, key = { it.id }) { medicine ->
                            Box(
                                modifier = Modifier.clickable {
                                    val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                    navController.navigate("medicine_history_detail/$dateString")
                                }
                            ) {
                                MedicineHistoryItem(medicine = medicine)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C60FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Quay lại", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

// Composable cho từng item (Không thay đổi, đã đúng)
@Composable
fun MedicineHistoryItem(medicine: MedicineHistoryUi) {
    val backgroundColor = if (medicine.isTaken) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFFC700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💊", fontSize = 24.sp)
                }
                Column {
                    Text(text = medicine.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(text = medicine.dosage, fontSize = 13.sp, color = Color.Gray)
                }
            }
            Surface(
                color = Color(0xFF2C60FF),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = medicine.time,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
