package com.example.medinotify.ui.screens.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.repository.MedicineRepository // ✅ THÊM IMPORT NÀY
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    // ✅ THÊM DEPENDENCY: Cần thiết để đồng bộ dữ liệu
    private val repository: MedicineRepository
) : ViewModel() {

    // null: đang kiểm tra, true: đã đăng nhập, false: chưa đăng nhập
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            // Thêm độ trễ ngắn để trải nghiệm mượt mà hơn
            delay(600)

            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser != null) {
                // ✅ BƯỚC ĐỒNG BỘ: Nếu người dùng đã đăng nhập, tải dữ liệu từ Firebase về Room.
                // Điều này đảm bảo dữ liệu thuốc luôn là bản mới nhất sau khi khởi động app.
                repository.syncDataFromFirebase()

            }

            _isLoggedIn.value = (currentUser != null)
        }
    }
}