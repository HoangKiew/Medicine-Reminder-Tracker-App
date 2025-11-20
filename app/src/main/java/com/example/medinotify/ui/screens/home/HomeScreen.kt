package com.example.medinotify.ui.screens.home

import androidx.compose.foundation.*
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
import com.example.medinotify.R
import com.example.medinotify.viewmodel.MedicineViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(vm: MedicineViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var selectedDate by remember { mutableStateOf(sdf.format(Date())) }

    // Load lịch uống trong ngày
    val schedule = vm.daySchedule

    LaunchedEffect(selectedDate) {
        vm.loadScheduleByDate("U001", selectedDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 40.dp)
    ) {

        /* ---------------- TOP BAR ---------------- */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.DateRange, null, tint = Color(0xFFFF5A5A), modifier = Modifier.size(28.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Person, null, tint = Color(0xFF355CFF), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(18.dp))
                Icon(Icons.Filled.Settings, null, tint = Color.DarkGray, modifier = Modifier.size(28.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        /* ---------------- TITLE ---------------- */
        Text(
            "Hôm nay",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C60FF),
            modifier = Modifier.padding(start = 20.dp)
        )

        Spacer(modifier = Modifier.height(15.dp))

        /* ---------------- DATE SELECTOR ---------------- */
        DaySelector(
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )

        Spacer(modifier = Modifier.height(22.dp))

        /* ---------------- SUMMARY ---------------- */

        val total = schedule.size
        val taken = schedule.count { it.status == "Taken" }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "Lượng thuốc tiêu thụ",
                fontSize = 18.sp,
                color = Color(0xFF2C60FF),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color(0xFFEFF6FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Image(
                        painterResource(id = R.drawable.medicine_start),
                        contentDescription = null,
                        modifier = Modifier.size(55.dp)
                    )

                    Text(
                        "$taken/$total",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF355CFF)
                    )

                    val weekday = SimpleDateFormat("EEEE", Locale.getDefault())
                        .format(sdf.parse(selectedDate)!!)

                    Text(weekday, fontSize = 14.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        /* ---------------- LIST MEDICINES ---------------- */

        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            items(schedule) { item ->

                MedicineCard(
                    name = item.name ?: "Không tên",
                    description = "${item.medicineType ?: ""} ${item.dosage ?: ""}",
                    time = item.time ?: "--:--",
                    isTaken = item.status == "Taken",
                    onClick = { /* NO ACTION — VIEW ONLY */ }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/* ------------------------- MEDICINE CARD ----------------------------- */

@Composable
fun MedicineCard(
    name: String,
    description: String,
    time: String,
    isTaken: Boolean,
    onClick: () -> Unit
) {

    val bgColor = if (isTaken) Color(0xFFDBFFE8) else Color(0xFFFFE3E3)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(16.dp))
            .padding(16.dp)   // ⭐ Không clickable nữa
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
                    Icon(Icons.Filled.Info, null, tint = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(name, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(description, fontSize = 13.sp, color = Color.Gray)
                }
            }

            Box(
                modifier = Modifier
                    .background(Color(0xFF2C60FF), RoundedCornerShape(10.dp))
                    .padding(vertical = 6.dp, horizontal = 14.dp)
            ) {
                Text(
                    time,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/* ------------------------- DAY SELECTOR ----------------------------- */

@Composable
fun DaySelector(
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val weekDays = (0..6).map {
        Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY + it)
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(weekDays) { date ->

            val formatted = sdf.format(date.time)
            val number = date.get(Calendar.DAY_OF_MONTH).toString()
            val label = date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) ?: ""

            val isSelected = formatted == selectedDate

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
                    .clickable { onDateSelected(formatted) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(number, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    label,
                    fontSize = 12.sp,
                    color = if (isSelected) Color(0xFF2C60FF) else Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
