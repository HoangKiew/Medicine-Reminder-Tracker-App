package com.example.medinotify.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlin.text.orEmpty

sealed interface AuthResult {
    data class Success(val userId: String) : AuthResult
    data class Error(val message: String) : AuthResult
}

interface AuthRepository {
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signInWithGoogle(idToken: String): AuthResult
}

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid.orEmpty()
            AuthResult.Success(userId)
        } catch (exception: FirebaseAuthInvalidUserException) {
            AuthResult.Error("Tài khoản không tồn tại. Vui lòng kiểm tra lại email.")
        } catch (exception: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Email hoặc mật khẩu chưa chính xác.")
        } catch (exception: FirebaseNetworkException) {
            AuthResult.Error("Không thể kết nối tới máy chủ. Vui lòng thử lại sau.")
        } catch (exception: Exception) {
            AuthResult.Error(exception.message ?: "Đăng nhập thất bại. Vui lòng thử lại.")
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val userId = result.user?.uid.orEmpty()
            AuthResult.Success(userId)
        } catch (exception: FirebaseNetworkException) {
            AuthResult.Error("Không thể kết nối tới máy chủ. Vui lòng thử lại sau.")
        } catch (exception: Exception) {
            AuthResult.Error(exception.message ?: "Đăng nhập Google thất bại. Vui lòng thử lại.")
        }
    }
}

