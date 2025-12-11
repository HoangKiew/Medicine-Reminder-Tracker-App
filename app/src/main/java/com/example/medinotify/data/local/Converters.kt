package com.example.medinotify.data.local

import androidx.room.TypeConverter
import java.util.Date

/**
 * Bộ chuyển đổi kiểu dữ liệu cho Room, giúp Room hiểu cách
 * lưu trữ và đọc kiểu dữ liệu 'Date' từ cơ sở dữ liệu SQLite.
 */
class Converters {
    /**
     * Chuyển đổi từ một con số Long (timestamp) mà SQLite lưu trữ
     * thành một đối tượng Date để ứng dụng sử dụng.
     * Room sẽ tự động gọi hàm này khi đọc dữ liệu.
     * @param value Giá trị Long từ database (có thể null).
     * @return Một đối tượng Date (hoặc null).
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Chuyển đổi từ một đối tượng Date của ứng dụng
     * thành một con số Long (timestamp) để SQLite có thể lưu trữ.
     * Room sẽ tự động gọi hàm này khi ghi dữ liệu.
     * @param date Đối tượng Date từ ứng dụng (có thể null).
     * @return Một giá trị Long (hoặc null).
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
