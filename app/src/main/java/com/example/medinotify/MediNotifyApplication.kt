package com.example.medinotify

import android.app.Application
import com.example.medinotify.di.appModule // ✅ THÊM IMPORT cho module Koin của bạn
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MediNotifyApplication : Application() {


    override fun onCreate() {
        super.onCreate()


        startKoin {

            androidLogger()


            androidContext(this@MediNotifyApplication)


            modules(appModule)
        }
    }
}
