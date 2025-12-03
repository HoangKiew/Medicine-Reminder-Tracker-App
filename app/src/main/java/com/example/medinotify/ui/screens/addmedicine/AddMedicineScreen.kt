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
import androidx.compose.material.icons.filled.*
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
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineScreen(navController: NavController) {

    // ================== STATES ==================
    var medicineName by remember { mutableStateOf("") }
    var medicineType by remember { mutableStateOf("Chọn dạng thuốc") }
    var dosage by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    var reminderDate by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf("") }

    var selectedDay by remember { mutableStateOf("") }
    var selectedHour by remember { mutableStateOf("") }
    var selectedAmPm by remember { mutableStateOf("") }

    var enableReminder by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // ================== HELPER ==================
    fun getDayName(year: Int, month: Int, day: Int): String {
        val sdf = SimpleDateFormat("EEE", Locale.US)
        val c = Calendar.getInstance()
        c.set(year, month, day)
        return sdf.format(c.time)
    }

    // ================== LISTENERS ==================
    val timePicker = TimePickerDialog.OnTimeSetListener { _, h, min ->
        val ampm = if (h >= 12) "PM" else "AM"
        val hour12 = if (h == 0) 12 else if (h > 12) h - 12 else h

        selectedHour = String.format(Locale.getDefault(), "%02d:%02d", hour12, min)
        selectedAmPm = ampm
        reminderTime = "$selectedHour $selectedAmPm"
    }

    val datePicker = DatePickerDialog.OnDateSetListener { _, y, m, d ->
        reminderDate = "$d/${m + 1}/$y"
        selectedDay = getDayName(y, m, d)

        // Sau khi chọn ngày -> chọn giờ
        TimePickerDialog(
            context,
            timePicker,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    // ================== COLORS ==================
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

    // ================== UI ==================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)   // ⭐ NỀN TRẮNG
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
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

            // NOTE
            Text(
                text = "Điền vào các ô và nhấn nút Lưu để thêm!",
                color = mainPink,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // NAME
            OutlinedTextField(
                value = medicineName,
                onValueChange = { medicineName = it },
                label = {
                    Row {
                        Text("Tên thuốc"); if (medicineName.isEmpty()) Text(
                        " *",
                        color = Color.Red
                    )
                    }
                },
                placeholder = { Text("Ví dụ: Paracetamol", color = placeholderGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = transparentColors
            )

            Spacer(Modifier.height(15.dp))

            // DROPDOWN
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = medicineType,
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Row {
                            Text("Loại thuốc"); if (medicineType == "Chọn dạng thuốc") Text(
                            " *",
                            color = Color.Red
                        )
                        }
                    },
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
                label = {
                    Row { Text("Liều lượng"); if (dosage.isEmpty()) Text(" *", color = Color.Red) }
                },
                placeholder = { Text("Ví dụ: 500mg", color = placeholderGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = transparentColors
            )

            Spacer(Modifier.height(15.dp))

            // QUANTITY
            OutlinedTextField(
                value = quantity,
                onValueChange = { if (it.all { ch -> ch.isDigit() }) quantity = it },
                label = {
                    Row { Text("Số lượng"); if (quantity.isEmpty()) Text(" *", color = Color.Red) }
                },
                placeholder = { Text("Ví dụ: 10", color = placeholderGray) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = transparentColors
            )

            Spacer(Modifier.height(25.dp))

            // REMINDER TITLE
            Text("Lời nhắc nhở", color = mainPink, fontSize = 17.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))

            // REMINDER INPUT
            if (!enableReminder) {

                // DATE
                OutlinedTextField(
                    value = reminderDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ngày") },
                    placeholder = { Text("dd/mm/yyyy", color = placeholderGray) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.DateRange, null,
                            tint = mainPink,
                            modifier = Modifier.clickable {
                                DatePickerDialog(
                                    context, datePicker,
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            DatePickerDialog(
                                context, datePicker,
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    colors = transparentColors
                )

                Spacer(Modifier.height(15.dp))

                // TIME
                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Giờ") },
                    placeholder = { Text("00:00 AM/PM", color = placeholderGray) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.AccessTime, null,
                            tint = mainPink,
                            modifier = Modifier.clickable {
                                TimePickerDialog(
                                    context, timePicker,
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    false
                                ).show()
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            TimePickerDialog(
                                context, timePicker,
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                false
                            ).show()
                        },
                    colors = transparentColors
                )

            } else {

                // PREVIEW
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(softPink, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedDay, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text(selectedHour, Modifier.weight(1f))
                    Text(selectedAmPm, Modifier.weight(0.6f))

                    Button(
                        onClick = {
                            DatePickerDialog(
                                context, datePicker,
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(Color(0xFFFFA500)),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Edit", color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // SWITCH
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Bật báo thức", color = Color.Gray)
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = enableReminder,
                    onCheckedChange = { enableReminder = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = mainPink,
                        checkedTrackColor = mainPink.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(Modifier.height(30.dp))

            // SAVE BUTTON
            Button(
                onClick = {
                    if (
                        medicineName.isEmpty() ||
                        medicineType == "Chọn dạng thuốc" ||
                        dosage.isEmpty() ||
                        quantity.isEmpty()
                    ) {
                        Toast.makeText(context, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(context, "Đã lưu thuốc!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
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
