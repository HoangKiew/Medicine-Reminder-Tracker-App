package com.example.medinotify.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.auth.AuthRepository // ✅ Cần import AuthRepository
import com.example.medinotify.data.repository.MedicineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Data class cho trạng thái của màn hình Profile (Giữ nguyên)
data class ProfileUiState(
    val userName: String = "",
    val email: String = "",
    val dateOfBirth: String = "01/01/1988",
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val isLoading: Boolean = true,
    val isSignedOut: Boolean = false
)

class ProfileViewModel(
    // ✅ FIX 1: Thay thế repository bằng 2 dependency để khớp với appModule.kt
    private val authRepository: AuthRepository,
    private val medicineRepository: MedicineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // ✅ FIX 2: Lấy thông tin người dùng từ AuthRepository
            val firebaseUser = authRepository.getCurrentUser()

            if (firebaseUser != null) {
                _uiState.update {
                    it.copy(
                        userName = firebaseUser.displayName?.takeIf { name -> name.isNotBlank() } ?: "Chưa cập nhật tên",
                        email = firebaseUser.email ?: "Không có email",
                        photoUrl = firebaseUser.photoUrl?.toString(),
                        isEmailVerified = firebaseUser.isEmailVerified,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSignedOut = true
                    )
                }
            }
        }
    }

    /**
     * Xử lý sự kiện đăng xuất.
     * Hàm này gọi MedicineRepository.signOut() để xóa dữ liệu local và logout Firebase.
     */
    fun signOut() {
        viewModelScope.launch {
            // ✅ FIX 3: Dùng medicineRepository.signOut()
            // (Đã được cấu hình để gọi cả xóa local data và authRepository.signOut() bên trong)
            medicineRepository.signOut()

            _uiState.update { it.copy(isSignedOut = true, isLoading = false) }
        }
    }

    /**
     * Hàm để reset lại trạng thái isSignedOut sau khi đã điều hướng.
     */
    fun onSignOutComplete() {
        _uiState.update { it.copy(isSignedOut = false) }
    }
}