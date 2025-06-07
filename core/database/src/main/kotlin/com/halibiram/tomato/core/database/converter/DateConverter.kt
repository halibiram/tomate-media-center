package com.halibiram.tomato.core.database.converter

import androidx.room.TypeConverter
import java.util.Date

// DateConverter for Room
object DateConverter {
    @TypeConverter
    @JvmStatic
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    @TypeConverter
    @JvmStatic
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}
