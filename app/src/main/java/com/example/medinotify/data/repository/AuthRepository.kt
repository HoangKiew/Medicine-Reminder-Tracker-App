package com.example.medinotify.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser // Import này hữu ích
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

sealed interface AuthResult {
    data object Success : AuthResult
    data class Error(val message: String) : AuthResult
}

interface AuthRepository {
    suspend fun signUp(email: String, password: String): AuthResult
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signInWithGoogle(idToken: String): AuthResult
    // ✅ THÊM 1: Đưa hàm signOut vào đây để giữ đúng trách nhiệm
    fun signOut()
    fun getCurrentUser(): FirebaseUser? // Hàm trợ giúp
}

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // ... (Hàm signUp, signIn, signInWithGoogle giữ nguyên, chúng đã rất tốt) ...

    override suspend fun signUp(email: String, password: String): AuthResult {
        // ... (Logic signUp giữ nguyên) ...
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                // ... (Logic ghi Firestore giữ nguyên) ...
                val userDocument = firestore.collection("users").document(user.uid)
                val userData = mapOf(
                    "uid" to user.uid,
                    "email" to user.email,
                    "createdAt" to System.currentTimeMillis()
                )
                userDocument.set(userData).await()
            }
            AuthResult.Success
        } catch (exception: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Địa chỉ email không hợp lệ.")
        } catch (exception: Exception) {
            AuthResult.Error(exception.message ?: "Đăng ký thất bại. Vui lòng thử lại.")
        }
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        // ... (Logic signIn giữ nguyên) ...
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success
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
        // ... (Logic signInWithGoogle giữ nguyên) ...
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val isNewUser = result.additionalUserInfo?.isNewUser ?: false
            result.user?.let { user ->
                if (isNewUser) {
                    val userDocument = firestore.collection("users").document(user.uid)
                    val userData = mapOf(
                        "uid" to user.uid,
                        "email" to user.email,
                        "displayName" to user.displayName,
                        "photoUrl" to user.photoUrl?.toString(),
                        "createdAt" to System.currentTimeMillis()
                    )
                    userDocument.set(userData).await()
                }
            }
            AuthResult.Success
        } catch (exception: FirebaseNetworkException) {
            AuthResult.Error("Không thể kết nối tới máy chủ. Vui lòng thử lại sau.")
        } catch (exception: Exception) {
            AuthResult.Error(exception.message ?: "Đăng nhập Google thất bại. Vui lòng thử lại.")
        }
    }

    // ✅ THÊM 2: Triển khai hàm signOut
    override fun signOut() {
        firebaseAuth.signOut()

    }

    // ✅ THÊM 3: Triển khai hàm getCurrentUser (rất tiện lợi cho các ViewModel)
    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
}