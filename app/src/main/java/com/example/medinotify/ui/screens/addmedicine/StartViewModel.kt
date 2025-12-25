package com.example.medinotify.ui.screens.addmedicine

import androidx.lifecycle.ViewModel
import com.example.medinotify.data.repository.MedicineRepository

/**
 * ViewModel cho StartScreen.
 * Hiện tại, màn hình này không cần logic phức tạp,
 * nhưng chúng ta vẫn tạo ViewModel để tuân thủ kiến trúc MVVM
 * và để dễ dàng mở rộng trong tương lai.
 */
class StartViewModel(private val repository: MedicineRepository) : ViewModel() {

}
