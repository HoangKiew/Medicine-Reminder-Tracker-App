package com.example.medinotify.di

import androidx.work.WorkManager
import com.example.medinotify.data.auth.AuthRepository
import com.example.medinotify.data.auth.FirebaseAuthRepository
import com.example.medinotify.data.local.AppDatabase
import com.example.medinotify.data.repository.MedicineRepository
import com.example.medinotify.ui.screens.addmedicine.AddMedicineViewModel
import com.example.medinotify.ui.screens.addmedicine.StartViewModel
import com.example.medinotify.ui.screens.auth.login.LoginViewModel // Cần sửa
import com.example.medinotify.ui.screens.auth.register.RegisterViewModel // Cần sửa
import com.example.medinotify.ui.screens.calendar.CalendarViewModel
import com.example.medinotify.ui.screens.history.HistoryViewModel
import com.example.medinotify.ui.screens.home.HomeViewModel
import com.example.medinotify.ui.screens.profile.ProfileViewModel // Cần sửa
import com.example.medinotify.ui.screens.reminder.MedicineReminderViewModel
import com.example.medinotify.ui.screens.settings.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // --- CUNG CẤP CÁC SINGLETON (Giữ nguyên) ---
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single<AuthRepository> {
        FirebaseAuthRepository(firebaseAuth = get(), firestore = get())
    }
    single { AppDatabase.getDatabase(context = androidContext()) }
    single { get<AppDatabase>().medicineDao() }
    single { get<AppDatabase>().scheduleDao() }
    single { get<AppDatabase>().logEntryDao() }
    single {
        MedicineRepository(
            firestore = get(),
            auth = get(),
            medicineDao = get(),
            scheduleDao = get(),
            logEntryDao = get()
        )
    }
    single { WorkManager.getInstance(androidContext()) }


    // --- CUNG CẤP CÁC VIEWMODELS (ĐÃ SỬA LỖI DEPENDENCY) ---

    // 1. LoginViewModel: Cần Auth + MedicineRepo để sync data
    viewModel {
        LoginViewModel(
            authRepository = get(),
            medicineRepository = get() // ✅ ĐÃ THÊM: Cần MedicineRepo để gọi syncData
        )
    }

    // 2. RegisterViewModel: Cần Auth + MedicineRepo để sync data sau khi đăng ký
    viewModel {
        RegisterViewModel(
            authRepository = get(),
            medicineRepository = get() // ✅ ĐÃ THÊM: Cần MedicineRepo để gọi syncData
        )
    }

    // 3. ProfileViewModel: Cần Auth + MedicineRepo để signOut và clearLocalData
    viewModel {
        ProfileViewModel(
            authRepository = get(), // ✅ ĐÃ THÊM: Cần AuthRepo để signOut
            medicineRepository = get()
        )
    }

    // Các ViewModel khác (Giữ nguyên, cần Repository đã được cung cấp)
    viewModel { AddMedicineViewModel(repository = get(), workManager = get(), savedStateHandle = get()) }
    viewModel { HomeViewModel(repository = get()) }
    viewModel { CalendarViewModel(repository = get()) }
    viewModel { HistoryViewModel(repository = get()) }
    viewModel { StartViewModel(repository = get()) }
    viewModel { MedicineReminderViewModel(repository = get()) }
    viewModel { SettingsViewModel(repository = get()) }
}