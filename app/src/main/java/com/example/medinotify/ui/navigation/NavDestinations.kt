package com.example.medinotify.ui.navigation

sealed class NavDestination(val route: String) {
    data object Splash : NavDestination("splash")
    data object Login : NavDestination("login")
    data object Register : NavDestination("register")
    data object ForgotPassword : NavDestination("forgot_password")
    data object VerifyCode : NavDestination("verify_code")
    data object ResetPassword : NavDestination("reset_password")
    data object ResetPasswordSuccess : NavDestination("reset_password_success")

    companion object {
        val startDestination = Splash.route
    }
}

