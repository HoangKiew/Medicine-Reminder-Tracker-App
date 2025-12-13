package com.example.medinotify.ui.navigation

/**
 * Lớp niêm phong (sealed class) định nghĩa tất cả các màn hình (đích đến) trong ứng dụng.
 * Việc sử dụng sealed class giúp quản lý các tuyến đường một cách an toàn và tránh lỗi gõ sai chuỗi.
 */
sealed class NavDestination(val route: String) {
    // ---- Nhóm màn hình Xác thực (Auth) ----
    object Splash : NavDestination("splash")
    object Login : NavDestination("login")
    object Register : NavDestination("register")
    object ForgotPassword : NavDestination("forgot_password")
    object VerifyCode : NavDestination("verify_code")
    object ResetPassword : NavDestination("reset_password")
    object ResetPasswordSuccess : NavDestination("reset_password_success")

    // ---- Nhóm màn hình Chính (có BottomBar) ----
    object Home : NavDestination("home")
    object Calendar : NavDestination("calendar")
    object MedicineHistory : NavDestination("medicine_history")
    object Profile : NavDestination("profile")

    object HelpAndSupport : NavDestination("settings/help_and_support")

    // ---- Nhóm màn hình Chi tiết & Luồng chức năng ----
    // Màn hình chi tiết lịch sử (sử dụng tham số 'date')
    object MedicineHistoryDetail : NavDestination("medicine_history_detail/{date}") {
        // Hàm trợ giúp để tạo route hoàn chỉnh với một ngày cụ thể
        fun createRoute(date: String) = "medicine_history_detail/$date"
    }

    // Luồng thêm thuốc
    object StartAddMedicine : NavDestination("start_add_medicine") // Sửa đổi
    object AddMedicine : NavDestination("add_medicine?medicineId={medicineId}") {
        fun createRoute(medicineId: String? = null): String {
            return if (medicineId != null) "add_medicine?medicineId=$medicineId" else "add_medicine"
        }
    }        // Sửa đổi

    // ---- Nhóm màn hình Cài đặt ----
    object Settings : NavDestination("settings")
    object Notifications : NavDestination("settings/notifications") // Sửa đổi
    object Security : NavDestination("settings/security")           // Sửa đổi

    companion object {
        /**
         * Màn hình khởi đầu của toàn bộ ứng dụng.
         * Sử dụng const để tối ưu hóa hiệu suất tại thời điểm biên dịch.
         */
        const val START_DESTINATION = "splash"
    }
}
