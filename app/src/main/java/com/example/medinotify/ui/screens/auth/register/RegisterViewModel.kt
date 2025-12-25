package com.example.medinotify.ui.screens.auth.register

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.auth.AuthRepository
import com.example.medinotify.data.auth.AuthResult
import com.example.medinotify.data.repository.MedicineRepository
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
    val navigateToLogin: Boolean = false,
    val isRegistered: Boolean = false
)

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val medicineRepository: MedicineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

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

    fun register() {
        val current = _uiState.value
        val trimmedName = current.name.trim()
        val trimmedEmail = current.email.trim()
        val digitsOnlyPhone = current.phone.filter { it.isDigit() }

        // --- Validation Logic ---
        val error = when {
            trimmedName.length < 3 -> "Vui lòng nhập đầy đủ họ tên (tối thiểu 3 ký tự)."
            !trimmedName.contains(" ") -> "Vui lòng nhập cả họ và tên."
            trimmedEmail.isBlank() -> "Vui lòng nhập email."
            !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> "Email chưa đúng định dạng."
            digitsOnlyPhone.length !in 9..11 -> "Số điện thoại không hợp lệ."
            current.password.length < 6 -> "Mật khẩu cần ít nhất 6 ký tự."
            current.password != current.confirmPassword -> "Mật khẩu nhập lại không khớp."
            else -> null
        }

        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Gọi hàm đăng ký
            val result = authRepository.signUp(
                email = trimmedEmail,
                pass = current.password,
                name = trimmedName
            )

            when (result) {
                is AuthResult.Success -> {

                    try {
                        medicineRepository.syncDataFromFirebase()
                        Log.d("RegisterVM", "Đồng bộ dữ liệu ban đầu thành công.")
                    } catch (e: Exception) {
                        Log.e("RegisterVM", "Lỗi đồng bộ sau khi đăng ký: ${e.message}")
                        // Không chặn luồng chính, vẫn cho user vào app nhưng ghi log lỗi
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRegistered = true,
                            navigateToLogin = true
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun onNavigationHandled() {
        _uiState.update {
            it.copy(
                navigateToLogin = false,
                isRegistered = false
            )
        }
    }
}