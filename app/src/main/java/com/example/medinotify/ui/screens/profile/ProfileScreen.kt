package com.example.medinotify.ui.screens.profile

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier     // ⭐ THÊM MODIFIER
) {
    Box(
        modifier = modifier           // ⭐ DÙNG PADDING TỪ SCAFFOLD
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Màn hình Cá nhân")
    }
}
