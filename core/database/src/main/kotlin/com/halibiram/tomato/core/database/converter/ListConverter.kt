package com.halibiram.tomato.core.database.converter

import androidx.room.TypeConverter

object ListConverter {
    // Example for List<String>. Adjust for other list types if needed.
    // For complex objects in lists, consider serializing to JSON string.
    private const val SEPARATOR = ","

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(SEPARATOR)
    }

    @TypeConverter
    fun toStringList(data: String?): List<String>? {
        return data?.split(SEPARATOR)?.map { it.trim() }?.filter { it.isNotEmpty() }
    }

    // If you need to store a list of more complex objects, JSON is a good option:
    // Example using kotlinx.serialization (add dependency if not present)
    // private val json = Json { ignoreUnknownKeys = true }
    //
    // @TypeConverter
    // fun fromGenreList(genres: List<Genre>?): String? {
    //     return genres?.let { json.encodeToString(it) }
    // }
    //
    // @TypeConverter
    // fun toGenreList(genreString: String?): List<Genre>? {
    //     return genreString?.let { json.decodeFromString<List<Genre>>(it) }
    // }
    // @Serializable
    // data class Genre(val id: Int, val name: String)
}
