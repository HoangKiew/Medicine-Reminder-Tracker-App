package com.example.medinotify.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.Calendar
import java.text.DateFormatSymbols

data class CalendarDay(
    val day: Int,
    val isCurrentMonth: Boolean,
    val isSelected: Boolean = false,
    val isToday: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController) {
    // Lấy tháng và năm hiện tại
    val calendar = Calendar.getInstance()
    var currentMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) } // Calendar.MONTH bắt đầu từ 0 (1-12)
    var currentYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedDays by remember { mutableStateOf(setOf(15, 16, 17)) }

    // 🔴 SỬA LỖI 1: Định nghĩa currentMonthYear
    val currentMonthYear by remember {
        derivedStateOf {
            // Lấy tên tháng (tiếng Anh để phù hợp với định dạng "May 2025")
            val monthName = DateFormatSymbols().months[currentMonth - 1]
            "${monthName.replaceFirstChar { it.uppercase() }} $currentYear"
        }
    }

    val medicineList = listOf(
        MedicineCalendarItem("Vitamin D", "1 viên nén, 1000mg", "09:41"),
        MedicineCalendarItem("Viên nang B12", "5 viên, 1000mg", "06:13")
    )

    // --- LOGIC CHUYỂN THÁNG ---
    fun changeMonth(offset: Int) {
        val newMonth = currentMonth + offset
        when {
            newMonth > 12 -> {
                currentMonth = 1
                currentYear += 1
            }
            newMonth < 1 -> {
                currentMonth = 12
                currentYear -= 1
            }
            else -> {
                currentMonth = newMonth
            }
        }
    }
    // -------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top Bar (Không thay đổi)
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
                    tint = Color(0xFF6395EE),
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

        // Calendar Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Month Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { changeMonth(-1) }) { // Logic Previous month
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
                    }

                    Text(
                        text = currentMonthYear, // Đã sửa lỗi Unresolved reference
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    IconButton(onClick = { changeMonth(1) }) { // Logic Next month
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Day Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { day ->
                        Text(
                            text = day,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Calendar Grid
                CalendarGrid(
                    month = currentMonth, // 🔴 SỬA LỖI 2: Thêm tham số month
                    year = currentYear,   // 🔴 SỬA LỖI 2: Thêm tham số year
                    selectedDays = selectedDays,
                    onDayClick = { day ->
                        selectedDays = if (selectedDays.contains(day)) {
                            selectedDays - day
                        } else {
                            selectedDays + day
                        }
                    }
                )
            }
        }

        // Week Range Title (Không thay đổi)
        Text(
            text = "Tuần này",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "May 5 - May 21",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Row {
                IconButton(onClick = { /* Previous week */ }) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous",
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { /* Next week */ }) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Medicine List (Không thay đổi)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            if (medicineList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Không có thuốc",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                items(
                    items = medicineList,
                    key = { it.name }
                ) { medicine ->
                    MedicineCalendarCard(medicine)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                // Thêm spacing cuối để không bị che
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Bottom Buttons (Không thay đổi)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6395EE)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Trang chủ", fontSize = 16.sp, color = Color.White)
            }

            Button(
                onClick = { navController.navigate("medicine_history") },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6395EE)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lịch sử", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

// --- CALENDAR GRID ĐƯỢC CẬP NHẬT ---
@Composable
fun CalendarGrid(
    month: Int,
    year: Int,
    selectedDays: Set<Int>,
    onDayClick: (Int) -> Unit
) {
    // 🔴 LẤY DỮ LIỆU THỰC TẾ DỰA TRÊN THÁNG/NĂM
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1) // Calendar.MONTH là 0-indexed
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=CN, 6=T7
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = if (year == Calendar.getInstance().get(Calendar.YEAR) &&
        month == Calendar.getInstance().get(Calendar.MONTH) + 1) {
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    } else {
        -1 // Đánh dấu không phải tháng hiện tại
    }

    // ⚠️ Xóa biến totalItems không sử dụng để loại bỏ cảnh báo

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.height(280.dp)
    ) {
        // Empty cells before first day
        items(firstDayOfWeek) {
            Box(modifier = Modifier.size(40.dp))
        }

        // Days of the month
        items(daysInMonth) { index ->
            val day = index + 1
            val isSelected = selectedDays.contains(day)
            val isToday = today == day

            CalendarDayCell(
                day = day,
                isSelected = isSelected,
                isToday = isToday,
                onClick = { onDayClick(day) }
            )
        }
    }
}

// --- CÁC COMPOSABLE PHỤ (Không thay đổi) ---
@Composable
fun CalendarDayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .padding(2.dp)
            .background(
                color = when {
                    isSelected -> Color(0xFF6395EE)
                    isToday -> Color(0xFFE3F2FD)
                    else -> Color.Transparent
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 14.sp,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isSelected -> Color.White
                isToday -> Color(0xFF6395EE)
                else -> Color.Black
            }
        )
    }
}

data class MedicineCalendarItem(
    val name: String,
    val description: String,
    val time: String
)

@Composable
fun MedicineCalendarCard(medicine: MedicineCalendarItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFFC700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💊", fontSize = 24.sp)
                }

                // Medicine info
                Column {
                    Text(
                        text = medicine.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = medicine.description,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            // Time
            Surface(
                color = Color(0xFF6395EE),
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