// ======= FILE HOÀN CHỈNH SAU KHI ĐÃ SỬA =======

package com.example.medinotify.ui.screens.addmedicine

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.example.medinotify.data.api.MedicineDTO
import com.example.medinotify.viewmodel.MedicineViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons

// ================= CHIP SELECT ================
@Composable
fun FrequencyChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    mainColor: Color
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) mainColor else Color.LightGray.copy(alpha = 0.3f),
            labelColor = if (selected) Color.White else Color.DarkGray
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(navController: NavController) {

    // ================= STATES =================
    var medicineName by remember { mutableStateOf("") }
    var medicineType by remember { mutableStateOf("Chọn dạng thuốc") }
    var dosage by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    var reminderDate by remember { mutableStateOf("") }
    var enableReminder by remember { mutableStateOf(false) }

    var timeList by remember { mutableStateOf(listOf<String>()) }

    var notes by remember { mutableStateOf("") }

    var frequency by remember { mutableStateOf("Daily") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val vm: MedicineViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    // ============ HELPERS =============
    fun convertTo24h(time12: String): String {
        val sdf12 = SimpleDateFormat("hh:mm a", Locale.US)
        val sdf24 = SimpleDateFormat("HH:mm", Locale.US)
        return sdf24.format(sdf12.parse(time12)!!)
    }

    fun getDayName(date: String): String {
        val sdfInput = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val sdfDay = SimpleDateFormat("EEE", Locale.US)
        return sdfDay.format(sdfInput.parse(date)!!)
    }

    // ================ EDIT REMINDER ================
    fun editReminder() {

        if (frequency == "Daily") {

            fun openSequential(i: Int) {
                if (i >= timeList.size) return

                TimePickerDialog(
                    context,
                    { _, h, m ->
                        val ampm = if (h >= 12) "PM" else "AM"
                        val h12 = when {
                            h == 0 -> 12
                            h > 12 -> h - 12
                            else -> h
                        }

                        val newTime = String.format(Locale.US, "%02d:%02d %s", h12, m, ampm)
                        timeList = timeList.toMutableList().also { it[i] = newTime }

                        openSequential(i + 1)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            }

            openSequential(0)
            return
        }

        // If Once → pick date first
        DatePickerDialog(
            context,
            { _, year, month, day ->

                reminderDate = String.format(Locale.US, "%02d/%02d/%04d", day, month + 1, year)

                fun openSequential(i: Int) {
                    if (i >= timeList.size) return

                    TimePickerDialog(
                        context,
                        { _, h, m ->
                            val ampm = if (h >= 12) "PM" else "AM"
                            val h12 = when {
                                h == 0 -> 12
                                h > 12 -> h - 12
                                else -> h
                            }

                            val newTime = String.format(Locale.US, "%02d:%02d %s", h12, m, ampm)
                            timeList = timeList.toMutableList().also { it[i] = newTime }

                            openSequential(i + 1)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false
                    ).show()
                }

                openSequential(0)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun pickSingleTime(index: Int) {
        TimePickerDialog(
            context,
            { _, h, m ->

                val ampm = if (h >= 12) "PM" else "AM"
                val h12 = when {
                    h == 0 -> 12
                    h > 12 -> h - 12
                    else -> h
                }

                val newTime = String.format(Locale.US, "%02d:%02d %s", h12, m, ampm)
                timeList = timeList.toMutableList().also { it[index] = newTime }

            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    // ================= COLORS =================
    val mainBlue = Color(0xFF2C60FF)
    val placeholderGray = Color.LightGray
    val softBlueBG = Color(0xFFEAF0FF)

    val textFieldColors = TextFieldDefaults.colors(
        focusedIndicatorColor = mainBlue,
        unfocusedIndicatorColor = Color.Gray,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )

    val types = listOf("Viên nén", "Viên nang", "Xi-rô", "Thuốc nước")

    // ================= UI =================
    Column(
        Modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState())
    ) {

        // HEADER
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = mainBlue)
            }

            Text(
                "Thêm thuốc mới",
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = mainBlue
            )

            Spacer(Modifier.width(48.dp))
        }

        Spacer(Modifier.height(20.dp))

        // NAME
        OutlinedTextField(
            value = medicineName,
            onValueChange = { medicineName = it },
            label = { Text("Tên thuốc") },
            placeholder = { Text("Ví dụ: Paracetamol", color = placeholderGray) },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors
        )

        Spacer(Modifier.height(15.dp))

        // TYPE
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {

            OutlinedTextField(
                value = medicineType,
                readOnly = true,
                label = { Text("Loại thuốc") },
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null, tint = mainBlue) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                onValueChange = {},
                colors = textFieldColors
            )

            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                types.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            medicineType = it
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(15.dp))

        // DOSAGE
        OutlinedTextField(
            value = dosage,
            onValueChange = { dosage = it },
            label = { Text("Liều lượng") },
            placeholder = { Text("Ví dụ: 500mg", color = placeholderGray) },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors
        )

        Spacer(Modifier.height(20.dp))

        // FREQUENCY
        Text("Tần suất uống", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = mainBlue)
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FrequencyChip("Uống hằng ngày", frequency == "Daily", { frequency = "Daily" }, mainBlue)
            FrequencyChip("Chỉ uống vào ngày đã chọn", frequency == "Once", { frequency = "Once" }, mainBlue)
        }

        Spacer(Modifier.height(20.dp))

        // DATE
        OutlinedTextField(
            value = reminderDate,
            readOnly = true,
            onValueChange = {},
            label = { Text(if (frequency == "Daily") "Ngày bắt đầu" else "Ngày uống") },
            trailingIcon = {
                Icon(
                    Icons.Default.DateRange,
                    null,
                    tint = mainBlue,
                    modifier = Modifier.clickable { editReminder() }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors
        )

        Spacer(Modifier.height(20.dp))

        // QUANTITY
        OutlinedTextField(
            value = quantity,
            onValueChange = {
                if (it.all { c -> c.isDigit() }) {
                    quantity = it
                    val num = it.toIntOrNull() ?: 0
                    timeList = List(num) { i -> timeList.getOrNull(i) ?: "" }
                }
            },
            label = { Text("Số lần uống / ngày") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors
        )

        Spacer(Modifier.height(20.dp))

        // TIME PICKERS
        timeList.forEachIndexed { index, time ->
            OutlinedTextField(
                value = time,
                readOnly = true,
                onValueChange = {},
                label = { Text("Giờ uống ${index + 1}") },
                trailingIcon = {
                    Icon(
                        Icons.Default.AccessTime,
                        null,
                        tint = mainBlue,
                        modifier = Modifier.clickable { pickSingleTime(index) }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = textFieldColors
            )
        }

        Spacer(Modifier.height(20.dp))

        // =============== REMINDER SWITCH ===============
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Bật báo thức", color = Color.Gray)
            Spacer(Modifier.weight(1f))
            Switch(
                checked = enableReminder,
                onCheckedChange = { enableReminder = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = mainBlue,
                    checkedTrackColor = mainBlue.copy(alpha = 0.4f)
                )
            )
        }

        Spacer(Modifier.height(20.dp))

        // =============== REMINDER BOX (NEW!) ===============
        if (enableReminder && timeList.isNotEmpty()) {

            val displayDay = when (frequency) {
                "Daily" -> "Daily"
                "Once" -> if (reminderDate.isNotEmpty()) getDayName(reminderDate) else ""
                else -> ""
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEAF0FF), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = displayDay,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(Modifier.width(16.dp))

                    val currentTime = timeList.first()
                    val parts = currentTime.split(" ")

                    Text(parts[0], fontSize = 18.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(parts[1], fontSize = 18.sp)

                    Spacer(Modifier.width(16.dp))

                    Button(
                        onClick = { editReminder() },
                        colors = ButtonDefaults.buttonColors(Color(0xFFFFB800)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Edit", color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }

        // NOTES
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Ghi chú") },
            placeholder = { Text("Ví dụ: Uống sau ăn, tránh buổi tối", color = placeholderGray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5,
            colors = textFieldColors
        )

        Spacer(Modifier.height(20.dp))

        // SAVE
        Button(
            onClick = {
                if (
                    medicineName.isEmpty() ||
                    medicineType == "Chọn dạng thuốc" ||
                    dosage.isEmpty() ||
                    quantity.isEmpty() ||
                    reminderDate.isEmpty() ||
                    timeList.any { it.isEmpty() }
                ) {
                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val apiDate = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                    .parse(reminderDate)
                    ?.let { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it) }
                    ?: ""

                val time24List = timeList.map { convertTo24h(it) }

                val dto = MedicineDTO(
                    userId = "U001",
                    name = medicineName,
                    medicineType = medicineType,
                    dosage = dosage,
                    notes = if (notes.isEmpty()) null else notes,
                    timesPerDay = quantity.toInt(),
                    scheduleTimes = time24List,
                    startDate = apiDate,
                    frequency = frequency
                )

                vm.addMedicine(dto) {
                    Toast.makeText(context, "Đã lưu thuốc!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(mainBlue)
        ) {
            Text("Lưu", color = Color.White, fontSize = 18.sp)
        }

        Spacer(Modifier.height(40.dp))
    }
}
