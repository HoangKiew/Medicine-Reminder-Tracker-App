package com.example.medinotify.di

import com.example.medinotify.data.auth.AuthRepository
import com.example.medinotify.data.auth.FirebaseAuthRepository
import com.example.medinotify.data.local.AppDatabase
import com.example.medinotify.data.repository.MedicineRepository
import com.example.medinotify.ui.screens.addmedicine.AddMedicineViewModel
import com.example.medinotify.ui.screens.auth.login.LoginViewModel
import com.example.medinotify.ui.screens.calendar.CalendarViewModel
import com.example.medinotify.ui.screens.history.HistoryViewModel
import com.example.medinotify.ui.screens.home.HomeViewModel
import com.example.medinotify.ui.screens.profile.ProfileViewModel
import com.example.medinotify.ui.screens.addmedicine.StartViewModel
import com.example.medinotify.ui.screens.auth.register.RegisterViewModel
import com.example.medinotify.ui.screens.auth.splash.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // --- CUNG CẤP CÁC SINGLETON (Đối tượng chỉ được tạo 1 lần trong suốt vòng đời app) ---

    // 1. Firebase Instances
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    // 2. Authentication Repository
    // Koin sẽ tự động inject 'FirebaseAuth' đã được định nghĩa ở trên vào đây.
    single<AuthRepository> { FirebaseAuthRepository(firebaseAuth = get()) }

    // 3. Room Database Instance
    single {
        AppDatabase.getDatabase(
            context = androidContext()
        )
    }

    // 4. Data Access Objects (DAOs)
    // Koin sẽ lấy AppDatabase đã được định nghĩa ở trên để tạo ra các DAO này.
    single { get<AppDatabase>().medicineDao() }
    single { get<AppDatabase>().scheduleDao() }
    single { get<AppDatabase>().logEntryDao() }

    // 5. Medicine Repository
    // Koin sẽ tự động tìm các instance cần thiết đã được định nghĩa ở trên để inject vào đây.
    single {
        MedicineRepository(
            firestore = get(),
            auth = get(), // Vẫn cần FirebaseAuth để lấy userId và signOut
            medicineDao = get(),
            scheduleDao = get(),
            logEntryDao = get()
        )
    }

    // --- CUNG CẤP CÁC VIEWMODELS (Đối tượng được tạo mới khi cần) ---
    // Koin sẽ tự động inject `AuthRepository` đã được định nghĩa ở mục (2) vào đây.
    viewModel { LoginViewModel(authRepository = get()) }

    // Các ViewModel còn lại vẫn sử dụng MedicineRepository như bình thường.
    viewModel { AddMedicineViewModel(repository = get()) }
    viewModel { HomeViewModel(repository = get()) }
    viewModel { CalendarViewModel(repository = get()) }
    viewModel { HistoryViewModel(repository = get()) }
    viewModel { ProfileViewModel(repository = get()) }
    viewModel { StartViewModel(repository = get()) }
    viewModel { RegisterViewModel(authRepository = get()) }
    viewModel { SplashViewModel() } // ✅ THÊM DÒNG NÀY
    viewModel { LoginViewModel(authRepository = get()) }
}

