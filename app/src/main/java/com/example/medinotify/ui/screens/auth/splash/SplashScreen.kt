package com.example.medinotify.ui.screens.auth.splash

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medinotify.R
import com.example.medinotify.ui.screens.auth.components.PrimaryButton
import com.example.medinotify.ui.theme.MedinotifyTheme

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(
                    top = 24.dp,
                    bottom = 32.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            Image(
                painter = painterResource(id = R.drawable.splash_screen),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Welcome To",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFFF57C00),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    fontSize = 24.sp
                )
            )
            Text(
                text = "MediNotify",
                style = MaterialTheme.typography.displaySmall.copy(
                    color = Color(0xFF3662FF),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    fontSize = 40.sp
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Ứng dụng nhắc uống thuốc giúp cải thiện sức khỏe thêm an hảo",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFF6F6F6F),
                    lineHeight = 22.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp, start = 8.dp, end = 8.dp)
            )
            Spacer(modifier = Modifier.height(48.dp))
            PrimaryButton(
                label = "Đăng Nhập",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                onClick = onLogin
            )
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                onClick = onRegister,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.5.dp, Color(0xFF5A8BFF)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF5A8BFF),
                    containerColor = Color.Transparent
                )
            ) {
                Text(
                    text = "Đăng kí",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 96.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = null,
                tint = Color(0xFF9E9E9E)
            )
            Icon(
                imageVector = Icons.Filled.Circle,
                contentDescription = null,
                tint = Color(0xFF292929)
            )
            Icon(
                imageVector = Icons.Filled.Stop,
                contentDescription = null,
                tint = Color(0xFF9E9E9E)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashPreview() {
    MedinotifyTheme {
        SplashScreen(onLogin = {}, onRegister = {})
    }
}


