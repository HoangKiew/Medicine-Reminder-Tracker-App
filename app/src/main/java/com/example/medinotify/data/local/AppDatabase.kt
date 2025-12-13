package com.example.medinotify.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters // ✅ BƯỚC 1: Đảm bảo có import này
import com.example.medinotify.data.model.MedicineEntity
import com.example.medinotify.data.model.ScheduleEntity
import com.example.medinotify.data.model.LogEntryEntity

@Database(
    entities = [
        MedicineEntity::class,
        ScheduleEntity::class,
        LogEntryEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class) // ✅ BƯỚC 2: THÊM DÒNG NÀY NGAY ĐÂY
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicineDao(): MedicineDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun logEntryDao(): LogEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medinotify_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
