package com.halibiram.tomato.core.database.converter

import androidx.room.TypeConverter
import com.google.gson.Gson // Using Gson for simplicity, could use kotlinx.serialization too
import com.google.gson.reflect.TypeToken

object ListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String?): List<String>? {
        if (value == null) {
            return null
        }
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>?): String? {
        if (list == null) {
            return null
        }
        return gson.toJson(list)
    }

    // Example for List<Int> if needed elsewhere, or make generic
    @TypeConverter
    fun fromIntListString(value: String?): List<Int>? {
        if (value == null) {
            return null
        }
        val listType = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromIntList(list: List<Int>?): String? {
        if (list == null) {
            return null
        }
        return gson.toJson(list)
    }
}
