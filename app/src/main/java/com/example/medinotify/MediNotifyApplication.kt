package com.example.medinotify

import android.app.Application
import com.example.medinotify.di.appModule // ✅ THÊM IMPORT cho module Koin của bạn
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MediNotifyApplication : Application() {

    // ✅ SỬA LỖI: Di chuyển toàn bộ logic khởi tạo dependency vào Koin
    override fun onCreate() {
        super.onCreate()

        // Khởi động Koin tại đây, ngay khi ứng dụng bắt đầu
        startKoin {
            // (Tùy chọn) Ghi log của Koin ra Logcat, rất hữu ích để debug
            // Ở chế độ production, bạn có thể tắt dòng này
            androidLogger()

            // Cung cấp Android context cho Koin để nó có thể sử dụng
            androidContext(this@MediNotifyApplication)

            // Tải tất cả các module dependency đã được định nghĩa trong appModule
            modules(appModule)
        }
    }
}
