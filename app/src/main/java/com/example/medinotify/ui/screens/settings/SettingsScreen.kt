package com.example.medinotify.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val route: String? = null,
    val onClick: (() -> Unit)? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
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
                    "Cài đặt",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6395EE)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                // Tài khoản Section
                item {
                    SettingsSectionTitle("Tài khoản")
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column {
                            SettingsMenuItem(
                                icon = Icons.Default.Person,
                                title = "Chỉnh sửa hồ sơ",
                                onClick = { navController.navigate("profile") }
                            )
                            Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                            SettingsMenuItem(
                                icon = Icons.Default.Security,
                                title = "Bảo vệ",
                                onClick = { navController.navigate("settings/account/security") }
                            )
                            Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                            SettingsMenuItem(
                                icon = Icons.Default.Notifications,
                                title = "Thông báo",
                                onClick = { navController.navigate("settings/account/notifications") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Hỗ trợ & Giới thiệu Section
                item {
                    SettingsSectionTitle("Hỗ trợ & Giới thiệu")
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column {
                            Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                            SettingsMenuItem(
                                icon = Icons.Default.Help,
                                title = "Trợ giúp & Hỗ trợ",
                                onClick = { /* TODO */ }
                            )
                            Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                            SettingsMenuItem(
                                icon = Icons.Default.Info,
                                title = "Điều khoản và Chính sách",
                                onClick = { /* TODO */ }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Bộ nhớ đệm và di động Section
                item {
                    SettingsSectionTitle("Bộ nhớ đệm và di động")
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column {
                            SettingsMenuItem(
                                icon = Icons.Default.Delete,
                                title = "Dọn bộ nhớ đệm",
                                onClick = { /* TODO */ }
                            )
                            Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                            SettingsMenuItem(
                                icon = Icons.Default.DataUsage,
                                title = "Tiết kiệm dữ liệu",
                                onClick = { /* TODO */ }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Actions Section
                item {
                    SettingsSectionTitle("Actions")
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column {
                            SettingsMenuItem(
                                icon = Icons.Default.Flag,
                                title = "Báo cáo sự cố",
                                onClick = { /* TODO */ }
                            )
                            Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                            SettingsMenuItem(
                                icon = Icons.Default.PersonAdd,
                                title = "Thêm tài khoản",
                                onClick = { /* TODO */ }
                            )
                            Divider(color = Color(0xFFE0E0E0), thickness = 0.5.dp)
                            SettingsMenuItem(
                                icon = Icons.Default.Logout,
                                title = "Đăng xuất",
                                textColor = Color(0xFFFF5252),
                                onClick = {
                                    // TODO: Handle logout
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp)) // Space for bottom navigation
                }
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (textColor == Color.Black) Color.Gray else textColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            title,
            fontSize = 16.sp,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen(navController = rememberNavController())
    }
}