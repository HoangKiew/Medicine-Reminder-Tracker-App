// File: com.example.medinotify/Navigation.kt
package com.example.medinotify

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.medinotify.ui.components.BottomNavBar
import com.example.medinotify.ui.navigation.NavDestination
import com.example.medinotify.ui.screens.addmedicine.AddMedicineScreen
import com.example.medinotify.ui.screens.addmedicine.StartScreen
import com.example.medinotify.ui.screens.auth.login.LoginRoute
import com.example.medinotify.ui.screens.auth.password.ForgotPasswordRoute
import com.example.medinotify.ui.screens.auth.password.ResetPasswordRoute
import com.example.medinotify.ui.screens.auth.password.ResetPasswordSuccessScreen
import com.example.medinotify.ui.screens.auth.password.VerifyCodeRoute
import com.example.medinotify.ui.screens.auth.register.RegisterRoute
import com.example.medinotify.ui.screens.auth.splash.SplashScreen
import com.example.medinotify.ui.screens.calendar.CalendarScreen
import com.example.medinotify.ui.screens.home.HomeScreen
import com.example.medinotify.ui.screens.medicine.MedicineReminderScreen
import com.example.medinotify.ui.screens.medicinelist.MedicineListScreen
import com.example.medinotify.ui.screens.profile.ProfileScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String                          // ĐÃ XÓA = "auth_graph" → BẮT BUỘC MainActivity phải truyền đúng!
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hiện BottomBar chỉ khi đang ở các màn hình chính
    val showBottomBar = currentRoute?.startsWith("main/") == true &&
            !currentRoute.contains("medicine_reminder") &&
            !currentRoute.contains("add") &&
            !currentRoute.contains("start")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ====================== AUTH GRAPH ======================
            navigation(
                startDestination = NavDestination.Splash.route,
                route = "auth_graph"
            ) {
                composable(NavDestination.Splash.route) {
                    SplashScreen(
                        onLogin = {
                            navController.navigate(NavDestination.Login.route) {
                                popUpTo(NavDestination.Splash.route) { inclusive = true }
                            }
                        },
                        onRegister = { navController.navigate(NavDestination.Register.route) }
                    )
                }

                composable(NavDestination.Login.route) {
                    LoginRoute(
                        onRegister = { navController.navigate(NavDestination.Register.route) },
                        onContinue = {
                            navController.navigate("main_graph") {
                                popUpTo("auth_graph") { inclusive = true }
                            }
                        },
                        onBack = {
                            navController.navigate(NavDestination.Splash.route) {
                                popUpTo(NavDestination.Login.route) { inclusive = true }
                            }
                        },
                        onForgotPassword = { navController.navigate(NavDestination.ForgotPassword.route) }
                    )
                }

                composable(NavDestination.Register.route) {
                    RegisterRoute(
                        onBack = { navController.popBackStack() },
                        onRegisterSuccess = {
                            navController.navigate("main_graph") {
                                popUpTo("auth_graph") { inclusive = true }
                            }
                        },
                        onLogin = { navController.popBackStack() }
                    )
                }

                composable(NavDestination.ForgotPassword.route) {
                    ForgotPasswordRoute(
                        onBack = { navController.popBackStack() },
                        onSendCode = { navController.navigate(NavDestination.VerifyCode.route) }
                    )
                }

                composable(NavDestination.VerifyCode.route) {
                    VerifyCodeRoute(
                        onBack = { navController.popBackStack() },
                        onConfirm = { navController.navigate(NavDestination.ResetPassword.route) }
                    )
                }

                composable(NavDestination.ResetPassword.route) {
                    ResetPasswordRoute(
                        onBack = { navController.popBackStack() },
                        onReset = {
                            navController.navigate(NavDestination.ResetPasswordSuccess.route) {
                                popUpTo(NavDestination.ResetPassword.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(NavDestination.ResetPasswordSuccess.route) {
                    ResetPasswordSuccessScreen(
                        onBackToLogin = {
                            navController.navigate(NavDestination.Login.route) {
                                popUpTo("auth_graph") { inclusive = true }
                            }
                        }
                    )
                }
            }

            // ====================== MAIN APP GRAPH ======================
            navigation(
                startDestination = "main/home",
                route = "main_graph"
            ) {
                composable("main/home") { HomeScreen() }
                composable("main/start") {
                    StartScreen(onStart = { navController.navigate("main/add") })
                }
                composable("main/add") {
                    AddMedicineScreen(navController = navController)
                }
                composable("main/medicine_list") {
                    MedicineListScreen(navController = navController)
                }
                composable("main/calendar") { CalendarScreen() }
                composable("main/profile") { ProfileScreen() }

                composable(
                    route = "main/medicine_reminder/{logId}",
                    arguments = listOf(navArgument("logId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val logId = backStackEntry.arguments?.getString("logId") ?: ""
                    MedicineReminderScreen(
                        logId = logId,
                        onTake = { navController.popBackStack() },
                        onLater = { navController.popBackStack() },
                        onMissed = { navController.popBackStack() },
                        onBack = { navController.popBackStack() },
                        onDelete = { }
                    )
                }
            }
        }
    }
}