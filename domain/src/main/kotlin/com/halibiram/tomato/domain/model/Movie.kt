package com.halibiram.tomato.domain.model

data class Movie(
    val id: String,
    val title: String,
    val description: String,
    val posterUrl: String?, // Changed to nullable to match previous usage
    val releaseDate: String?, // Changed to nullable
    val genres: List<String>?, // Changed to nullable
    val rating: Double? // Changed to nullable
)

// This replaces the Movie data class previously defined in MovieRepository.kt
// If MovieDetails is still needed as a separate, more detailed domain model, it should be defined here too.
// For now, assuming Movie covers the details needed. If not, MovieDetails can be added.
data class MovieDetails(
    val id: String,
    val title: String,
    val overview: String, // 'description' in Movie, 'overview' here. Standardizing to 'overview' or 'description' is good. Let's use 'overview'.
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String?,
    val genres: List<String>?,
    val rating: Double?,
    val runtimeMinutes: Int? = null, // Example additional detail
    // Add other fields like cast, crew, trailers, etc.
)
