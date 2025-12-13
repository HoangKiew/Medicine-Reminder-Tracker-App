package com.example.medinotify.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException // ✅ Thêm import lỗi trùng email
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest // ✅ Thêm import để cập nhật tên
import com.google.firebase.firestore.FirebaseFirestore // ✅ Thêm import Firestore
import kotlinx.coroutines.tasks.await

sealed interface AuthResult {
    data class Success(val userId: String) : AuthResult
    data class Error(val message: String) : AuthResult
}

interface AuthRepository {
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signInWithGoogle(idToken: String): AuthResult
    // ✨ THÊM HÀM ĐĂNG KÝ
    suspend fun signUp(email: String, pass: String, name: String): AuthResult
}

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    // ✨ Thêm Firestore để lưu thông tin User (Tên, Email) vào DB
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
            AuthResult.Success(userId)
        } catch (exception: FirebaseNetworkException) {
            AuthResult.Error("Không thể kết nối tới máy chủ. Vui lòng thử lại sau.")
        } catch (exception: Exception) {
            AuthResult.Error(exception.message ?: "Đăng nhập Google thất bại. Vui lòng thử lại.")
        }
    }

    // ✨✨✨ HÀM ĐĂNG KÝ MỚI ✨✨✨
    override suspend fun signUp(email: String, pass: String, name: String): AuthResult {
        return try {
            // 1. Tạo tài khoản trên Firebase Auth
            val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user

            // 2. Cập nhật tên hiển thị (DisplayName) và lưu vào Firestore
            user?.let {
                // Cập nhật Profile Auth (để hiện tên khi login bằng Google/Email)
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                it.updateProfile(profileUpdates).await()

                // Lưu thông tin user vào Firestore (quan trọng để quản lý dữ liệu sau này)
                val userMap = hashMapOf(
                    "uid" to it.uid,
                    "email" to email,
                    "name" to name
                )
                firestore.collection("users").document(it.uid).set(userMap).await()
            }

            // 3. 🔴 QUAN TRỌNG: Đăng xuất ngay lập tức
            // Lý do: Firebase tự động login sau khi đăng ký.
            // Ta logout để bắt người dùng phải đăng nhập lại ở màn hình Login.
            firebaseAuth.signOut()

            AuthResult.Success(user?.uid ?: "")
        } catch (e: FirebaseAuthUserCollisionException) {
            AuthResult.Error("Email này đã được sử dụng.")
        } catch (e: FirebaseNetworkException) {
            AuthResult.Error("Lỗi kết nối mạng.")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Đăng ký thất bại.")
        }
    }
}