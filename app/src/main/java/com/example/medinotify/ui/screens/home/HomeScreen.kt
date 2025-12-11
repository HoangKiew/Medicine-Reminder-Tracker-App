package com.example.medinotify.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medinotify.R
// ✅ BỔ SUNG IMPORT CÒN THIẾU
import com.example.medinotify.ui.navigation.NavDestination
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale


data class Day(val date: LocalDate, val number: String, val label: String)


// ----------------------------- MAIN HOMESCREEN -------------------------

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel() // Inject ViewModel bằng Koin
) {
    // Lấy trạng thái (State) từ StateFlow trong ViewModel.
    val uiState by viewModel.uiState.collectAsState()

    // Lấy dữ liệu từ uiState để dùng cho tiện
    val medicineList = uiState.medicineSchedules
    val totalMedicines = medicineList.size
    val takenMedicines = medicineList.count { it.isTaken }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 40.dp)
    ) {
        // ------------------- TOP BAR -------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.DateRange, contentDescription = "Calendar", tint = Color(0xFFFF5A5A),
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        navController.navigate(NavDestination.Calendar.route)
                    }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Person, contentDescription = "Profile", tint = Color(0xFF355CFF),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            navController.navigate(NavDestination.Profile.route)
                        }
                )
                Spacer(modifier = Modifier.width(18.dp))
                Icon(
                    Icons.Filled.Settings, contentDescription = "Settings", tint = Color.DarkGray,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            navController.navigate(NavDestination.Settings.route)
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Hôm nay", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C60FF), modifier = Modifier.padding(start = 20.dp))
        Spacer(modifier = Modifier.height(15.dp))

        // ------------------ DATE SELECTOR ----------------------
        DaySelector(
            selectedDate = uiState.selectedDate,
            onDaySelected = { newDate -> viewModel.loadSchedulesForDate(newDate) } // Gọi hàm trong ViewModel
        )

        Spacer(modifier = Modifier.height(22.dp))

        // ------------------ SUMMARY CIRCLE ----------------------
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Lượng thuốc tiêu thụ", fontSize = 18.sp, color = Color(0xFF2C60FF), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color(0xFFEFF6FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(id = R.drawable.medicine_start), contentDescription = null, modifier = Modifier.size(55.dp))
                    Text("$takenMedicines/$totalMedicines", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF355CFF))
                    Text(uiState.selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()), fontSize = 14.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ------------------ LIST MEDICINE ----------------------
        if (uiState.isLoading) {
            // Hiển thị vòng xoay loading khi dữ liệu đang tải
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Hiển thị danh sách khi đã có dữ liệu
            LazyColumn(modifier = Modifier.padding(horizontal = 20.dp)) {
                items(medicineList) { item ->
                    MedicineCard(item)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Để tránh bị che bởi BottomBar
                }
            }
        }
    }
}

// ----------------------------- SUPPORTING COMPOSABLES -------------------------

@Composable
fun DaySelector(
    selectedDate: LocalDate,
    onDaySelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val daysOfWeek = remember(today) { // Chỉ tính toán lại khi 'today' thay đổi
        List(7) { i ->
            val date = today.plusDays(i.toLong())
            Day(
                date = date,
                number = date.dayOfMonth.toString(),
                label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US).uppercase()
            )
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(daysOfWeek) { day ->
            val isSelected = day.date == selectedDate

            Column(
                modifier = Modifier
                    .width(60.dp)
                    .height(78.dp)
                    .background(Color.White, RoundedCornerShape(18.dp))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Color(0xFF2C60FF) else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { onDaySelected(day.date) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(day.number, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    day.label,
                    fontSize = 12.sp,
                    color = if (isSelected) Color(0xFF2C60FF) else Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun MedicineCard(item: MedicineItem) {
    val bgColor = if (item.isTaken) Color(0xFFDBFFE8) else Color(0xFFFFE3E3)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFFFFC700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(item.name, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(item.description, fontSize = 13.sp, color = Color.Gray)
                }
            }
            Box(
                modifier = Modifier
                    .background(Color(0xFF2C60FF), RoundedCornerShape(10.dp))
                    .padding(vertical = 6.dp, horizontal = 14.dp)
            ) {
                Text(item.time, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
