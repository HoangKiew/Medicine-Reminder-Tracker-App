package com.example.medinotify.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController

@Composable
fun BottomBar(navController: NavHostController, currentRoute: String?) {

    NavigationBar(containerColor = Color.White) {

        // ------------------- HOME -------------------
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Trang chủ") }
        )

        // ------------------- THÊM THUỐC → STARTSCREEN -------------------
        NavigationBarItem(
            selected = currentRoute == "start",
            onClick = {
                navController.navigate("start") {
                    popUpTo("home") { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Add, contentDescription = "Add Medicine") },
            label = { Text("Thêm thuốc") }
        )
    }
}
