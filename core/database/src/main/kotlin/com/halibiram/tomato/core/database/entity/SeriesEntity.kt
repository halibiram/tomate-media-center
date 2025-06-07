package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.halibiram.tomato.core.database.converter.DateConverter
import com.halibiram.tomato.core.database.converter.ListConverter
import java.util.Date

@Entity(tableName = "series")
@TypeConverters(DateConverter::class, ListConverter::class)
data class SeriesEntity(
    @PrimaryKey
    val id: String,
    val title: String?, // 'name' previously, standardized to 'title'
    val overview: String?, // 'description' previously
    val posterUrl: String?, // 'posterPath' previously
    val backdropUrl: String?, // 'backdropPath' previously, added for consistency
    val firstAirDate: Date?,
    val genres: List<String>?,
    val voteAverage: Double?, // 'rating' in domain model
    val voteCount: Int? = null, // Added
    val numberOfSeasons: Int?, // 'totalSeasons' in domain model
    val numberOfEpisodes: Int? = null, // Added
    val popularity: Double? = null, // Added
    val lastRefreshed: Date = Date()
)
