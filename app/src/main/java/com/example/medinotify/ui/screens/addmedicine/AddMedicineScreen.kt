package com.example.medinotify.ui.screens.addmedicine

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
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
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(
    navController: NavController,
    viewModel: AddMedicineViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val uiMessage by rememberUpdatedState(viewModel.uiMessage)

    val timePicker = TimePickerDialog.OnTimeSetListener { _, h, min ->
        val newTime = LocalTime.of(h, min)
        viewModel.addSpecificTime(newTime)
    }

    // Colors
    val softPink = Color(0xFFFFD9D9)
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

    LaunchedEffect(uiMessage) {
        uiMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearUiMessage()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp,
                vertical = 8.dp).verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = mainPink)
                }
                Text("Thêm thuốc mới",
                    Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    color = blueTitle)
                Spacer(Modifier.width(48.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text("Điền vào các ô và nhấn nút Lưu để thêm!",
                color = mainPink,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))

            // Các ô nhập liệu cơ bản
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = viewModel::onNameChange,
                label = { Row { Text("Tên thuốc");
                    if (viewModel.name.isEmpty()) Text(" *", color = Color.Red) } },
                modifier = Modifier.fillMaxWidth(),
                colors = transparentColors
            )
            Spacer(Modifier.height(15.dp))

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded,
                onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = viewModel.medicineType, onValueChange = {},
                    readOnly = true,
                    label = { Row { Text("Loại thuốc");
                        if (viewModel.medicineType == "Chọn dạng thuốc") Text(" *", color = Color.Red) } },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown,
                        null, tint = mainPink) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    colors = transparentColors
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    types.forEach { DropdownMenuItem(text = { Text(it) },
                        onClick = { viewModel.onTypeChange(it); expanded = false }) }
                }
            }
            Spacer(Modifier.height(15.dp))

            OutlinedTextField(
                value = viewModel.dosage,
                onValueChange = viewModel::onDosageChange,
                label = { Row { Text("Liều lượng");
                    if (viewModel.dosage.isEmpty()) Text(" *",
                        color = Color.Red) } },
                modifier = Modifier.fillMaxWidth(),
                colors = transparentColors
            )
            Spacer(Modifier.height(15.dp))

            OutlinedTextField(
                value = viewModel.quantity, onValueChange = viewModel::onQuantityChange,
                label = { Row { Text("Số lượng");
                    if (viewModel.quantity.isEmpty()) Text(" *", color = Color.Red) } },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = transparentColors
            )

            // --- PHẦN CHỌN NGÀY ---
            Spacer(Modifier.height(25.dp))
            Text("Thời gian sử dụng",
                color = mainPink,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                DateSelectorField(
                    label = "Bắt đầu",
                    date = viewModel.startDate,
                    onDateSelected = viewModel::onStartDateChange,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    colors = transparentColors
                )
                DateSelectorField(
                    label = "Kết thúc",
                    date = viewModel.endDate,
                    onDateSelected = viewModel::onEndDateChange,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    colors = transparentColors
                )
            }

            Spacer(Modifier.height(25.dp))

            // Phần nhắc nhở
            Text("Lời nhắc nhở",
                color = mainPink,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Bật báo thức",
                    color = Color.Gray)
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = viewModel.enableReminder,
                    onCheckedChange = viewModel::onEnableReminderChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = mainPink,
                        checkedTrackColor = mainPink.copy(alpha = 0.4f))
                )
            }

            Spacer(Modifier.height(18.dp))

            if (viewModel.enableReminder) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Giờ uống (${viewModel.specificTimes.size} lần):",
                        color = blueTitle, fontSize = 16.sp)
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        TimePickerDialog(context, timePicker,
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE), true).show()
                    }) { Text("Thêm giờ") }
                }
                Spacer(Modifier.height(12.dp))
                FlowRow(Modifier.fillMaxWidth()) {
                    viewModel.specificTimes.forEach { time -> TimeChip(time = time,
                        onRemove = viewModel::removeSpecificTime,
                        mainColor = mainPink) }
                }
            }

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = {
                    viewModel.saveMedicine {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(mainPink)
            ) {
                Text("Lưu", color = Color.White, fontSize = 18.sp)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun DateSelectorField(
    label: String,
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    colors: TextFieldColors
) {
    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
        },
        date.year,
        date.monthValue - 1,
        date.dayOfMonth
    )

    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                datePickerDialog.show()
            }
        }
    }

    OutlinedTextField(
        value = date.format(dateFormatter),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Default.DateRange,
            contentDescription = null,
            tint = Color(0xFFFF6B6B)) },
        modifier = modifier,
        interactionSource = interactionSource,
        colors = colors
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
            Text(time.format(DateTimeFormatter.ofPattern("HH:mm")),
                fontSize = 14.sp, color = mainColor)
            Icon(Icons.Default.Close, contentDescription = "Xóa",
                modifier = Modifier.padding(start = 4.dp).size(16.dp).clickable { onRemove(time) },
                tint = Color.Gray)
        }
    }
}