package com.example.medinotify.di

import androidx.work.WorkManager
import com.example.medinotify.data.auth.AuthRepository
import com.example.medinotify.data.auth.FirebaseAuthRepository
import com.example.medinotify.data.local.AppDatabase
import com.example.medinotify.data.repository.MedicineRepository
import com.example.medinotify.ui.screens.addmedicine.AddMedicineViewModel
import com.example.medinotify.ui.screens.addmedicine.StartViewModel
import com.example.medinotify.ui.screens.auth.login.LoginViewModel
import com.example.medinotify.ui.screens.auth.register.RegisterViewModel
import com.example.medinotify.ui.screens.calendar.CalendarViewModel
import com.example.medinotify.ui.screens.history.HistoryViewModel
import com.example.medinotify.ui.screens.home.HomeViewModel
import com.example.medinotify.ui.screens.profile.ProfileViewModel
import com.example.medinotify.ui.screens.reminder.MedicineReminderViewModel
import com.example.medinotify.ui.screens.settings.SettingsViewModel
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
    single<AuthRepository> {
        FirebaseAuthRepository(
            firebaseAuth = get(),
            firestore = get()
        )
    }

    // 3. Room Database Instance
    single {
        AppDatabase.getDatabase(
            context = androidContext()
        )
    }

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

    // 6. WORK MANAGER
    single { WorkManager.getInstance(androidContext()) }


    // --- CUNG CẤP CÁC VIEWMODELS ---

    viewModel { LoginViewModel(authRepository = get()) }

    viewModel { RegisterViewModel(authRepository = get()) }

    // ✅ ĐÃ SỬA: Thêm savedStateHandle = get()
    viewModel {
        AddMedicineViewModel(
            repository = get(),
            workManager = get(),
            savedStateHandle = get()
        )
    }

    viewModel { HomeViewModel(repository = get()) }
    viewModel { CalendarViewModel(repository = get()) }
    viewModel { HistoryViewModel(repository = get()) }
    viewModel { ProfileViewModel(repository = get()) }
    viewModel { StartViewModel(repository = get()) }
    viewModel { MedicineReminderViewModel(repository = get()) }
    viewModel { SettingsViewModel(repository = get()) }
}