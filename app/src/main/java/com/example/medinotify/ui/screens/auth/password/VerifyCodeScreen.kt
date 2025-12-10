package com.example.medinotify.ui.screens.auth.password

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medinotify.ui.screens.auth.components.PrimaryButton

@Composable
fun VerifyCodeRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    viewModel: VerifyCodeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.navigateNext) {
        if (uiState.navigateNext) {
            Toast.makeText(context, "Mã xác minh hợp lệ.", Toast.LENGTH_SHORT).show()
            viewModel.onNavigationHandled()
            onConfirm()
        }
    }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onInfoMessageShown()
        }
    }

    VerifyCodeScreen(
        modifier = modifier,
        state = uiState,
        onBack = onBack,
        onCodeChanged = viewModel::onCodeChanged,
        onConfirm = viewModel::confirmCode,
        onResendCode = viewModel::resendCode
    )
}

@Composable
fun VerifyCodeScreen(
    modifier: Modifier = Modifier,
    state: VerifyCodeUiState,
    onBack: () -> Unit,
    onCodeChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onResendCode: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

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
            text = "Xác minh",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Nhập mã xác minh mà chúng tôi đã gửi đến địa chỉ email của bạn.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        BasicTextField(
            value = state.code,
            onValueChange = onCodeChanged,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            cursorBrush = SolidColor(Color.Transparent),
            textStyle = MaterialTheme.typography.titleLarge.copy(color = Color.Transparent),
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(OTP_CODE_LENGTH) { index ->
                        val char = state.code.getOrNull(index)?.toString().orEmpty()
                        val isFilled = char.isNotEmpty()
                        val borderColor = if (isFilled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                        val borderWidth = if (isFilled) 1.5.dp else 1.dp

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .border(
                                    BorderStroke(borderWidth, borderColor),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(
                                    color = if (isFilled) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { focusRequester.requestFocus() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        )

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
            label = "Xác nhận",
            modifier = Modifier.fillMaxWidth(),
            enabled = state.code.length == OTP_CODE_LENGTH,
            isLoading = state.isLoading,
            onClick = onConfirm
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Không nhận được mã?",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = "Gửi lại",
                modifier = Modifier
                    .padding(start = 4.dp)
                    .clickable(
                        enabled = !state.isLoading,
                        onClick = onResendCode
                    ),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}


