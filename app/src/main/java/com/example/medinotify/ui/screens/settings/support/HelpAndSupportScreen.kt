package com.example.medinotify.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpAndSupportScreen(navController: NavController) {
    // Định nghĩa màu sắc trực tiếp (đã thống nhất)
    val PrimaryBlue = Color(0xFF6395EE)
    val LightGreyBackground = Color(0xFFF5F5F5)

    val context = LocalContext.current
    val reportUrl = "https://forms.gle/VNsjfURN1jxA3WaU7"

    Scaffold(
        containerColor = LightGreyBackground
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
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    "Trợ giúp & Hỗ trợ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nội dung chính
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                HelpAndSupportItem(
                    title = "Báo cáo sự cố đến nhà phát triển",
                    onClick = {

                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reportUrl))
                        context.startActivity(intent)
                    },
                )

                ExpandableDeveloperInfoItem(
                    title = "Thông tin nhà phát triển",
                    primaryBlue = PrimaryBlue
                )
            }
        }
    }
}

@Composable
fun HelpAndSupportItem(
    title: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                title,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}


@Composable
fun DetailRow(label: String, value: String, isBold: Boolean = false, color: Color = Color.Gray) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isBold) Color.Black else Color.Gray,
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            color = color,
        )
    }
}

@Composable
fun ExpandableDeveloperInfoItem(title: String, primaryBlue: Color) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = if (expanded) RoundedCornerShape(topStart = 12.dp,
                topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
            else RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { expanded = !expanded })
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    title,
                    fontSize = 16.sp,
                    color = Color.Black,
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Thu gọn" else "Mở rộng",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(300), expandFrom = Alignment.Top),
            exit = shrinkVertically(animationSpec = tween(300), shrinkTowards = Alignment.Top)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 1.dp),
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp, topStart = 0.dp, topEnd = 0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. THÔNG TIN ĐỒ ÁN
                    Text(
                        "THÔNG TIN ĐỒ ÁN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = primaryBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    DetailRow(
                        label = "Tên đồ án",
                        value = "Đồ án thực tế công nghệ phần mềm",
                        isBold = true,
                        color = Color.Black
                    )
                    DetailRow(
                        label = "Năm học",
                        value = "2024-2025"
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE0E0E0))

                    // 2. THÔNG TIN GIÁO VIÊN
                    Text(
                        "GIÁO VIÊN HƯỚNG DẪN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = primaryBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DetailRow(
                        label = "Giáo viên",
                        value = "TS. Lê Văn Quốc Anh",
                        isBold = true,
                        color = Color.Black
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE0E0E0))

                    Text(
                        "THÀNH VIÊN THỰC HIỆN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = primaryBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "• Hoàng Mai Kiều (MSSV: 067305001315)",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        "• Lương Thị Ánh Tuyết (MSSV: 067305001563)",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        "• Kiều Trần Thu Uyên (MSSV: 064305005016)",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE0E0E0))

                    Text(
                        "HỖ TRỢ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = primaryBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "Email hỗ trợ: support@medinotify.com",
                        fontSize = 14.sp,
                        color = primaryBlue,
                        modifier = Modifier.clickable { /* TODO: Xử lý mở ứng dụng email */ }
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HelpAndSupportScreenPreview() {
    MaterialTheme {
        HelpAndSupportScreen(navController = rememberNavController())
    }
}