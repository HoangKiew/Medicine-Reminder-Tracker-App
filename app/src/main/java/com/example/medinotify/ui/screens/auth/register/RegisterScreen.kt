package com.example.medinotify.ui.screens.auth.register

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ❌ ĐÃ XÓA: import androidx.lifecycle.viewmodel.compose.viewModel
// ✅ THÊM MỚI: Import Koin để tiêm ViewModel đúng cách
import org.koin.androidx.compose.koinViewModel

import com.example.medinotify.ui.screens.auth.components.AuthTextField
import com.example.medinotify.ui.screens.auth.components.GoogleButton
import com.example.medinotify.ui.screens.auth.components.PrimaryButton
import com.example.medinotify.ui.theme.MedinotifyTheme

@Composable
fun RegisterRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onLogin: () -> Unit,
    // ✨✨✨ QUAN TRỌNG: Đổi viewModel() thành koinViewModel() ✨✨✨
    viewModel: RegisterViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.navigateToLogin) {
        if (uiState.navigateToLogin) {
            Toast.makeText(context, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show()
            viewModel.onNavigationHandled()
            onRegisterSuccess()
        }
    }

    // Hiển thị thông báo lỗi (nếu có) khi đăng ký thất bại
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    RegisterScreen(
        modifier = modifier,
        state = uiState,
        onBack = onBack,
        onNameChanged = viewModel::onNameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onPhoneChanged = viewModel::onPhoneChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
        onPasswordVisibilityToggle = viewModel::onPasswordVisibilityToggle,
        onConfirmPasswordVisibilityToggle = viewModel::onConfirmPasswordVisibilityToggle,
        onRegister = viewModel::register,
        onLogin = onLogin,
        onGoogleRegister = {
            Toast.makeText(context, "Đăng ký với Google sẽ sớm có mặt.", Toast.LENGTH_SHORT).show()
        }
    )
}

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    state: RegisterUiState,
    onBack: () -> Unit,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onConfirmPasswordVisibilityToggle: () -> Unit,
    onRegister: () -> Unit,
    onLogin: () -> Unit,
    onGoogleRegister: () -> Unit = onRegister
) {
    val isFormFilled = state.name.isNotBlank() &&
            state.email.isNotBlank() &&
            state.phone.isNotBlank() &&
            state.password.isNotBlank() &&
            state.confirmPassword.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Quay lại",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Xin chào, hãy tạo tài khoản",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFF5A77FF),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                )
            )
            Text(
                text = "cùng MediNotify!",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color(0xFF5A77FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AuthTextField(
                value = state.name,
                onValueChange = onNameChanged,
                label = "Nhập đầy đủ tên",
                modifier = Modifier.fillMaxWidth()
            )
            AuthTextField(
                value = state.email,
                onValueChange = onEmailChanged,
                label = "Email",
                modifier = Modifier.fillMaxWidth()
            )
            AuthTextField(
                value = state.phone,
                onValueChange = onPhoneChanged,
                label = "Số điện thoại",
                modifier = Modifier.fillMaxWidth()
            )
            AuthTextField(
                value = state.password,
                onValueChange = onPasswordChanged,
                label = "Mật khẩu",
                modifier = Modifier.fillMaxWidth(),
                isPassword = true,
                isPasswordVisible = state.passwordVisible,
                onPasswordToggle = onPasswordVisibilityToggle
            )
            AuthTextField(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChanged,
                label = "Nhập lại mật khẩu",
                modifier = Modifier.fillMaxWidth(),
                isPassword = true,
                isPasswordVisible = state.confirmPasswordVisible,
                onPasswordToggle = onConfirmPasswordVisibilityToggle
            )
            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryButton(
                label = "Đăng ký",
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormFilled && !state.isLoading,
                isLoading = state.isLoading,
                onClick = onRegister
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
                Text(
                    text = "Hoặc",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            }
            GoogleButton(
                label = "Đăng ký với Google",
                modifier = Modifier.fillMaxWidth(),
                onClick = onGoogleRegister
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Đã có tài khoản? ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Đăng nhập",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .clickable { onLogin() }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterPreview() {
    MedinotifyTheme {
        RegisterScreen(
            state = RegisterUiState(),
            onBack = {},
            onNameChanged = {},
            onEmailChanged = {},
            onPhoneChanged = {},
            onPasswordChanged = {},
            onConfirmPasswordChanged = {},
            onPasswordVisibilityToggle = {},
            onConfirmPasswordVisibilityToggle = {},
            onRegister = {},
            onLogin = {}
        )
    }
}