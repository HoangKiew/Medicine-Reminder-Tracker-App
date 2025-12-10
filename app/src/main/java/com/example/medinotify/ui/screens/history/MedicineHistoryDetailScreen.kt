package com.example.medinotify.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineHistoryDetailScreen(navController: NavController, date: String = "15/05/2025") {
    var searchQuery by remember { mutableStateOf("") }

    // Danh sách thuốc với trạng thái đã uống/chưa uống
    val allMedicineList = remember {
        listOf(
            MedicineHistory("Vitamin D", "1 viên nang, 1000mg", "09:41", true),  // Đã uống
            MedicineHistory("Viên nang B12", "5 Viên, 1000mg", "06:13", false), // Chưa uống
            MedicineHistory("Paracetamol", "2 viên, 500mg", "14:30", true),
            MedicineHistory("Vitamin C", "1 viên, 1000mg", "08:00", false),
            MedicineHistory("Omega 3", "2 viên, 500mg", "10:15", true)
        )
    }

    // Lọc danh sách theo tên thuốc
    val filteredList = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            allMedicineList
        } else {
            allMedicineList.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.DateRange,
                    contentDescription = "Calendar",
                    tint = Color(0xFFFF5A5A),
                    modifier = Modifier.size(28.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color(0xFF6395EE),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(18.dp))
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                "Lịch sử uống thuốc",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6395EE),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date display with search
            OutlinedTextField(
                value = date,
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = { Text("dd/mm/yyyy") },
                trailingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF6395EE),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                readOnly = true,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search by medicine name
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = { Text("Tìm kiếm theo tên thuốc") },
                trailingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF6395EE),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Medicine list with status
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Không tìm thấy thuốc",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    items(filteredList) { medicine ->
                        MedicineHistoryItemWithStatus(medicine = medicine)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Back to home button
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6395EE)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Quay lại", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun MedicineHistoryItemWithStatus(medicine: MedicineHistory) {
    // Màu nền: xanh = đã uống (isTaken = true), đỏ = chưa uống (isTaken = false)
    val backgroundColor = if (medicine.isTaken) {
        Color(0xFFE8F5E9) // Xanh lá nhạt - đã uống
    } else {
        Color(0xFFFFEBEE) // Đỏ nhạt - chưa uống
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
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
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFFC700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💊", fontSize = 24.sp)
                }

                // Medicine info
                Column {
                    Text(
                        text = medicine.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = medicine.dosage,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            // Time
            Surface(
                color = Color(0xFF6395EE),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = medicine.time,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}