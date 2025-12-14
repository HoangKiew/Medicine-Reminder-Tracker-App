package com.example.medinotify.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // ✅ 1. Import clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.example.medinotify.ui.navigation.NavDestination // ✅ 2. Import NavDestination
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

// =========================================================================
// I. CÁC HÀM COMPOSABLE PHỤ
// =========================================================================

@Composable
fun MedicineCalendarCard(item: ScheduleWithMedicine) {
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
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFFC700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💊", fontSize = 24.sp)
                }

                Column {
                    Text(
                        text = item.medicine?.name ?: "Tên thuốc không xác định",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    item.medicine?.dosage?.let {
                        Text(
                            text = "Liều lượng: $it",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Surface(
                color = Color(0xFF2C60FF),
                shape = RoundedCornerShape(8.dp)
            ) {
                val timeString = item.schedule.specificTimeStr.format(DateTimeFormatter.ofPattern("HH:mm"))
                Text(
                    text = timeString,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasSchedule: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .padding(2.dp)
            .background(
                color = when {
                    isSelected -> Color(0xFF2C60FF)
                    isToday -> Color(0xFFE3F2FD)
                    else -> Color.Transparent
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> Color.White
                    isToday -> Color(0xFF2C60FF)
                    else -> Color.Black
                }
            )
            if (hasSchedule && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(Color(0xFFFF5A5A), CircleShape)
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    selectedDate: LocalDate,
    scheduledDays: Set<Int>,
    onDayClick: (Int) -> Unit
) {
    val yearMonth = YearMonth.from(selectedDate)
    val firstDayOfMonth = selectedDate.withDayOfMonth(1)
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value % 7)
    val daysInMonth = yearMonth.lengthOfMonth()
    val today = LocalDate.now()

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.height(280.dp)
    ) {
        items(firstDayOfWeek) {
            Box(modifier = Modifier.size(40.dp))
        }

        items(daysInMonth) { index ->
            val day = index + 1
            val isSelected = selectedDate.dayOfMonth == day
            val hasSchedule = scheduledDays.contains(day)
            val isToday = (today.dayOfMonth == day && today.monthValue == selectedDate.monthValue && today.year == selectedDate.year)

            CalendarDayCell(
                day = day,
                isSelected = isSelected,
                isToday = isToday,
                hasSchedule = hasSchedule,
                onClick = { onDayClick(day) }
            )
        }
    }
}


// =========================================================================
// II. HÀM COMPOSABLE CHÍNH
// =========================================================================

@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = koinViewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val scheduledDays by viewModel.scheduledDaysInMonth.collectAsState()
    val schedulesWithMedicineList by viewModel.schedulesForSelectedDay.collectAsState()

    val currentMonthYear by remember(selectedDate) {
        derivedStateOf {
            val monthName = selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            "${monthName.replaceFirstChar { it.uppercase() }} ${selectedDate.year}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // ================== TOP BAR (ĐÃ SỬA) ==================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 📅 1. Icon Lịch (Hiện tại đang ở trang Lịch nên có thể không cần navigate, nhưng để cho đồng bộ)
            Icon(
                Icons.Filled.DateRange,
                contentDescription = "Calendar",
                tint = Color(0xFFFF5A5A),
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        // Nếu muốn reload lại trang lịch hoặc chỉ đơn giản là hiện icon
                        navController.navigate(NavDestination.Calendar.route) {
                            launchSingleTop = true
                        }
                    }
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // 👤 2. Icon Hồ sơ (Profile)
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color(0xFF355CFF),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { // ✅ Thêm điều hướng
                            navController.navigate(NavDestination.Profile.route)
                        }
                )

                Spacer(modifier = Modifier.width(18.dp))

                // ⚙️ 3. Icon Cài đặt (Settings)
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.DarkGray,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { // ✅ Thêm điều hướng
                            navController.navigate(NavDestination.Settings.route)
                        }
                )
            }
        }

        // ... Phần còn lại giữ nguyên ...
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.changeMonth(-1) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
                    }
                    Text(
                        text = currentMonthYear,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = { viewModel.changeMonth(1) }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7").forEach { day ->
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

                CalendarGrid(
                    selectedDate = selectedDate,
                    scheduledDays = scheduledDays,
                    onDayClick = viewModel::onDaySelected
                )
            }
        }

        Text(
            text = "Lịch uống thuốc trong ngày",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (schedulesWithMedicineList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Không có lịch uống thuốc cho ngày ${selectedDate.dayOfMonth}/${selectedDate.monthValue}",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(schedulesWithMedicineList.size) { index ->
                    val item = schedulesWithMedicineList[index]
                    MedicineCalendarCard(item = item)
                }
            }
        }
    }
}