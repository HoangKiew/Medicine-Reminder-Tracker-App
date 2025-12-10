package com.example.medinotify.ui.screens.settings.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medinotify.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(navController: NavController) {
    val backgroundColor = Color(0xFFF5F5F5)
    val cardColor = Color.White
    val primaryColor = Color(0xFF6395EE)
    val textColor = Color(0xFF2D2D2D)

    // Trạng thái cho Switch (được chia sẻ với Dialog)
    var isTwoFactorAuthEnabled by remember { mutableStateOf(false) }

    // Trạng thái cho Dialog
    var showConfirmationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SecurityTopBar(
                title = "Bảo vệ",
                textColor = textColor,
                primaryColor = primaryColor,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                // Truyền trạng thái và hàm xử lý sự kiện
                TwoFactorAuthItem(
                    primaryColor = primaryColor,
                    isEnabled = isTwoFactorAuthEnabled,
                    onCheckedChange = { isNewStateChecked ->
                        if (isNewStateChecked) {
                            // Nếu người dùng cố gắng BẬT, hiện Dialog
                            showConfirmationDialog = true
                        } else {
                            // Nếu người dùng TẮT, cập nhật trạng thái ngay lập tức
                            isTwoFactorAuthEnabled = false
                            // TODO: GỌI HÀM TẮT XÁC THỰC 2 LỚP
                        }
                    }
                )
            }
        }
    }

    // ====================================================================
    // SỬ DỤNG ConfirmationDialog (Giả định nó nằm trong CommonComposables.kt)
    // ====================================================================
    if (showConfirmationDialog) {

        ConfirmationDialog(
            primaryColor = primaryColor,
            onDismiss = { showConfirmationDialog = false },
            onConfirm = {
                isTwoFactorAuthEnabled = true
                showConfirmationDialog = false
                // TODO: GỌI HÀM BẬT XÁC THỰC 2 LỚP TRONG VM
            },
            titleText = "Xác nhận",
            bodyText = "Bạn có muốn bật xác thực hai lớp không?"
        )

    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityTopBar(
    title: String,
    textColor: Color,
    primaryColor: Color,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "Bảo vệ",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C60FF)
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color(0xFF2D2D2D),
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
fun TwoFactorAuthItem(
    primaryColor: Color,
    isEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val textColor = Color(0xFF2D2D2D)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isEnabled) }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Bật xác thực hai yếu tố",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = isEnabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = primaryColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}
@Preview(showBackground = true)
@Composable
fun SecurityScreenPreview() {
    SecurityScreen(navController = rememberNavController())
}