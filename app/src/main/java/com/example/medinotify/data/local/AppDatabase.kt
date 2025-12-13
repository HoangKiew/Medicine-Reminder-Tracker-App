package com.example.medinotify.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
                    // Gợi ý: Trong quá trình phát triển, dòng này rất hữu ích
                    // Nó sẽ tự động xóa và tạo lại DB nếu bạn thay đổi cấu trúc (schema)
                    // mà không tăng version. Hãy xóa nó đi trước khi phát hành ứng dụng.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
