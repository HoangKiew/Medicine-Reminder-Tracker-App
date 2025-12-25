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

    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    single<AuthRepository> {
        FirebaseAuthRepository(
            firebaseAuth = get(),
            firestore = get()
        )
    }

    single { AppDatabase.getDatabase(context = androidContext()) }

    // DAOs
    single { get<AppDatabase>().medicineDao() }
    single { get<AppDatabase>().scheduleDao() }
    single { get<AppDatabase>().logEntryDao() }

    // Repository
    single {
        MedicineRepository(
            firestore = get(),
            auth = get(),
            medicineDao = get(),
            scheduleDao = get(),
            logEntryDao = get()
        )
    }

    // WorkManager
    single { WorkManager.getInstance(androidContext()) }



    // 1. LoginViewModel
    viewModel {
        LoginViewModel(
            authRepository = get(),
            medicineRepository = get()
        )
    }

    // 2. RegisterViewModel
    viewModel {
        RegisterViewModel(
            authRepository = get(),
            medicineRepository = get()
        )
    }

    // 3. ProfileViewModel
    viewModel {
        ProfileViewModel(
            authRepository = get(),
            medicineRepository = get()
        )
    }

    // 4. AddMedicineViewModel
    viewModel {
        AddMedicineViewModel(
            repository = get(),
            workManager = get(),
            savedStateHandle = get()
        )
    }

    // 5. MedicineReminderViewModel
    viewModel {
        MedicineReminderViewModel(
            repository = get(),
            workManager = get()
        )
    }

    // Các ViewModel cơ bản khác
    viewModel { HomeViewModel(repository = get()) }
    viewModel { CalendarViewModel(repository = get()) }
    viewModel { HistoryViewModel(repository = get()) }
    viewModel { StartViewModel(repository = get()) }
    viewModel { SettingsViewModel(repository = get()) }
}