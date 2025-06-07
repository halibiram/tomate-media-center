package com.halibiram.tomato.domain.model

data class Movie(
    val id: String,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?, // Or a Date/LocalDate type
    val overview: String?,
    val voteAverage: Double?,
    val adult: Boolean?,
    val genreIds: List<Int>?
    // Add other relevant domain model fields
)
