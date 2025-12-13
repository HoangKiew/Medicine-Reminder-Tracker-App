package com.example.medinotify.di

import androidx.work.WorkManager // ✅ IMPORT MỚI: Cần thiết cho tính năng hẹn giờ
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
import com.example.medinotify.ui.screens.reminder.MedicineReminderViewModel
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
    single<AuthRepository> { FirebaseAuthRepository(firebaseAuth = get()) }

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

    // ✨✨✨ 6. WORK MANAGER (QUAN TRỌNG CHO HẸN GIỜ) ✨✨✨
    // Cung cấp instance của WorkManager cho toàn bộ ứng dụng
    single { WorkManager.getInstance(androidContext()) }


    // --- CUNG CẤP CÁC VIEWMODELS (Đối tượng được tạo mới khi cần) ---

    viewModel { LoginViewModel(authRepository = get()) }

    // ✨ CẬP NHẬT: AddMedicineViewModel cần thêm WorkManager
    viewModel {
        AddMedicineViewModel(
            repository = get(),
            workManager = get() // ✅ Koin tự động lấy WorkManager ở mục 6 truyền vào đây
        )
    }

    viewModel { HomeViewModel(repository = get()) }
    viewModel { CalendarViewModel(repository = get()) }
    viewModel { HistoryViewModel(repository = get()) }
    viewModel { ProfileViewModel(repository = get()) }
    viewModel { StartViewModel(repository = get()) }

    // Cung cấp MedicineReminderViewModel (đã thêm từ bước trước)
    viewModel { MedicineReminderViewModel(repository = get()) }
}