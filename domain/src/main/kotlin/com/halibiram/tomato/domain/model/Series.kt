package com.halibiram.tomato.domain.model

data class Series(
    val id: String,
    val title: String, // Harmonized to 'title' in domain layer
    val posterPath: String?,
    val backdropPath: String?,
    val firstAirDate: String?, // Or a Date/LocalDate type
    val overview: String?,
    val voteAverage: Double?,
    val genreIds: List<Int>?
    // Add other relevant domain model fields like seasons, episodes list etc.
)
