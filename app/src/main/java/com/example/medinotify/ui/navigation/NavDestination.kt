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

    object MedicineReminder : NavDestination("medicine_reminder")

    object Profile : NavDestination("profile")
    object EditProfile : NavDestination("edit_profile")
    object Settings : NavDestination("settings")
    object HelpAndSupport : NavDestination("settings/support/help_and_support")
    object Account : NavDestination("settings/account")
    object Notifications : NavDestination("settings/account/notifications")



    companion object {
        val startDestination = Splash.route
    }
}