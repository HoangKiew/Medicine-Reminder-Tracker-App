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
// ✅ SỬA 1: Không cần import ViewModel nữa, lớp UI Model sẽ ở file riêng
// import com.example.medinotify.ui.screens.history.HistoryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineHistoryDetailScreen(
    navController: NavController,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val context = LocalContext.current

    // Lắng nghe trạng thái từ ViewModel
    val searchQuery by viewModel.searchQuery.collectAsState()
    val historyList by viewModel.filteredHistory.collectAsState()
    // ✅ SỬA 2: Sử dụng 'selectedDate' với kiểu LocalDate
    val selectedDate by viewModel.selectedDate.collectAsState()

    // Định dạng ngày hiển thị từ LocalDate
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()) }
    val displayedDate = remember(selectedDate) {
        selectedDate.format(dateFormatter)
    }

    // Logic DatePicker sử dụng LocalDate
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            // ✅ SỬA 3: Tạo đối tượng LocalDate mới và gọi ViewModel
            val newDate = LocalDate.of(year, month + 1, dayOfMonth) // month trong DatePicker bắt đầu từ 0
            viewModel.onDateSelected(newDate)
        },
        selectedDate.year,
        selectedDate.monthValue - 1, // month trong DatePicker bắt đầu từ 0
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
            // Top Bar (Giữ nguyên)
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
                    modifier = Modifier.size(28.dp)
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

            // Title
            Text(
                "Lịch sử uống thuốc",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C60FF),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date display với DatePicker Logic
            OutlinedTextField(
                value = displayedDate,
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { datePickerDialog.show() }, // Mở DatePicker khi nhấn
                placeholder = { Text("dd/mm/yyyy") },
                trailingIcon = {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Chọn ngày",
                        modifier = Modifier.clickable { datePickerDialog.show() }
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF2C60FF),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                readOnly = true,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search by medicine name
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = { Text("Tìm kiếm theo tên thuốc") },
                trailingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF2C60FF),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Medicine list with status
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                if (historyList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Không có dữ liệu cho ngày này",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    items(historyList, key = { it.id }) { medicine -> // Thêm key để tối ưu
                        MedicineHistoryItemWithStatus(medicine = medicine)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Back to home button
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2C60FF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Quay lại", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

// Composable cho từng item (giữ nguyên)
@Composable
fun MedicineHistoryItemWithStatus(medicine: MedicineHistoryUi) {
    val backgroundColor = if (medicine.isTaken) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp).background(Color(0xFFFFC700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💊", fontSize = 24.sp)
                }
                Column {
                    Text(
                        text = medicine.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = medicine.dosage,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
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
