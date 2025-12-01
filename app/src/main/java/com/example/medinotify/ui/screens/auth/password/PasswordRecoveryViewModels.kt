package com.example.medinotify.ui.screens.auth.password

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigateNext: Boolean = false
)

class ForgotPasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.update { current ->
            current.copy(email = value, errorMessage = null)
        }
    }

    fun sendCode() {
        val email = _uiState.value.email.trim()
        val error = when {
            email.isBlank() -> "Vui lòng nhập email."
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email chưa đúng định dạng."
            else -> null
        }

        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            simulateNetworkDelay()
            _uiState.update { it.copy(isLoading = false, navigateNext = true) }
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(navigateNext = false) }
    }
}

data class VerifyCodeUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val navigateNext: Boolean = false
)

class VerifyCodeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyCodeUiState())
    val uiState = _uiState.asStateFlow()

    fun onCodeChanged(value: String) {
        _uiState.update { current ->
            val digitsOnly = value.filter { it.isDigit() }.take(OTP_CODE_LENGTH)
            current.copy(code = digitsOnly, errorMessage = null)
        }
    }

    fun confirmCode() {
        val code = _uiState.value.code
        if (code.length < OTP_CODE_LENGTH) {
            _uiState.update { it.copy(errorMessage = "Mã xác minh gồm $OTP_CODE_LENGTH số.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            simulateNetworkDelay()
            _uiState.update { it.copy(isLoading = false, navigateNext = true) }
        }
    }

    fun resendCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            simulateNetworkDelay()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    infoMessage = "Đã gửi lại mã xác minh."
                )
            }
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(navigateNext = false) }
    }

    fun onInfoMessageShown() {
        _uiState.update { it.copy(infoMessage = null) }
    }
}

data class ResetPasswordUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigateNext: Boolean = false
)

class ResetPasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState = _uiState.asStateFlow()

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

    fun resetPassword() {
        val state = _uiState.value
        val error = when {
            state.password.length < 6 -> "Mật khẩu cần ít nhất 6 ký tự."
            state.password != state.confirmPassword -> "Mật khẩu nhập lại chưa trùng khớp."
            else -> null
        }

        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            simulateNetworkDelay()
            _uiState.update { it.copy(isLoading = false, navigateNext = true) }
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(navigateNext = false) }
    }
}

private suspend fun simulateNetworkDelay() {
    delay(300)
}


