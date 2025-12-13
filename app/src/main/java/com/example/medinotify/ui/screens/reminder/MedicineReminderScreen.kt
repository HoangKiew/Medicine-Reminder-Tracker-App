package com.example.medinotify.ui.screens.reminder

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.medinotify.R
import com.example.medinotify.ui.navigation.NavDestination
import com.example.medinotify.ui.theme.MedinotifyTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun MedicineReminderScreen(
    navController: NavController,
    viewModel: MedicineReminderViewModel = koinViewModel(),
    medicineId: String,
    medicineName: String,
    dosage: String,
    time: String,
    dayOfWeek: String = "Hôm nay",
    onBack: () -> Unit = { navController.popBackStack() }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (Phần UI phía trên GIỮ NGUYÊN không đổi) ...

        // Nút Back
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.Black, modifier = Modifier.size(32.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FBFF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ... (Phần hiển thị Icon, Tên thuốc... GIỮ NGUYÊN) ...

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(40.dp).background(Color(0xFFFFE9A1), CircleShape).padding(10.dp))
                    Icon(Icons.Default.Delete, contentDescription = "Xóa lịch", tint = Color.White, modifier = Modifier.size(40.dp).background(Color(0xFFFF6B6B), CircleShape).padding(10.dp).clickable {})
                }
                Spacer(modifier = Modifier.height(40.dp))
                Text(text = "Bạn đã uống thuốc chưa?", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2C3E50))
                Image(painter = painterResource(id = R.drawable.medicine_start), contentDescription = null, modifier = Modifier.size(110.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = medicineName, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C60FF), lineHeight = 40.sp)
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF5E80FF), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Đã lên lịch vào $time, $dayOfWeek", fontSize = 16.sp, color = Color(0xFF555555))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Medication, contentDescription = null, tint = Color(0xFF5E80FF), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = dosage, fontSize = 16.sp, color = Color(0xFF555555))
                }

                Spacer(modifier = Modifier.height(48.dp))

                // --- KHU VỰC NÚT BẤM (CẦN SỬA) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ✨ NÚT ĐÃ UỐNG ĐƯỢC CẬP NHẬT ✨
                    Button(
                        onClick = {
                            // Gọi hàm ViewModel và truyền vào hành động sẽ làm SAU KHI lưu xong
                            viewModel.markAsTaken(medicineId, time) {
                                // Code trong này chỉ chạy khi Database đã cập nhật xong
                                navController.navigate(NavDestination.Home.route) {
                                    popUpTo(NavDestination.Home.route) { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Đã uống", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }

                    // Nút Lát nữa (Giữ nguyên)
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(30.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                    ) {
                        Text("Lát nữa", fontSize = 18.sp, color = Color(0xFF2C60FF), fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}