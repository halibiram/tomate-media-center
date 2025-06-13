package com.halibiram.tomato.core.database.converter

import androidx.room.TypeConverter

// ListConverter for Room (e.g., for a list of strings)
object ListConverter {
    @TypeConverter
    @JvmStatic
    fun fromString(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }
    }

    @TypeConverter
    @JvmStatic
    fun fromList(list: List<String>?): String? {
        return list?.joinToString(",")
    }
}
