package com.example.medinotify.di

import com.example.medinotify.data.auth.AuthRepository
import com.example.medinotify.data.auth.FirebaseAuthRepository
import com.example.medinotify.data.local.AppDatabase
import com.example.medinotify.data.repository.MedicineRepository
// Import tất cả ViewModels
import com.example.medinotify.ui.screens.addmedicine.AddMedicineViewModel
import com.example.medinotify.ui.screens.auth.login.LoginViewModel
import com.example.medinotify.ui.screens.calendar.CalendarViewModel
import com.example.medinotify.ui.screens.history.HistoryViewModel
import com.example.medinotify.ui.screens.home.HomeViewModel
import com.example.medinotify.ui.screens.profile.ProfileViewModel
import com.example.medinotify.ui.screens.addmedicine.StartViewModel
import com.example.medinotify.ui.screens.auth.register.RegisterViewModel // ✅ RegisterViewModel
import com.example.medinotify.ui.screens.auth.splash.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // --- CUNG CẤP CÁC SINGLETON ---

    // 1. Firebase Instances
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    // 2. Authentication Repository
    single<AuthRepository> { FirebaseAuthRepository(firebaseAuth = get()) }

    // 3. Room Database Instance
    single { AppDatabase.getDatabase(context = androidContext()) }

    // 4. Data Access Objects (DAOs)
    single { get<AppDatabase>().medicineDao() }
    single { get<AppDatabase>().scheduleDao() }
    single { get<AppDatabase>().logEntryDao() }

    // 5. Medicine Repository
    single {
        MedicineRepository(
            firestore = get(),
            auth = get(),
            medicineDao = get(),
            scheduleDao = get(),
            logEntryDao = get()
        )
    }

    // --- CUNG CẤP CÁC VIEWMODELS ---

    // LoginViewModel
    viewModel {
        LoginViewModel(
            authRepository = get(),
            repository = get() // MedicineRepository
        )
    }

    // ProfileViewModel
    viewModel {
        ProfileViewModel(
            authRepository = get(),
            medicineRepository = get()
        )
    }

    // ✅ FIX CHO CRASH: RegisterViewModel
    // Khai báo rõ ràng dependency cho RegisterViewModel
    viewModel {
        RegisterViewModel(
            authRepository = get()
        )
    }
    // Lỗi có thể do bạn không sử dụng koinViewModel() hoặc không import đúng
    // Nếu RegisterViewModel chỉ có 1 dependency, bạn có thể rút gọn:
    // viewModel { RegisterViewModel(get()) }


    // Các ViewModels còn lại (Đã đúng)
    viewModel { AddMedicineViewModel(repository = get()) }
    viewModel { HomeViewModel(repository = get()) }
    viewModel { CalendarViewModel(repository = get()) }
    viewModel { HistoryViewModel(repository = get()) }
    viewModel { StartViewModel(repository = get()) }
    viewModel { SplashViewModel(repository = get()) }
}