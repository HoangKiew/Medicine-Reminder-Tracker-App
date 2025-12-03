package com.example.medinotify

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.medinotify.ui.components.BottomBar
import com.example.medinotify.ui.screens.addmedicine.AddMedicineScreen
import com.example.medinotify.ui.screens.addmedicine.StartScreen
import com.example.medinotify.ui.screens.home.HomeScreen
import com.example.medinotify.ui.screens.calendar.CalendarScreen
import com.example.medinotify.ui.screens.history.MedicineHistoryScreen
import com.example.medinotify.ui.screens.history.MedicineHistoryDetailScreen

@Composable
fun Navigation(navController: NavHostController) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ⭐ Hiển thị bottom bar ở Home + Start
    val showBottomBar = currentRoute in listOf("home", "start")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {

            // ---------------- HOME ---------------------
            composable("home") {
                HomeScreen(navController = navController)
            }

            // ---------------- CALENDAR -----------------
            composable("calendar") {
                CalendarScreen(navController = navController)
            }

            // ---------------- MEDICINE HISTORY ---------
            composable("medicine_history") {
                MedicineHistoryScreen(navController = navController)
            }

            // ---------------- MEDICINE HISTORY DETAIL --
            composable(
                route = "medicine_history_detail/{date}",
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date") ?: ""
                MedicineHistoryDetailScreen(
                    navController = navController,
                    date = date
                )
            }

            // ---------------- START (thêm thuốc) -------
            composable("start") {
                StartScreen(
                    onStart = {
                        navController.navigate("add") {
                            launchSingleTop = true   // tránh mở lại nhiều lần
                        }
                    }
                )
            }

            // ---------------- ADD MEDICINE -------------
            composable("add") {
                AddMedicineScreen(navController)
            }
        }
    }
}