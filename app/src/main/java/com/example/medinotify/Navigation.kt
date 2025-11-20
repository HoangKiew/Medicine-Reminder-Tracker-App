package com.example.medinotify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.medinotify.ui.components.BottomNavBar
import com.example.medinotify.ui.screens.addmedicine.AddMedicineScreen
import com.example.medinotify.ui.screens.addmedicine.StartScreen
import com.example.medinotify.ui.screens.home.HomeScreen
import com.example.medinotify.ui.screens.medicinelist.MedicineListScreen
import com.example.medinotify.ui.screens.calendar.CalendarScreen
import com.example.medinotify.ui.screens.profile.ProfileScreen
import com.example.medinotify.ui.screens.medicine.MedicineReminderScreen

@Composable
fun Navigation(navController: NavHostController) {

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != null &&
                !currentRoute.startsWith("medicine_reminder") &&
                currentRoute != "add"
            ) {
                BottomNavBar(navController)
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {

            /* ========= MAIN SCREENS ========= */
            composable("home") { HomeScreen() }
            composable("start") { StartScreen(onStart = { navController.navigate("add") }) }
            composable("add") { AddMedicineScreen(navController) }
            composable("medicine_list") { MedicineListScreen(navController) }
            composable("calendar") { CalendarScreen() }
            composable("profile") { ProfileScreen() }


            /* ========= FCM REMINDER SCREEN ========= */
            composable(
                route = "medicine_reminder/{logId}",
                arguments = listOf(navArgument("logId") { type = NavType.StringType })
            ) { backStackEntry ->

                val logId = backStackEntry.arguments?.getString("logId") ?: ""

                MedicineReminderScreen(
                    logId = logId,

                    // ✅ KHÔNG GỌI API Ở NAVIGATION
                    onTake = { navController.popBackStack() },

                    onLater = { navController.popBackStack() },

                    onMissed = { navController.popBackStack() },

                    onBack = { navController.popBackStack() },

                    onDelete = {}
                )
            }
        }
    }
}
