// File: app/src/main/java/com/example/medinotify/ui/navigation/NavDestinations.kt
package com.example.medinotify.ui.navigation

sealed class NavDestination(val route: String) {
    object Splash : NavDestination("splash")
    object Login : NavDestination("login")
    object Register : NavDestination("register")
    object ForgotPassword : NavDestination("forgot_password")
    object VerifyCode : NavDestination("verify_code")
    object ResetPassword : NavDestination("reset_password")
    object ResetPasswordSuccess : NavDestination("reset_password_success")

    companion object {
        const val startDestination = "splash"  // hoặc dùng Splash.route cũng được
    }
}