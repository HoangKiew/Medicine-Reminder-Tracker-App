package com.example.medinotify.ui.screens.addmedicine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.example.medinotify.R

@Composable
fun StartScreen(
    onStart: () -> Unit,
    modifier: Modifier = Modifier    // ⭐ FIX #1: nhận padding từ Scaffold
) {

    Box(
        modifier = modifier           // ⭐ FIX #2: dùng padding ở đây
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

            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = null,
                tint = Color(0xFFFF5A5A),
                modifier = Modifier.size(28.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color(0xFF355CFF),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(18.dp))
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    tint = Color.DarkGray,
                    modifier = Modifier.size(28.dp)
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
