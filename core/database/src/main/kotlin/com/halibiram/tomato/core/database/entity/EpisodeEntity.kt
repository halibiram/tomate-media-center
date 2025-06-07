package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = SeriesEntity::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE // If a series is deleted, its episodes are also deleted
        )
    ],
    indices = [Index(value = ["seriesId"])]
)
data class EpisodeEntity(
    @PrimaryKey
    val id: String,
    val seriesId: String, // Foreign key to SeriesEntity
    val seasonNumber: Int,
    val episodeNumber: Int,
    val name: String?,
    val overview: String?,
    val airDate: Date?,
    val stillPath: String?, // Image for the episode
    val voteAverage: Double?,
    val voteCount: Int?,
    val lastRefreshed: Date = Date()
)
