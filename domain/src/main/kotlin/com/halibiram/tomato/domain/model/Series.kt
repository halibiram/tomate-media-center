package com.halibiram.tomato.domain.model

// Domain model for Series
data class Series(
    val id: String,
    val name: String,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val firstAirDate: String?,
    val voteAverage: Double?,
    val genres: List<String> = emptyList(),
    val numberOfSeasons: Int?,
    val numberOfEpisodes: Int?
)
