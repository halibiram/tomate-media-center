package com.halibiram.tomato.domain.model

data class Series(
    val id: String,
    val title: String, // 'name' in SeriesRepository.kt, standardizing to 'title'
    val description: String?, // New field
    val posterUrl: String?,
    val firstAirDate: String?,
    val genres: List<String>?, // Changed to nullable
    val rating: Double?, // Changed to nullable
    val totalSeasons: Int? // Changed to nullable
)

// If SeriesDetails is needed as a separate, more detailed domain model:
data class SeriesDetails(
    val id: String,
    val title: String,
    val overview: String, // 'description' in Series, 'overview' here. Standardizing.
    val posterUrl: String?,
    val backdropUrl: String?,
    val firstAirDate: String?,
    val genres: List<String>?,
    val rating: Double?,
    val numberOfSeasons: Int?, // 'totalSeasons' in Series
    val numberOfEpisodes: Int?,
    val seasons: List<Season>? = null // List of seasons with their episodes
    // Add other fields like cast, crew, network, etc.
)

data class Season(
    val id: String, // Or season number as ID if API provides it that way
    val seasonNumber: Int,
    val name: String?,
    val overview: String?,
    val posterUrl: String?,
    val airDate: String?,
    val episodeCount: Int
)
