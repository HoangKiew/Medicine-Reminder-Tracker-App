package com.example.medinotify.ui.screens.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null) // null: đang kiểm tra, true: đã đăng nhập, false: chưa đăng nhập
    val isLoggedIn = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            // Thêm độ trễ ngắn để trải nghiệm mượt mà hơn
            delay(600)
            _isLoggedIn.value = (FirebaseAuth.getInstance().currentUser != null)
        }
    }
}
