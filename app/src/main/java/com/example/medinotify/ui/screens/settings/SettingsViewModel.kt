package com.example.medinotify.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.repository.MedicineRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: MedicineRepository
) : ViewModel() {

    // Hàm xử lý đăng xuất
    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            // 1. Gọi Repository để xóa sạch dữ liệu Local (Room) và đăng xuất Firebase
            repository.signOut()

            // 2. Gọi callback để báo cho UI biết đã xong (để chuyển màn hình)
            onComplete()
        }
    }
}