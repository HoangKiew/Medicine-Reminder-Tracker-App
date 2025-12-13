package com.example.medinotify.ui.screens.profile

import androidx.compose.animation.core.copy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.repository.MedicineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Data class cho trạng thái của màn hình Profile (Không thay đổi)
data class ProfileUiState(
    val userName: String = "",
    val email: String = "",
    val dateOfBirth: String = "01/01/1988", // Dữ liệu giả, có thể mở rộng sau
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val isLoading: Boolean = true,
    // ✅ THÊM 1: Thêm trạng thái để điều hướng sau khi đăng xuất
    val isSignedOut: Boolean = false
)

class ProfileViewModel(private val repository: MedicineRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        // Đặt trạng thái isLoading = true ngay khi bắt đầu tải
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val firebaseUser = repository.getCurrentUser()

            if (firebaseUser != null) {
                _uiState.update {
                    it.copy(
                        // ✅ SỬA 2: Cung cấp giá trị mặc định an toàn hơn
                        userName = firebaseUser.displayName?.takeIf { name -> name.isNotBlank() } ?: "Chưa cập nhật tên",
                        email = firebaseUser.email ?: "Không có email",
                        photoUrl = firebaseUser.photoUrl?.toString(),
                        isEmailVerified = firebaseUser.isEmailVerified,
                        isLoading = false
                    )
                }
            } else {
                // Xử lý trường hợp không có người dùng, có thể coi như đã đăng xuất
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSignedOut = true // Cập nhật trạng thái để UI có thể điều hướng về màn hình Login
                    )
                }
            }
        }
    }

    /**
     * Xử lý sự kiện đăng xuất.
     * Hàm này sẽ gọi Repository để thực hiện đăng xuất và sau đó cập nhật UI State.
     */
    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            // Sau khi đăng xuất thành công, cập nhật trạng thái để UI biết và điều hướng
            _uiState.update { it.copy(isSignedOut = true, isLoading = false) }
        }
    }

    /**
     * ✅ THÊM 3: Hàm để reset lại trạng thái isSignedOut sau khi đã điều hướng.
     * Điều này ngăn việc điều hướng lặp lại nếu người dùng quay lại màn hình này.
     */
    fun onSignOutComplete() {
        _uiState.update { it.copy(isSignedOut = false) }
    }
}
