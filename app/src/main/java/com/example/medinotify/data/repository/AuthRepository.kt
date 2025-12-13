package com.example.medinotify.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore // ✅ 1. Thêm import cho Firestore
import kotlinx.coroutines.tasks.await

// Lớp sealed interface AuthResult đã rất tốt, không cần thay đổi
sealed interface AuthResult {
    // Chúng ta sẽ trả về Success không cần userId, vì ViewModel có thể lấy từ nơi khác
    // hoặc không cần trực tiếp sau khi đăng nhập.
    data object Success : AuthResult
    data class Error(val message: String) : AuthResult
}

interface AuthRepository {
    // Thêm hàm signUp vào interface
    suspend fun signUp(email: String, password: String): AuthResult
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signInWithGoogle(idToken: String): AuthResult
}

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    // ✅ 2. Thêm Firestore instance để có thể ghi dữ liệu người dùng
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // ✅ 3. Thêm hàm signUp với logic ghi dữ liệu người dùng
    override suspend fun signUp(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            // Sau khi tạo tài khoản thành công, ghi thông tin vào Firestore
            result.user?.let { user ->
                val userDocument = firestore.collection("users").document(user.uid)
                val userData = mapOf(
                    "uid" to user.uid,
                    "email" to user.email,
                    "createdAt" to System.currentTimeMillis()
                )
                // Dùng set(userData) để tạo document với các trường dữ liệu
                userDocument.set(userData).await()
            }
            AuthResult.Success
        } catch (exception: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Địa chỉ email không hợp lệ.")
        } catch (exception: Exception) {
            // Lỗi email đã tồn tại cũng sẽ rơi vào đây
            AuthResult.Error(exception.message ?: "Đăng ký thất bại. Vui lòng thử lại.")
        }
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            // Đăng nhập thành công, không cần làm gì thêm ở đây
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
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()

            // ✅ 4. Kiểm tra nếu là người dùng mới thì ghi thông tin vào Firestore
            val isNewUser = result.additionalUserInfo?.isNewUser ?: false
            result.user?.let { user ->
                // Chỉ ghi thông tin vào Firestore nếu đây là LẦN ĐẦU TIÊN họ đăng nhập
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
}
