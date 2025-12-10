package com.example.medinotify.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    var username by remember { mutableStateOf("User123") }
    var dateOfBirth by remember { mutableStateOf("01/01/1988") }
    var email by remember { mutableStateOf("User123@mail.com") }
    var showImagePicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    "Chỉnh sửa hồ sơ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6395EE)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Avatar Section - Editable
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .border(3.dp, Color(0xFF6395EE), CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD))
                            .clickable { showImagePicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(80.dp),
                            tint = Color(0xFF6395EE)
                        )
                    }

                    // Edit button with clickable
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF6395EE), CircleShape)
                            .clickable { showImagePicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Nhấn để thay đổi ảnh đại diện",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Edit Form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Username Field
                EditableField(
                    label = "Tên người dùng",
                    value = username,
                    onValueChange = { username = it },
                    icon = Icons.Default.Person
                )

                // Date of Birth Field
                EditableField(
                    label = "Ngày sinh",
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    icon = Icons.Default.DateRange,
                    placeholder = "dd/mm/yyyy"
                )

                // Email Field
                EditableField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    icon = Icons.Default.Email,
                    enabled = false  // Email thường không cho edit
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = {
                    // TODO: Save profile changes
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6395EE)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Lưu thay đổi",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cancel Button
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF6395EE)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.5.dp
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Hủy",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Image Picker Dialog (Placeholder)
    if (showImagePicker) {
        AlertDialog(
            onDismissRequest = { showImagePicker = false },
            icon = {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color(0xFF6395EE),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Chọn ảnh đại diện",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            // TODO: Open camera
                            showImagePicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chụp ảnh mới")
                    }

                    TextButton(
                        onClick = {
                            // TODO: Open gallery
                            showImagePicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chọn từ thư viện")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImagePicker = false }) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Composable
fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String = "",
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6395EE),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (enabled) Color(0xFF6395EE) else Color.Gray
                )
            },
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(placeholder, color = Color.Gray)
                }
            },
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6395EE),
                unfocusedBorderColor = Color.LightGray,
                disabledBorderColor = Color.LightGray,
                disabledTextColor = Color.Gray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color(0xFFF5F5F5)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}


@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    MaterialTheme {
        EditProfileScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun EditableFieldPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EditableField(
                label = "Tên người dùng",
                value = "User123",
                onValueChange = {},
                icon = Icons.Default.Person
            )
            EditableField(
                label = "Email",
                value = "user@example.com",
                onValueChange = {},
                icon = Icons.Default.Email,
                enabled = false
            )
        }
    }
}