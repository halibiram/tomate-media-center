package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.halibiram.tomato.core.database.converter.DateConverter
import java.util.Date

@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = SeriesEntity::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["seriesId"]), Index(value = ["seriesId", "seasonNumber"])] // Added compound index
)
@TypeConverters(DateConverter::class)
data class EpisodeEntity(
    @PrimaryKey
    val id: String,
    val seriesId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String?, // 'name' previously, standardized
    val overview: String?,
    val airDate: Date?,
    val stillPath: String?,
    val voteAverage: Double?, // 'rating' in domain model
    val voteCount: Int? = null, // Added
    val lastRefreshed: Date = Date() // Added
)
