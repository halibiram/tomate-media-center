package com.halibiram.tomato.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// SearchResultDto can represent a movie, series, or person from a multi-search
@Serializable
data class SearchResultDto(
    val id: Int,
    @SerialName("media_type") val mediaType: String, // "movie", "tv", "person"
    // Movie specific fields (nullable)
    val title: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    // Series specific fields (nullable)
    @SerialName("name") val name: String? = null, // For TV series
    @SerialName("first_air_date") val firstAirDate: String? = null,
    // Person specific fields (nullable)
    @SerialName("profile_path") val profilePath: String? = null,
    // Common fields
    val overview: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null
)
