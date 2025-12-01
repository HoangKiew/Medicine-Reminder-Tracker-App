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
fun ForgotPasswordRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onSendCode: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.navigateNext) {
        if (uiState.navigateNext) {
            Toast.makeText(
                context,
                "Đã gửi mã xác minh đến email của bạn.",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.onNavigationHandled()
            onSendCode()
        }
    }

    ForgotPasswordScreen(
        modifier = modifier,
        state = uiState,
        onBack = onBack,
        onEmailChanged = viewModel::onEmailChanged,
        onSendCode = viewModel::sendCode
    )
}

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    state: ForgotPasswordUiState,
    onBack: () -> Unit,
    onEmailChanged: (String) -> Unit,
    onSendCode: () -> Unit
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
            text = "Quên mật khẩu?",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Nhập địa chỉ email, chúng tôi sẽ gửi mã xác minh đến địa chỉ email của bạn.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        AuthTextField(
            value = state.email,
            onValueChange = onEmailChanged,
            label = "Email của bạn",
            modifier = Modifier.fillMaxWidth()
        )

        state.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            label = "Gửi code",
            modifier = Modifier.fillMaxWidth(),
            enabled = state.email.isNotBlank(),
            isLoading = state.isLoading,
            onClick = onSendCode
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Bạn đã nhớ mật khẩu?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


