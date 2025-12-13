package com.example.medinotify.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medinotify.data.repository.MedicineRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: MedicineRepository
) : ViewModel() {

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            // Gọi hàm này để xóa sạch DB local và đăng xuất Firebase
            repository.signOut()
            onComplete()
        }
    }
}