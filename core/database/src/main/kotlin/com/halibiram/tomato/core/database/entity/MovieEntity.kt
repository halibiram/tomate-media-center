package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: String,
    val title: String,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?, // Consider using a Date/Timestamp TypeConverter
    val voteAverage: Double?,
    val genres: List<String> = emptyList(), // Requires a TypeConverter for List<String>
    val lastRefreshed: Long = System.currentTimeMillis() // For caching strategy
)
