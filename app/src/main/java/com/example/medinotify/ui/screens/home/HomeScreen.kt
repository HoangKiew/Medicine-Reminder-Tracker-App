package com.example.medinotify.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.medinotify.R
import com.example.medinotify.ui.navigation.NavDestination
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class Day(val date: LocalDate, val number: String, val label: String)

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Lifecycle Observer để reload data khi quay lại màn hình
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                modifier = Modifier.size(28.dp).clickable { navController.navigate(NavDestination.Calendar.route) }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Person, contentDescription = "Profile", tint = Color(0xFF355CFF),
                    modifier = Modifier.size(28.dp).clickable { navController.navigate(NavDestination.Profile.route) }
                )
                Spacer(modifier = Modifier.width(18.dp))
                Icon(
                    Icons.Filled.Settings, contentDescription = "Settings", tint = Color.DarkGray,
                    modifier = Modifier.size(28.dp).clickable { navController.navigate(NavDestination.Settings.route) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Hôm nay", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C60FF), modifier = Modifier.padding(start = 20.dp))
        Spacer(modifier = Modifier.height(15.dp))

        // ------------------ DATE SELECTOR ----------------------
        DaySelector(
            selectedDate = uiState.selectedDate,
            onDaySelected = { newDate -> viewModel.loadSchedulesForDate(newDate) }
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(horizontal = 20.dp)) {
                items(medicineList) { item ->
                    MedicineCard(
                        item = item,
                        onDelete = {
                            viewModel.deleteMedicine(item.id)
                            Toast.makeText(context, "Đã xóa thuốc: ${item.name}", Toast.LENGTH_SHORT).show()
                        },
                        // ✨✨✨ CẬP NHẬT PHẦN SỬA THUỐC TẠI ĐÂY ✨✨✨
                        onEdit = {
                            // Gọi hàm createRoute mà chúng ta đã thêm vào NavDestination
                            // để chuyển sang màn hình AddMedicine kèm theo ID thuốc
                            navController.navigate(NavDestination.AddMedicine.createRoute(item.id))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

// ------------------ COMPOSABLES PHỤ ----------------------

@Composable
fun DaySelector(selectedDate: LocalDate, onDaySelected: (LocalDate) -> Unit) {
    val today = LocalDate.now()
    val daysOfWeek = remember(today) {
        List(7) { i ->
            val date = today.plusDays(i.toLong())
            Day(
                date = date,
                number = date.dayOfMonth.toString(),
                label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US).uppercase()
            )
        }
    }

    LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                Text(day.label, fontSize = 12.sp, color = if (isSelected) Color(0xFF2C60FF) else Color.Gray, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MedicineCard(
    item: MedicineItem,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val bgColor = if (item.isTaken) Color(0xFFDBFFE8) else Color(0xFFFFE3E3)
    val iconTint = if (item.isTaken) Color(0xFF00C853) else Color(0xFFFFC700)
    val icon = if (item.isTaken) Icons.Filled.CheckCircle else Icons.Filled.Info

    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor, RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = { },
                    onLongClick = { expanded = true }
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint)
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

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 10.dp, y = 0.dp),
            containerColor = Color.White
        ) {
            DropdownMenuItem(
                text = { Text("Chỉnh sửa") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF2C60FF)) },
                onClick = {
                    expanded = false
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = { Text("Xóa thuốc", color = Color.Red) },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                onClick = {
                    expanded = false
                    showDeleteDialog = true
                }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Xóa thuốc?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa '${item.name}' và toàn bộ lịch nhắc nhở của thuốc này không?") },
            containerColor = Color.White,
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Xóa", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy", color = Color.Gray)
                }
            }
        )
    }
}