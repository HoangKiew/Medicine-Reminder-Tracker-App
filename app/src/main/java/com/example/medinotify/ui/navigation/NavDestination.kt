package com.example.medinotify.ui.navigation

sealed class NavDestination(val route: String) {
    // AUTH
    object Splash : NavDestination("splash")
    object Login : NavDestination("login")
    object Register : NavDestination("register")
    object ForgotPassword : NavDestination("forgot_password")
    object VerifyCode : NavDestination("verify_code")
    object ResetPassword : NavDestination("reset_password")
    object ResetPasswordSuccess : NavDestination("reset_password_success")

    // MAIN APP
    object Home : NavDestination("home")
    object Calendar : NavDestination("calendar")
    object MedicineHistory : NavDestination("medicine_history")
    object MedicineHistoryDetail : NavDestination("medicine_history_detail/{date}") {
        fun createRoute(date: String) = "medicine_history_detail/$date"
    }
    object StartAddMedicine : NavDestination("start")
    object AddMedicine : NavDestination("add")

    // PROFILE & SETTINGS
    object Profile : NavDestination("profile")
    object Settings : NavDestination("settings")

    companion object {
        val startDestination = Splash.route
    }
}