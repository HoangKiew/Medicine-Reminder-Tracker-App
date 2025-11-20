package com.example.medinotify.ui.screens.calendar

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier   // ⭐ FIX #1: nhận padding từ Scaffold
) {
    Box(
        modifier = modifier          // ⭐ FIX #2: sử dụng padding từ Scaffold
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Màn hình Lịch")
    }
}
