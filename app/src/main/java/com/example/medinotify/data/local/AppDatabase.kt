package com.example.medinotify.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters // ✅ Import annotation
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
// ✨✨✨ THÊM DÒNG NÀY ĐỂ KẾT NỐI VỚI FILE CONVERTERS.KT ✨✨✨
@TypeConverters(Converters::class)
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