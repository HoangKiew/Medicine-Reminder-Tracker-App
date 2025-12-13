package com.example.medinotify.ui.screens.addmedicine

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medinotify.data.model.Frequency
import com.example.medinotify.data.model.WeekDay
import com.example.medinotify.ui.navigation.NavDestination // ✅ IMPORT QUAN TRỌNG
import org.koin.androidx.compose.koinViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.contains
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(
    navController: NavController,
    viewModel: AddMedicineViewModel = koinViewModel()
) {

    // ================== HELPER & STATES CẦN THIẾT ==================
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val uiMessage by rememberUpdatedState(viewModel.uiMessage)

    val timePicker = TimePickerDialog.OnTimeSetListener { _, h, min ->
        val newTime = LocalTime.of(h, min)
        viewModel.addSpecificTime(newTime)
    }

    // ================== COLORS ==================
    val mainPink = Color(0xFFFF6B6B)
    val blueTitle = Color(0xFF2C60FF)
    val placeholderGray = Color.LightGray

    val transparentColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = mainPink,
        unfocusedIndicatorColor = Color.Gray,
        focusedLabelColor = blueTitle
    )

    val types = listOf("Viên nén", "Viên nang", "Xi-rô", "Thuốc nước")

    // --- Xử lý Toast và Điều hướng ---
    LaunchedEffect(uiMessage) {
        uiMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            if (message.contains("thành công")) {
                // ✅ ĐÃ SỬA LỖI NAVIGATION: Điều hướng về Home và xóa backstack
                navController.navigate(NavDestination.Home.route) {
                    // Đặt Home là đích đến duy nhất, xóa các màn hình thêm thuốc đã hoàn thành.
                    popUpTo(NavDestination.Home.route) { inclusive = true }
                }
            }
            viewModel.clearUiMessage()
        }
    }


    // ================== UI ==================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // HEADER
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = mainPink
                    )
                }
                Text(
                    "Thêm thuốc mới",
                    Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = blueTitle
                )
                Spacer(Modifier.width(48.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Điền vào các ô và nhấn nút Lưu để thêm!",
                color = mainPink,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))

            // CÁC TRƯỜNG NHẬP LIỆU
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = viewModel::onNameChange,
                label = { Row { Text("Tên thuốc"); if (viewModel.name.isEmpty()) Text(" *", color = Color.Red) } },
                placeholder = { Text("Ví dụ: Paracetamol", color = placeholderGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = transparentColors
            )
            Spacer(Modifier.height(15.dp))
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = viewModel.medicineType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Row { Text("Loại thuốc"); if (viewModel.medicineType == "Chọn dạng thuốc") Text(" *", color = Color.Red) } },
                    placeholder = { Text("Chọn dạng thuốc", color = placeholderGray) },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null, tint = mainPink) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    colors = transparentColors
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    types.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                viewModel.onTypeChange(it)
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(15.dp))
            OutlinedTextField(
                value = viewModel.dosage,
                onValueChange = viewModel::onDosageChange,
                label = { Row { Text("Liều lượng"); if (viewModel.dosage.isEmpty()) Text(" *", color = Color.Red) } },
                placeholder = { Text("Ví dụ: 500mg", color = placeholderGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = transparentColors
            )
            Spacer(Modifier.height(15.dp))
            OutlinedTextField(
                value = viewModel.quantity,
                onValueChange = viewModel::onQuantityChange,
                label = { Row { Text("Số lượng"); if (viewModel.quantity.isEmpty()) Text(" *", color = Color.Red) } },
                placeholder = { Text("Ví dụ: 10", color = placeholderGray) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = transparentColors
            )

            Spacer(Modifier.height(25.dp))

            // MỤC CHỌN TẦN SUẤT
            Text("Tần suất", color = mainPink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            var freqExpanded by remember { mutableStateOf(false) }
            val frequencies = listOf(Frequency.DAILY, Frequency.SPECIFIC_DAYS, Frequency.INTERVAL)

            ExposedDropdownMenuBox(expanded = freqExpanded, onExpandedChange = { freqExpanded = !freqExpanded }) {
                OutlinedTextField(
                    value = viewModel.frequency.displayText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Chế độ lặp lại") },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null, tint = mainPink) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    colors = transparentColors
                )
                ExposedDropdownMenu(expanded = freqExpanded, onDismissRequest = { freqExpanded = false }) {
                    frequencies.forEach { freq ->
                        DropdownMenuItem(
                            text = { Text(freq.displayText) },
                            onClick = {
                                viewModel.onFrequencyChange(freq)
                                freqExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // HIỂN THỊ CÁC TÙY CHỌN TƯƠNG ỨNG VỚI TẦN SUẤT
            when (viewModel.frequency) {
                Frequency.SPECIFIC_DAYS -> {
                    Text("Chọn các ngày trong tuần:", fontSize = 14.sp, color = Color.DarkGray)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        WeekDay.entries.forEach { day ->
                            DayChip(
                                day = day,
                                isSelected = viewModel.selectedDays.contains(day),
                                onDaySelected = viewModel::toggleDaySelection
                            )
                        }
                    }
                }
                Frequency.INTERVAL -> {
                    OutlinedTextField(
                        value = viewModel.intervalDays,
                        onValueChange = viewModel::onIntervalChange,
                        label = { Text("Uống mỗi (ngày)") },
                        placeholder = { Text("2") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = transparentColors,
                        suffix = { Text("ngày / lần") }
                    )
                }
                Frequency.DAILY -> { /* Không cần hiển thị gì thêm cho chế độ hàng ngày */ }
            }

            Spacer(Modifier.height(25.dp))

            // PHẦN LỜI NHẮC
            Text("Lời nhắc nhở", color = mainPink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Bật báo thức", color = Color.Gray)
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = viewModel.enableReminder,
                    onCheckedChange = viewModel::onEnableReminderChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = mainPink,
                        checkedTrackColor = mainPink.copy(alpha = 0.4f)
                    )
                )
            }
            Spacer(Modifier.height(18.dp))
            if (viewModel.enableReminder) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Giờ uống (${viewModel.specificTimes.size} lần):", color = blueTitle, fontSize = 16.sp)
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        TimePickerDialog(
                            context,
                            timePicker,
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    }) {
                        Text("Thêm giờ")
                    }
                }
                Spacer(Modifier.height(12.dp))
                FlowRow(Modifier.fillMaxWidth()) {
                    viewModel.specificTimes.forEach { time ->
                        TimeChip(time = time, onRemove = viewModel::removeSpecificTime, mainColor = mainPink)
                    }
                    if (viewModel.specificTimes.isEmpty()) {
                        Text("Chưa có giờ nhắc nhở.", color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Spacer(Modifier.height(30.dp))

            // NÚT LƯU
            Button(
                onClick = viewModel::saveMedicine,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(mainPink)
            ) {
                Text("Lưu", color = Color.White, fontSize = 18.sp)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayChip(day: WeekDay, isSelected: Boolean, onDaySelected: (WeekDay) -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = { onDaySelected(day) },
        label = { Text(day.shortName) },
        shape = CircleShape,
        modifier = Modifier.padding(horizontal = 2.dp)
    )
}

@Composable
fun TimeChip(time: LocalTime, onRemove: (LocalTime) -> Unit, mainColor: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = mainColor.copy(alpha = 0.1f),
        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(time.format(DateTimeFormatter.ofPattern("HH:mm")), fontSize = 14.sp, color = mainColor)
            Icon(
                Icons.Default.Close,
                contentDescription = "Xóa giờ",
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(16.dp)
                    .clickable { onRemove(time) },
                tint = Color.Gray
            )
        }
    }
}