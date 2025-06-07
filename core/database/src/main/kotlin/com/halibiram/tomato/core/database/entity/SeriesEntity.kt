package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey
    val id: String,
    val name: String?,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val firstAirDate: Date?,
    val voteAverage: Double?,
    val voteCount: Int?,
    val numberOfSeasons: Int?,
    val numberOfEpisodes: Int?,
    // val genres: List<String>? = null, // Would need a TypeConverter
    val lastRefreshed: Date = Date()
)
