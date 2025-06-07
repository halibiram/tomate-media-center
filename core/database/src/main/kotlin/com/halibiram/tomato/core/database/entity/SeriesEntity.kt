package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// SeriesEntity
@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey val id: String,
    val title: String,
    val posterPath: String?,
    val firstAirDate: Long?,
    val overview: String?
)
