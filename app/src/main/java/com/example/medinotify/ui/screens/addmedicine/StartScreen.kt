package com.example.medinotify.ui.screens.addmedicine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // ✅ Import clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController // ✅ Import NavController
import com.example.medinotify.R
import com.example.medinotify.ui.navigation.NavDestination // ✅ Import NavDestination
import org.koin.androidx.compose.koinViewModel

@Composable
fun StartScreen(
    navController: NavController, // ✅ THÊM: Nhận NavController để điều hướng
    onStart: () -> Unit,
    viewModel: StartViewModel = koinViewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // =================== TOP ICONS ===================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 📅 ICON LỊCH
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = "Calendar",
                tint = Color(0xFFFF5A5A),
                modifier = Modifier
                    .size(28.dp)
                    .clickable { // ✅ Thêm sự kiện click
                        navController.navigate(NavDestination.Calendar.route)
                    }
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // 👤 ICON PROFILE
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile",
                    tint = Color(0xFF355CFF),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { // ✅ Thêm sự kiện click
                            navController.navigate(NavDestination.Profile.route)
                        }
                )
                Spacer(modifier = Modifier.width(18.dp))

                // ⚙️ ICON SETTINGS
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = Color.DarkGray,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { // ✅ Thêm sự kiện click
                            navController.navigate(NavDestination.Settings.route)
                        }
                )
            }
        }

        // =================== CONTENT ===================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 140.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.medicine_start),
                contentDescription = null,
                modifier = Modifier
                    .size(230.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Quản lý thuốc của bạn",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C60FF)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Thêm thuốc của bạn để được nhắc nhở đúng giờ\nvà theo dõi sức khỏe của bạn",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(120.dp))

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA500),
                    contentColor = Color.White
                )
            ) {
                Text(text = "Thêm thuốc", fontSize = 16.sp)
            }
        }
    }
}