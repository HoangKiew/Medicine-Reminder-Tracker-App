package com.example.medinotify.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.medinotify.ui.navigation.NavDestination

@Composable
fun BottomBar(navController: NavHostController, currentRoute: String?) {

    NavigationBar(containerColor = Color.White) {

        // ------------------- HOME -------------------
        NavigationBarItem(
            selected = currentRoute == NavDestination.Home.route,
            onClick = {
                navController.navigate(NavDestination.Home.route) {
                    popUpTo(NavDestination.Home.route) { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Trang chủ") }
        )

        // ------------------- THÊM THUỐC → STARTSCREEN -------------------
        NavigationBarItem(
            selected = currentRoute == NavDestination.StartAddMedicine.route,
            onClick = {
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
