package com.example.medinotify.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.medinotify.ui.navigation.NavDestination // ✨ BƯỚC 1: Import lớp NavDestination

@Composable
fun BottomBar(navController: NavHostController, currentRoute: String?) {

    NavigationBar(containerColor = Color.White) {

        // ------------------- HOME -------------------
        NavigationBarItem(
            // ✨ SỬA LỖI: So sánh với route từ NavDestination
            selected = currentRoute == NavDestination.Home.route,
            onClick = {
                // ✨ SỬA LỖI: Điều hướng bằng route từ NavDestination
                navController.navigate(NavDestination.Home.route) {
                    // Dùng route đúng để popUpTo
                    popUpTo(NavDestination.Home.route) { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Trang chủ") }
        )

        // ------------------- THÊM THUỐC → STARTSCREEN -------------------
        NavigationBarItem(
            // ✨ SỬA LỖI: So sánh với route đúng của luồng thêm thuốc
            selected = currentRoute == NavDestination.StartAddMedicine.route,
            onClick = {
                // ✨ SỬA LỖI: Điều hướng đến màn hình bắt đầu của luồng thêm thuốc
                navController.navigate(NavDestination.StartAddMedicine.route) {
                    // Dùng route đúng để popUpTo
                    popUpTo(NavDestination.Home.route) { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Add, contentDescription = "Add Medicine") },
            label = { Text("Thêm thuốc") }
        )
    }
}
