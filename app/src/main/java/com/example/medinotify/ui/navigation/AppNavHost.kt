package com.example.medinotify.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.medinotify.ui.components.BottomBar
import com.example.medinotify.ui.screens.addmedicine.AddMedicineScreen
import com.example.medinotify.ui.screens.addmedicine.StartScreen
import com.example.medinotify.ui.screens.calendar.CalendarScreen
import com.example.medinotify.ui.screens.history.MedicineHistoryDetailScreen
import com.example.medinotify.ui.screens.history.MedicineHistoryScreen
import com.example.medinotify.ui.screens.home.HomeScreen
import com.example.medinotify.ui.screens.auth.login.LoginRoute
import com.example.medinotify.ui.screens.auth.password.ForgotPasswordRoute
import com.example.medinotify.ui.screens.auth.password.ResetPasswordRoute
import com.example.medinotify.ui.screens.auth.password.ResetPasswordSuccessScreen
import com.example.medinotify.ui.screens.auth.password.VerifyCodeRoute
import com.example.medinotify.ui.screens.auth.register.RegisterRoute
import com.example.medinotify.ui.screens.auth.splash.SplashScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType

@Composable
fun MedinotifyApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        NavDestination.Home.route,
        NavDestination.StartAddMedicine.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = NavDestination.startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            authGraph(navController)
            mainGraph(navController)
        }
    }
}

private fun NavHostController.navigateToHome() {
    this.navigate(NavDestination.Home.route) {
        popUpTo(NavDestination.Splash.route) { inclusive = true }
        launchSingleTop = true
    }
}

private fun androidx.navigation.NavGraphBuilder.authGraph(navController: NavHostController) {
    composable(NavDestination.Splash.route) {
        SplashScreen(
            onLogin = { navController.navigate(NavDestination.Login.route) },
            onRegister = { navController.navigate(NavDestination.Register.route) }
        )
    }

    composable(NavDestination.Login.route) {
        LoginRoute(
            onRegister = { navController.navigate(NavDestination.Register.route) },
            onForgotPassword = { navController.navigate(NavDestination.ForgotPassword.route) },
            onBack = { navController.popBackStack() },
            onContinue = { navController.navigateToHome() }
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
            onReset = { navController.navigate(NavDestination.ResetPasswordSuccess.route) }
        )
    }

    composable(NavDestination.ResetPasswordSuccess.route) {
        ResetPasswordSuccessScreen(
            onBackToLogin = { navController.navigate(NavDestination.Login.route) {
                popUpTo(NavDestination.Splash.route) { inclusive = true }
            } }
        )
    }

    composable(NavDestination.Register.route) {
        RegisterRoute(
            onBack = { navController.popBackStack() },
            onRegisterSuccess = { navController.navigateToHome() },
            onLogin = { navController.popBackStack() }
        )
    }
}

private fun androidx.navigation.NavGraphBuilder.mainGraph(navController: NavHostController) {
    composable(NavDestination.Home.route) { HomeScreen(navController) }
    composable(NavDestination.Calendar.route) { CalendarScreen(navController) }
    composable(NavDestination.MedicineHistory.route) { MedicineHistoryScreen(navController) }
    composable(
        route = NavDestination.MedicineHistoryDetail.route,
        arguments = listOf(navArgument("date") { type = NavType.StringType })
    ) { backStackEntry ->
        val date = backStackEntry.arguments?.getString("date") ?: ""
        MedicineHistoryDetailScreen(navController, date)
    }
    composable(NavDestination.StartAddMedicine.route) {
        StartScreen {
            navController.navigate(NavDestination.AddMedicine.route) {
                launchSingleTop = true
            }
        }
    }
    composable(NavDestination.AddMedicine.route) { AddMedicineScreen(navController) }
}
