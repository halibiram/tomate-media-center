package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey
    val id: String,
    val title: String?,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: Date?,
    val voteAverage: Double?,
    val voteCount: Int?,
    val runtimeMinutes: Int?,
    // Add other relevant fields like genres, production companies etc.
    // val genres: List<String>? = null, // Would need a TypeConverter for List<String>
    val lastRefreshed: Date = Date() // For cache expiry logic
)
