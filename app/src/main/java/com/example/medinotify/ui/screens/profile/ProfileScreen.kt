package com.example.medinotify.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle // ✅ Thêm import mới
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.medinotify.ui.navigation.NavDestination // ✅ Thêm import mới
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    // Sử dụng collectAsStateWithLifecycle để theo dõi State an toàn
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ✅ LOGIC QUAN TRỌNG: Điều hướng khi đăng xuất thành công
    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) {
            // Điều hướng tới màn hình Login
            navController.navigate(NavDestination.Login.route) {
                // Xóa toàn bộ Back Stack cho đến và bao gồm màn hình Home
                // (Ngăn người dùng nhấn Back quay lại Home/Profile sau khi đăng xuất)
                popUpTo(NavDestination.Home.route) { inclusive = true }
            }
            // Reset trạng thái để ngăn điều hướng lặp lại nếu quay lại màn hình
            viewModel.onSignOutComplete()
        }
    }

    // Nếu dữ liệu đang tải, hiển thị CircularProgressIndicator (Tùy chọn)
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        Scaffold(
            containerColor = Color(0xFFF5F5F5)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Top Bar với nút back và title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hồ sơ cá nhân", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C60FF))
                    }

                    // Nút đăng xuất - Gọi signOut()
                    IconButton(onClick = { viewModel.signOut() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.Red)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Profile Avatar và Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .border(3.dp, Color(0xFF2C60FF), CircleShape)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE3F2FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Hiển thị ảnh người dùng thật bằng Coil
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(uiState.photoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                placeholder = rememberVectorPainter(Icons.Default.Person),
                                error = rememberVectorPainter(Icons.Default.Person)
                            )
                        }

                        // Edit button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF2C60FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(uiState.userName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Email với verified icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(uiState.email, fontSize = 14.sp, color = Color.Gray)
                        if (uiState.isEmailVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Profile Info Fields
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileInfoField(icon = Icons.Default.Person, value = uiState.userName)
                    ProfileInfoField(icon = Icons.Default.DateRange, value = uiState.dateOfBirth)
                    ProfileInfoField(icon = Icons.Default.Email, value = uiState.email)
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoField(icon: ImageVector, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF2C60FF),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}