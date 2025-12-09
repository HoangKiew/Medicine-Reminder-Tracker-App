package com.example.medinotify.ui.screens.settings.account

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ConfirmationDialog(
    primaryColor: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    titleText: String,
    bodyText: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = titleText,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF544C4C)
            )
        },
        text = {
            Text(
                text = bodyText,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF544C4C)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    "Đồng ý",
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Hủy",
                    color = Color.Gray
                )
            }
        }
    )
}

// Thêm các composable chung khác của bạn ở đây...