package com.example.medinotify.ui.screens.addmedicine

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.medinotify.ui.navigation.NavDestination
import org.koin.androidx.compose.koinViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import java.util.*
import java.time.DayOfWeek
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(
    navController: NavController,
    viewModel: AddMedicineViewModel = koinViewModel()
) {
    // ================== HELPER & STATES CẦN THIẾT ==================
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // ✅ FIX: Đọc State từ StateFlow
    val state by viewModel.uiState.collectAsState()
    val uiMessage by rememberUpdatedState(state.uiMessage)

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
    val frequencies = remember { Frequency.entries } // Dùng entries()

    // --- Xử lý Toast và Điều hướng ---
    LaunchedEffect(uiMessage) {
        uiMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            // Chỉ điều hướng khi có thông báo thành công từ ViewModel
            if (message.contains("Lưu thành công")) {
                navController.popBackStack() // Quay lại màn hình trước
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
                    text = if (state.medicineId == null) "Thêm thuốc mới" else "Sửa thuốc",
                    modifier = Modifier.weight(1f),
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
                value = state.name, // ✅ SỬA: Dùng state.name
                onValueChange = viewModel::onNameChange,
                label = { Row { Text("Tên thuốc"); if (state.name.isEmpty()) Text(" *", color = Color.Red) } },
                placeholder = { Text("Ví dụ: Paracetamol", color = placeholderGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = transparentColors
            )
            Spacer(Modifier.height(15.dp))

            // Dropdown Loại thuốc (Type)
            var expandedType by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = !expandedType }
            ) {
                OutlinedTextField(
                    value = state.medicineType, // ✅ SỬA: Dùng state.medicineType
                    onValueChange = {},
                    readOnly = true,
                    label = { Row { Text("Loại thuốc"); if (state.medicineType == "Chọn dạng thuốc") Text(" *", color = Color.Red) } },
                    placeholder = { Text("Chọn dạng thuốc", color = placeholderGray) },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null, tint = mainPink) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    colors = transparentColors
                )
                ExposedDropdownMenu(
                    expanded = expandedType,
                    onDismissRequest = { expandedType = false }
                ) {
                    types.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                viewModel.onTypeChange(type)
                                expandedType = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(15.dp))
            OutlinedTextField(
                value = state.dosage, // ✅ SỬA: Dùng state.dosage
                onValueChange = viewModel::onDosageChange,
                label = { Row { Text("Liều lượng"); if (state.dosage.isEmpty()) Text(" *", color = Color.Red) } },
                placeholder = { Text("Ví dụ: 500mg", color = placeholderGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = transparentColors
            )
            Spacer(Modifier.height(15.dp))
            OutlinedTextField(
                value = state.quantity, // ✅ SỬA: Dùng state.quantity
                onValueChange = viewModel::onQuantityChange,
                label = { Row { Text("Số lượng"); if (state.quantity.isEmpty()) Text(" *", color = Color.Red) } },
                placeholder = { Text("Ví dụ: 10", color = placeholderGray) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = transparentColors
            )

            Spacer(Modifier.height(25.dp))

            // MỤC CHỌN TẦN SUẤT
            Text("Tần suất", color = blueTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            // Dropdown Tần suất (Frequency)
            var freqExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = freqExpanded, onExpandedChange = { freqExpanded = !freqExpanded }) {
                OutlinedTextField(
                    value = state.frequencyType.displayText, // ✅ SỬA: Dùng state.frequencyType
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
                                viewModel.onFrequencyTypeChange(freq) // ✅ SỬA: Dùng hàm mới
                                freqExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // HIỂN THỊ CÁC TÙY CHỌN TƯƠNG ỨNG VỚI TẦN SUẤT
            when (state.frequencyType) { // ✅ SỬA: Dùng state.frequencyType
                // CHỌN NGÀY CỤ THỂ TRONG TUẦN
                Frequency.SPECIFIC_DAYS -> {
                    Text("Chọn các ngày trong tuần:", fontSize = 14.sp, color = Color.DarkGray)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        WeekDay.entries.forEach { day ->
                            DayChip(
                                day = day,
                                isSelected = state.selectedDays.contains(day.javaDayOfWeek), // ✅ SỬA: Dùng state.selectedDays
                                onDaySelected = viewModel::toggleDaySelection // ✅ SỬA: Dùng hàm mới (nhận DayOfWeek)
                            )
                        }
                    }
                    Spacer(Modifier.height(15.dp))
                }
                // CHỌN KHOẢNG CÁCH NGÀY
                Frequency.INTERVAL -> {
                    OutlinedTextField(
                        value = state.intervalDays, // ✅ SỬA: Dùng state.intervalDays
                        onValueChange = viewModel::onIntervalDaysChange, // ✅ SỬA: Dùng hàm mới
                        label = { Text("Uống mỗi (ngày)") },
                        placeholder = { Text("2") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = transparentColors,
                        suffix = { Text("ngày / lần") }
                    )
                    Spacer(Modifier.height(15.dp))
                }
                Frequency.DAILY -> { /* Không cần hiển thị gì thêm cho chế độ hàng ngày */ }
            }

            // ❌ XÓA KHUNG CHỌN NGÀY BẮT ĐẦU/KẾT THÚC (Theo yêu cầu)
            /*
            Spacer(Modifier.height(25.dp))
            Text("Thời gian sử dụng", color = blueTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            // ... (Logic DateSelectorField đã bị xóa)
            */


            Spacer(Modifier.height(25.dp))

            // PHẦN LỜI NHẮC
            Text("Lời nhắc nhở", color = mainPink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Bật báo thức", color = Color.Gray)
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = state.enableReminder, // ✅ SỬA: Dùng state.enableReminder
                    onCheckedChange = viewModel::onEnableReminderChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = mainPink,
                        checkedTrackColor = mainPink.copy(alpha = 0.4f)
                    )
                )
            }
            Spacer(Modifier.height(18.dp))
            if (state.enableReminder) { // ✅ SỬA: Dùng state.enableReminder
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Giờ uống (${state.specificTimes.size} lần):", color = blueTitle, fontSize = 16.sp) // ✅ SỬA: Dùng state.specificTimes
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
                    state.specificTimes.forEach { time -> // ✅ SỬA: Dùng state.specificTimes
                        TimeChip(time = time, onRemove = viewModel::removeSpecificTime, mainColor = mainPink)
                    }
                    if (state.specificTimes.isEmpty()) { // ✅ SỬA: Dùng state.specificTimes
                        Text("Chưa có giờ nhắc nhở.", color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Spacer(Modifier.height(30.dp))

            // NÚT LƯU
            Button(
                // ✅ FIX: Gọi hàm saveMedicine và truyền callback đóng màn hình
                onClick = {
                    viewModel.saveMedicine {
                        // Logic điều hướng sau khi lưu thành công
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(mainPink),
                enabled = !state.isLoading // ✅ FIX: Thêm trạng thái loading
            ) {
                Text(if (state.isLoading) "Đang lưu..." else "Lưu",
                    color = Color.White, fontSize = 18.sp)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

// ✅ SỬA: DayChip để sử dụng DayOfWeek (cho ViewModel)
@Composable
fun DayChip(day: WeekDay, isSelected: Boolean, onDaySelected: (DayOfWeek) -> Unit) {
    val mainPink = Color(0xFFFF6B6B)
    val selectedColor = mainPink
    val unselectedColor = Color.LightGray
    val backgroundColor = if (isSelected) selectedColor else Color.Transparent
    val borderColor = if (isSelected) selectedColor else unselectedColor
    val textColor = if (isSelected) Color.White else Color.Gray

    Box(
        modifier = Modifier
            .size(40.dp)
            .padding(vertical = 4.dp)
            .background(backgroundColor, shape = CircleShape)
            .border(1.dp, borderColor, CircleShape)
            .clickable { onDaySelected(day.javaDayOfWeek) } // ✅ FIX: Truyền DayOfWeek
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.shortName,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
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