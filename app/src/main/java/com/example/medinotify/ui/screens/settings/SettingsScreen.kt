package com.example.medinotify.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope // ✅ ĐÃ THÊM IMPORT NÀY
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medinotify.ui.navigation.NavDestination
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.io.File

// ==========================================
// DATA MODELS
// ==========================================

data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val route: String? = null,
    val onClick: (() -> Unit)? = null
)

// ==========================================
// MAIN SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,

    viewModel: SettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- TOP BAR ---
            SettingsTopBar(
                onBackClick = { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- MENU LIST ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                // 1. SECTION: TÀI KHOẢN
                item { SettingsSectionTitle("Tài khoản") }
                item {
                    SettingsCard {
                        SettingsMenuItem(
                            icon = Icons.Default.Person,
                            title = "Hồ sơ cá nhân",
                            onClick = { navController.navigate(NavDestination.Profile.route) }
                        )
                        SettingsDivider()
                        SettingsMenuItem(
                            icon = Icons.Default.Notifications,
                            title = "Thông báo",
                            onClick = { navController.navigate(NavDestination.Notifications.route) }
                        )
                    }
                    SectionSpacer()
                }

                // 2. SECTION: HỖ TRỢ & GIỚI THIỆU
                item { SettingsSectionTitle("Hỗ trợ & Giới thiệu") }
                item {
                    SettingsCard {
                        SettingsMenuItem(
                            icon = Icons.Default.Help,
                            title = "Trợ giúp & Hỗ trợ",
                            onClick = { navController.navigate(NavDestination.HelpAndSupport.route) }
                        )
                        SettingsDivider()
                        SettingsMenuItem(
                            icon = Icons.Default.Info,
                            title = "Điều khoản và Chính sách",
                            onClick = { /* TODO: Implement Policy link */ }
                        )
                    }
                    SectionSpacer()
                }

                // 3. SECTION: BỘ NHỚ ĐỆM & DI ĐỘNG
                item { SettingsSectionTitle("Bộ nhớ đệm và di động") }
                item {
                    SettingsCard {
                        SettingsMenuItem(
                            icon = Icons.Default.Delete,
                            title = "Dọn bộ nhớ đệm",
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        deleteCache(context)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Đã dọn sạch bộ nhớ đệm!", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        )
                    }
                    SectionSpacer()
                }

                // 4. SECTION: ACTIONS (LOGOUT)
                item { SettingsSectionTitle("Actions") }
                item {
                    SettingsCard {
                        SettingsMenuItem(
                            icon = Icons.Default.Flag,
                            title = "Báo cáo sự cố",
                            onClick = {
                                openUrl(context, "https://forms.gle/VNsjfURN1jxA3WaU7")
                            }
                        )
                        SettingsDivider()


                        SettingsMenuItem(
                            icon = Icons.Default.Logout,
                            title = "Đăng xuất",
                            textColor = Color(0xFFFF5252),
                            onClick = {
                                performLogout(context, viewModel, navController)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

// ==========================================
// COMPOSABLE COMPONENTS
// ==========================================

@Composable
fun SettingsTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Cài đặt",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6395EE)
        )
    }
}

// ✅ SỬA LỖI COLUMNSCOPE TẠI ĐÂY
@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        content = content
    )
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier
            .padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (textColor == Color.Black) Color.Gray else textColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsDivider() {
    Divider(
        color = Color(0xFFE0E0E0),
        thickness = 0.5.dp
    )
}

@Composable
fun SectionSpacer() {
    Spacer(modifier = Modifier.height(24.dp))
}

// ==========================================
// HELPER FUNCTIONS (LOGIC)
// ==========================================

private fun performLogout(
    context: Context,
    viewModel: SettingsViewModel,
    navController: NavController
) {
    // 1. Cấu hình Google Sign In
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    // 2. Thực hiện đăng xuất Google
    googleSignInClient.signOut().addOnCompleteListener {
        // 3. Xóa dữ liệu trong máy & Đăng xuất Firebase
        viewModel.signOut {
            // 4. Chuyển hướng về màn hình đăng nhập
            navController.navigate(NavDestination.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun deleteCache(context: Context) {
    try {
        val dir = context.cacheDir
        deleteDir(dir)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun deleteDir(dir: File?): Boolean {
    if (dir != null && dir.isDirectory) {
        val children = dir.list()
        if (children != null) {
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) return false
            }
        }
        return dir.delete()
    } else if (dir != null && dir.isFile) {
        return dir.delete()
    }
    return false
}

// ==========================================
// PREVIEW
// ==========================================

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen(navController = rememberNavController())
    }
}