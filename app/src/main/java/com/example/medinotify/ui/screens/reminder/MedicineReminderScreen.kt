// ui/screens/reminder/MedicineReminderScreen.kt
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
import com.example.medinotify.R
import com.example.medinotify.ui.theme.MedinotifyTheme

@Composable
fun MedicineReminderScreen(
    medicineName: String = "Vitamin D",
    dosage: String = "1 Viên nén, 1000mg",
    time: String = "09:41 PM",
    dayOfWeek: String = "Thứ tư",
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Nút Back
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Card chính – giống hệt ảnh
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
                // 2 icon trên cùng
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFFFB800),
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFE9A1), CircleShape)
                            .padding(10.dp)
                    )

                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa lịch",
                        tint = Color.White,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFF6B6B), CircleShape)
                            .padding(10.dp)
                            .clickable { /* TODO: Xóa */ }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
                // Câu hỏi – nằm ngay trên tên thuốc
                Text(
                    text = "Bạn đã uống thuốc chưa?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C3E50)
                )
                // Viên thuốc lớn
                Image(
                    painter = painterResource(id = R.drawable.medicine_start),
                    contentDescription = null,
                    modifier = Modifier.size(110.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))



                Spacer(modifier = Modifier.height(16.dp))

                // Tên thuốc to
                Text(
                    text = medicineName,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C60FF)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Dòng "Đã lên lịch" – sát trái
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF5E80FF), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Đã lên lịch vào $time, $dayOfWeek",
                        fontSize = 16.sp,
                        color = Color(0xFF555555)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dòng liều lượng – sát trái
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Medication, contentDescription = null, tint = Color(0xFF5E80FF), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = dosage,
                        fontSize = 16.sp,
                        color = Color(0xFF555555)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // 2 NÚT SONG SONG – ĐÃ UỐNG + LÁT NỮA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { /* Đã uống */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Đã uống", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { /* Lát nữa */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewMedicineReminderScreen() {
    MedinotifyTheme {
        MedicineReminderScreen(
            medicineName = "Vitamin D",
            dosage = "1 Viên nén, 1000mg",
            time = "09:41 PM",
            dayOfWeek = "Thứ tư"
        )
    }
}