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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medinotify.R

// ----------------------------- DATA CLASS -------------------------
data class Day(val number: String, val label: String)

data class MedicineItem(
    val name: String,
    val description: String,
    val time: String,
    val isTaken: Boolean     // ⭐ TRUE = xanh, FALSE = đỏ
)

// ----------------------------- DATE SELECTOR -------------------------

@Composable
fun DaySelector(
    selectedDay: String,
    onDaySelected: (String) -> Unit
) {
    val daysOfWeek = listOf(
        Day("2", "MON"),
        Day("3", "TUE"),
        Day("4", "WED"),
        Day("5", "THU"),
        Day("6", "FRI"),
        Day("7", "SAT"),
        Day("8", "SUN")
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(daysOfWeek) { day ->
            val isSelected = day.number == selectedDay

            Column(
                modifier = Modifier
                    .width(60.dp)             // ⭐ ỔN ĐỊNH – KHÔNG BÉ LẠI
                    .height(78.dp)
                    .background(
                        Color.White,
                        RoundedCornerShape(18.dp)
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Color(0xFF2C60FF) else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { onDaySelected(day.number) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = day.number,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = day.label,
                    fontSize = 12.sp,
                    color = if (isSelected) Color(0xFF2C60FF) else Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ----------------------------- MEDICINE CARD -------------------------

@Composable
fun MedicineCard(item: MedicineItem) {

    // 🔥 Màu theo trạng thái (giống hình bạn gửi)
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
                Text(
                    item.time,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ----------------------------- MAIN HOMESCREEN -------------------------

@Composable
fun HomeScreen() {

    var selectedDay by remember { mutableStateOf("7") }

    // 🔥 Thêm isTaken để phân màu
    val medicineList = listOf(
        MedicineItem("Vitamin D", "1 Viên nén, 1000mg", "09:41", true),
        MedicineItem("B12 Drops", "5 Viên, 1000mg", "06:13", false)
    )

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
            Icon(Icons.Filled.DateRange, null, tint = Color(0xFFFF5A5A), modifier = Modifier.size(28.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Person, null, tint = Color(0xFF355CFF), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(18.dp))
                Icon(Icons.Filled.Settings, null, tint = Color.DarkGray, modifier = Modifier.size(28.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ------------------ TITLE ----------------------
        Text(
            "Hôm nay",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C60FF),
            modifier = Modifier.padding(start = 20.dp)
        )

        Spacer(modifier = Modifier.height(15.dp))

        // ------------------ DATE SELECTOR ----------------------
        DaySelector(
            selectedDay = selectedDay,
            onDaySelected = { selectedDay = it }
        )

        Spacer(modifier = Modifier.height(22.dp))

        // ------------------ SUMMARY CIRCLE ----------------------
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

                    Text("1/2", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF355CFF))
                    Text("Tuesday", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ------------------ LIST MEDICINE ----------------------
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            items(medicineList) { item ->
                MedicineCard(item)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
