package com.example.medinotify.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser // ✅ Import FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

sealed interface AuthResult {
    data class Success(val userId: String) : AuthResult
    data class Error(val message: String) : AuthResult
}

interface AuthRepository {
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun signUp(email: String, pass: String, name: String): AuthResult

    // ✅ THÊM HÀM UTILITY: Cần cho ProfileViewModel và SplashViewModel
    fun getCurrentUser(): FirebaseUser?
    fun signOut()
}

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
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
            AuthResult.Success(userId) // ✅ Đã sửa để trả về userId
        } catch (exception: FirebaseNetworkException) {
            AuthResult.Error("Không thể kết nối tới máy chủ. Vui lòng thử lại sau.")
        } catch (exception: Exception) {
            AuthResult.Error(exception.message ?: "Đăng nhập Google thất bại. Vui lòng thử lại.")
        }
    }

    override suspend fun signUp(email: String, pass: String, name: String): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user

            user?.let {
                // Cập nhật Profile Auth (DisplayName)
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                it.updateProfile(profileUpdates).await()

                // Lưu thông tin user vào Firestore
                val userMap = hashMapOf(
                    "uid" to it.uid,
                    "email" to email,
                    "name" to name
                )
                firestore.collection("users").document(it.uid).set(userMap).await()
            }

            // Ghi chú: Nếu bạn muốn người dùng phải đăng nhập lại, giữ dòng này.
            // Nếu bạn muốn chuyển thẳng sang màn hình Home sau đăng ký, hãy xóa nó.
            // firebaseAuth.signOut()

            AuthResult.Success(user?.uid ?: "")
        } catch (e: FirebaseAuthUserCollisionException) {
            AuthResult.Error("Email này đã được sử dụng.")
        } catch (e: FirebaseNetworkException) {
            AuthResult.Error("Lỗi kết nối mạng.")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Đăng ký thất bại.")
        }
    }

    // ✅ THÊM IMPLEMENTATION: Cần cho ProfileViewModel
    override fun signOut() {
        firebaseAuth.signOut()
    }

    // ✅ THÊM IMPLEMENTATION: Cần cho ProfileViewModel và SplashViewModel
    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
}