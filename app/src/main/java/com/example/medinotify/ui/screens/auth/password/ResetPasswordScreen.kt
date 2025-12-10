package com.example.medinotify.ui.screens.auth.password

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medinotify.ui.screens.auth.components.AuthTextField
import com.example.medinotify.ui.screens.auth.components.PrimaryButton

@Composable
fun ResetPasswordRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onReset: () -> Unit,
    viewModel: ResetPasswordViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.navigateNext) {
        if (uiState.navigateNext) {
            Toast.makeText(context, "Đổi mật khẩu thành công.", Toast.LENGTH_SHORT).show()
            viewModel.onNavigationHandled()
            onReset()
        }
    }

    ResetPasswordScreen(
        modifier = modifier,
        state = uiState,
        onBack = onBack,
        onPasswordChanged = viewModel::onPasswordChanged,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
        onPasswordToggle = viewModel::onPasswordVisibilityToggle,
        onConfirmPasswordToggle = viewModel::onConfirmPasswordVisibilityToggle,
        onReset = viewModel::resetPassword
    )
}

@Composable
fun ResetPasswordScreen(
    modifier: Modifier = Modifier,
    state: ResetPasswordUiState,
    onBack: () -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onPasswordToggle: () -> Unit,
    onConfirmPasswordToggle: () -> Unit,
    onReset: () -> Unit
) {
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

        Text(
            text = "Tạo mật khẩu mới",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Mật khẩu mới của bạn phải khó đoán và bạn có thể tạo mật khẩu của riêng mình.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AuthTextField(
                value = state.password,
                onValueChange = onPasswordChanged,
                label = "Nhập mật khẩu mới",
                modifier = Modifier.fillMaxWidth(),
                isPassword = true,
                isPasswordVisible = state.passwordVisible,
                onPasswordToggle = onPasswordToggle
            )
            AuthTextField(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChanged,
                label = "Nhập lại mật khẩu mới",
                modifier = Modifier.fillMaxWidth(),
                isPassword = true,
                isPasswordVisible = state.confirmPasswordVisible,
                onPasswordToggle = onConfirmPasswordToggle
            )
        }

        state.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            label = "Đổi mật khẩu",
            modifier = Modifier.fillMaxWidth(),
            enabled = state.password.isNotBlank() && state.confirmPassword.isNotBlank(),
            isLoading = state.isLoading,
            onClick = onReset
        )
    }
}


