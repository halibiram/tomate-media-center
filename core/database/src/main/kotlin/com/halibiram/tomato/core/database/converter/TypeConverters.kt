package com.halibiram.tomato.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TypeConverters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { Json.decodeFromString<List<String>>(it) }
    }

    // Add more converters if needed (e.g., for Date/Timestamp)
    // @TypeConverter
    // fun fromTimestamp(value: Long?): Date? {
    //     return value?.let { Date(it) }
    // }

    // @TypeConverter
    // fun dateToTimestamp(date: Date?): Long? {
    //     return date?.time
    // }
}
