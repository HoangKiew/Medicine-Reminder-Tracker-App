package com.example.medinotify.ui.screens.auth.login

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medinotify.R
import com.example.medinotify.ui.screens.auth.components.AuthTextField
import com.example.medinotify.ui.screens.auth.components.GoogleButton
import com.example.medinotify.ui.screens.auth.components.PrimaryButton
import com.example.medinotify.ui.theme.MedinotifyTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginRoute(
    modifier: Modifier = Modifier,
    onRegister: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val defaultWebClientId = stringResource(id = R.string.default_web_client_id)

    val googleSignInOptions = remember(defaultWebClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(defaultWebClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember(googleSignInOptions, context) {
        GoogleSignIn.getClient(context, googleSignInOptions)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("LoginRoute", "Google sign-in resultCode=${result.resultCode}")
        if (result.resultCode != Activity.RESULT_OK) {
            viewModel.onExternalLoginError("Đăng nhập Google đã bị huỷ.")
            return@rememberLauncherForActivityResult
        }
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            Log.d("LoginRoute", "Google sign-in idTokenNull=${account?.idToken == null}")
            viewModel.loginWithGoogle(account?.idToken)
        } catch (exception: ApiException) {
            Log.e("LoginRoute", "Google sign-in failed code=${exception.statusCode}", exception)
            viewModel.onExternalLoginError(
                exception.localizedMessage ?: "Đăng nhập Google thất bại. Vui lòng thử lại."
            )
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
            viewModel.onSuccessConsumed()
            onContinue()
        }
    }

    LoginScreen(
        modifier = modifier,
        state = uiState,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onRegister = onRegister,
        onBack = onBack,
        onForgotPassword = onForgotPassword,
        onLogin = viewModel::login,
        onGoogleSignInClick = {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    )
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    state: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit = {},
    onLogin: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

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
                text = "Chào mừng đến với",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFF5A77FF),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                )
            )
            Text(
                text = "MediNotify!!!",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color(0xFF5A77FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AuthTextField(
                value = state.email,
                onValueChange = onEmailChanged,
                label = "Nhập email",
                modifier = Modifier.fillMaxWidth()
            )
            AuthTextField(
                value = state.password,
                onValueChange = onPasswordChanged,
                label = "Nhập mật khẩu",
                modifier = Modifier.fillMaxWidth(),
                isPassword = true,
                isPasswordVisible = state.isPasswordVisible,
                onPasswordToggle = onTogglePasswordVisibility
            )
            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "Quên mật khẩu?",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onForgotPassword() },
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryButton(
                label = "Đăng Nhập",
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                isLoading = state.isLoading,
                onClick = onLogin
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
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
                label = "Tiếp tục với Google",
                modifier = Modifier.fillMaxWidth(),
                onClick = onGoogleSignInClick
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
                text = "Chưa có tài khoản? ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Đăng ký",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .clickable { onRegister() }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    MedinotifyTheme {
        LoginScreen(
            state = LoginUiState(),
            onEmailChanged = {},
            onPasswordChanged = {},
            onTogglePasswordVisibility = {},
            onRegister = {},
            onBack = {},
            onForgotPassword = {},
            onLogin = {},
            onGoogleSignInClick = {}
        )
    }
}


