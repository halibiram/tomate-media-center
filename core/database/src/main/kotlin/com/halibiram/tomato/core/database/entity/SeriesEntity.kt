package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey val id: String,
    val name: String,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val firstAirDate: String?, // Consider using a Date/Timestamp TypeConverter
    val voteAverage: Double?,
    val genres: List<String> = emptyList(), // Requires a TypeConverter for List<String>
    val numberOfSeasons: Int?,
    val numberOfEpisodes: Int?,
    val lastRefreshed: Long = System.currentTimeMillis() // For caching strategy
)
