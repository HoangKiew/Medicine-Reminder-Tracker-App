package com.example.addmedicine.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.addmedicine.AddMedicineScreen
import com.example.addmedicine.StartScreen

@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "start"
    ) {

        composable("start") {
            StartScreen(onStart = {
                navController.navigate("add")
            })
        }

        composable("add") {
            AddMedicineScreen(navController)
        }
    }
}
