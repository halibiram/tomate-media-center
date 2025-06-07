package com.halibiram.tomato.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.halibiram.tomato.core.database.converter.DateConverter
import com.halibiram.tomato.core.database.converter.ListConverter
import java.util.Date

@Entity(tableName = "movies")
@TypeConverters(DateConverter::class, ListConverter::class)
data class MovieEntity(
    @PrimaryKey
    val id: String,
    val title: String?, // Made nullable to match domain model flexibility
    val overview: String?, // Changed from description, to match domain model
    val posterUrl: String?,
    val backdropUrl: String?, // Added for MovieDetails consistency
    val releaseDate: Date?,
    val genres: List<String>?,
    val voteAverage: Double?, // 'rating' in domain model
    val voteCount: Int? = null, // Added
    val runtimeMinutes: Int? = null, // Added
    val popularity: Double? = null, // Added
    val lastRefreshed: Date = Date() // For cache expiry logic
)
