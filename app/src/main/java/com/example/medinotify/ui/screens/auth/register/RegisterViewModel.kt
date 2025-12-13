package com.example.medinotify.ui.screens.auth.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.auth.AuthRepository // ✅ 1. Import AuthRepository
import com.example.medinotify.data.auth.AuthResult     // ✅ 2. Import AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigateToLogin: Boolean = false
)

// ✅ 3. Sửa constructor để nhận AuthRepository từ Koin
class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    // --- Các hàm xử lý input của người dùng (giữ nguyên, đã đúng) ---
    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPhoneChanged(value: String) {
        _uiState.update { it.copy(phone = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    fun onPasswordVisibilityToggle() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onConfirmPasswordVisibilityToggle() {
        _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }
    }

    // --- Hàm đăng ký chính ---
    fun register() {
        val current = _uiState.value
        val trimmedEmail = current.email.trim()

        // Logic validation (giữ nguyên, đã đúng)
        val error = when {
            current.name.trim().length < 3 -> "Vui lòng nhập đầy đủ họ tên."
            !current.name.trim().contains(" ") -> "Vui lòng nhập đầy đủ họ tên."
            trimmedEmail.isBlank() -> "Vui lòng nhập email."
            !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> "Email chưa đúng định dạng."
            current.phone.filter { it.isDigit() }.length !in 9..11 -> "Số điện thoại chưa hợp lệ."
            current.password.length < 6 -> "Mật khẩu cần ít nhất 6 ký tự."
            current.password != current.confirmPassword -> "Mật khẩu nhập lại chưa trùng khớp."
            else -> null
        }

        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        // ✅ 4. Thay thế logic giả lập bằng logic gọi Repository thật
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Gọi hàm signUp từ repository
            val result = authRepository.signUp(trimmedEmail, current.password)

            when (result) {
                is AuthResult.Success -> {
                    // Đăng ký thành công, cập nhật state để điều hướng
                    _uiState.update {
                        it.copy(isLoading = false, navigateToLogin = true)
                    }
                }
                is AuthResult.Error -> {
                    // Có lỗi xảy ra, hiển thị thông báo lỗi
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    // Hàm này giữ nguyên
    fun onNavigationHandled() {
        _uiState.update { it.copy(navigateToLogin = false, errorMessage = null) }
    }

    // Hàm simulateRegisterRequest không còn cần thiết nữa, có thể xóa
    // private suspend fun simulateRegisterRequest() {
    //     delay(600)
    // }
}
