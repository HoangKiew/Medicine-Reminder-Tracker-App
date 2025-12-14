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
    // ✅ Dependency Injection: Nhận cả Auth và Medicine Repo
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

        // --- Validation Logic (Giữ nguyên) ---
        val error = when {
            trimmedName.length < 3 -> "Vui lòng nhập đầy đủ họ tên."
            !trimmedName.contains(" ") -> "Vui lòng nhập đầy đủ họ tên."
            trimmedEmail.isBlank() -> "Vui lòng nhập email."
            !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> "Email chưa đúng định dạng."
            digitsOnlyPhone.length !in 9..11 -> "Số điện thoại chưa hợp lệ."
            current.password.length < 6 -> "Mật khẩu cần ít nhất 6 ký tự."
            current.password != current.confirmPassword -> "Mật khẩu nhập lại chưa trùng khớp."
            else -> null
        }

        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // ✅ FIX LỖI: Gọi hàm signUp với tên tham số (pass, name) khớp với AuthRepository
            val result = authRepository.signUp(
                email = trimmedEmail,
                pass = current.password,     // Dùng 'pass'
                name = trimmedName           // Thêm 'name'
            )

            when (result) {
                is AuthResult.Success -> {
                    try {
                        // KÍCH HOẠT ĐỒNG BỘ: Tải dữ liệu mẫu/mới về Room
                        medicineRepository.syncDataFromFirebase()
                        Log.d("RegisterVM", "Data synced successfully after registration.")

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRegistered = true,
                                navigateToLogin = true
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("RegisterVM", "Failed to sync data after registration: ${e.message}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Đăng ký thành công, nhưng không thể tải dữ liệu ban đầu."
                            )
                        }
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