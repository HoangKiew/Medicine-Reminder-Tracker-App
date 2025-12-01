// File: app/src/main/java/com/example/medinotify/ui/components/BottomNavBar.kt
package com.example.medinotify.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavController) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        // HOME
        NavigationBarItem(
            selected = currentRoute == "main/home",
            onClick = {
                navController.navigate("main/home") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
            label = { Text("Trang chủ") }
        )

        // DANH SÁCH THUỐC
        NavigationBarItem(
            selected = currentRoute == "main/medicine_list",
            onClick = {
                navController.navigate("main/medicine_list") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Medication, contentDescription = "Danh sách thuốc") },
            label = { Text("Danh sách") }
        )

        // THÊM THUỐC (màn StartScreen)
        NavigationBarItem(
            selected = currentRoute == "main/start",
            onClick = {
                navController.navigate("main/start") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Add, contentDescription = "Thêm thuốc") },
            label = { Text("Thêm") }
        )

        // LỊCH
        NavigationBarItem(
            selected = currentRoute == "main/calendar",
            onClick = {
                navController.navigate("main/calendar") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Lịch") },
            label = { Text("Lịch") }
        )

        // CÁ NHÂN
        NavigationBarItem(
            selected = currentRoute == "main/profile",
            onClick = {
                navController.navigate("main/profile") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Cá nhân") },
            label = { Text("Cá nhân") }
        )
    }
}