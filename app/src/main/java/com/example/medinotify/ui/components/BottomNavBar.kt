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

        // ---------------- HOME ----------------
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = {
                navController.navigate("home") {
                    launchSingleTop = true
                    popUpTo("home")
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Trang chủ") }
        )

        // ---------------- LIST ----------------
        NavigationBarItem(
            selected = currentRoute == "medicine_list",
            onClick = {
                navController.navigate("medicine_list") {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Medication, contentDescription = "Danh sách thuốc") },
            label = { Text("Danh sách") }
        )

        // ---------------- ADD ----------------
        NavigationBarItem(
            selected = currentRoute == "start",
            onClick = {
                navController.navigate("start") {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Add, contentDescription = "Thêm") },
            label = { Text("Thêm") }
        )

        // ---------------- CALENDAR ----------------
        NavigationBarItem(
            selected = currentRoute == "calendar",
            onClick = {
                navController.navigate("calendar") {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Lịch") },
            label = { Text("Lịch") }
        )

        // ---------------- PROFILE ----------------
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = {
                navController.navigate("profile") {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Cá nhân") },
            label = { Text("Cá nhân") }
        )
    }
}
