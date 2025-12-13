package com.example.medinotify.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.repository.MedicineRepository
import com.example.medinotify.data.auth.AuthRepository // ✅ BỔ SUNG: Import AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Data class cho trạng thái của màn hình Profile (Giữ nguyên)
data class ProfileUiState(
    val userName: String = "",
    val email: String = "",
    val dateOfBirth: String = "01/01/1988", // Dữ liệu giả
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val isLoading: Boolean = true,
    val isSignedOut: Boolean = false
)

class ProfileViewModel(
    // ✅ THAY ĐỔI: Inject cả hai Repository để xử lý cả Auth và Data
    private val authRepository: AuthRepository,
    private val medicineRepository: MedicineRepository // Đổi tên để rõ ràng hơn
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        // Đặt trạng thái isLoading = true ngay khi bắt đầu tải
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // ✅ SỬA LỖI: Lấy thông tin User từ AuthRepository
            val firebaseUser = authRepository.getCurrentUser()

            if (firebaseUser != null) {
                _uiState.update {
                    it.copy(
                        userName = firebaseUser.displayName?.takeIf { name -> name.isNotBlank() } ?: "Chưa cập nhật tên",
                        email = firebaseUser.email ?: "Không có email",
                        photoUrl = firebaseUser.photoUrl?.toString(),
                        isEmailVerified = firebaseUser.isEmailVerified,
                        isLoading = false,
                        isSignedOut = false
                    )
                }
            } else {
                // Xử lý trường hợp không có người dùng
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
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                // 1. ✅ SỬA LỖI: Đăng xuất khỏi Firebase (Gọi qua AuthRepository)
                authRepository.signOut()

                // 2. Xóa dữ liệu cục bộ (Room) (Gọi qua MedicineRepository)
                medicineRepository.clearLocalData()

                // 3. Cập nhật UI State
                _uiState.update {
                    it.copy(
                        isSignedOut = true,
                        isLoading = false,
                        userName = "",
                        email = "",
                        photoUrl = null
                    )
                }
            } catch (e: Exception) {
                // Xử lý lỗi nếu có
            }
        }
    }

    /**
     * Hàm để reset lại trạng thái isSignedOut sau khi đã điều hướng.
     */
    fun onSignOutComplete() {
        _uiState.update { it.copy(isSignedOut = false) }
    }
}