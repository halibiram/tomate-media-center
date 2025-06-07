package com.halibiram.tomato.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a generic search result item from an API.
 * APIs like TMDB provide a "media_type" field to distinguish between movies, TV shows, people, etc.
 */
@Serializable
data class SearchResultDto(
    val id: String, // Assuming API provides String ID, could be Int
    @SerialName("media_type")
    val mediaType: String, // "movie", "tv", "person", etc.

    // Common fields (may be null depending on media_type)
    val name: String? = null, // For TV shows or people
    val title: String? = null, // For movies

    val overview: String? = null,
    @SerialName("poster_path")
    val posterPath: String? = null,
    @SerialName("backdrop_path")
    val backdropPath: String? = null,

    // Movie specific (example)
    @SerialName("release_date")
    val releaseDate: String? = null,

    // TV specific (example)
    @SerialName("first_air_date")
    val firstAirDate: String? = null,

    // Person specific (example)
    @SerialName("profile_path")
    val profilePath: String? = null,
    @SerialName("known_for_department")
    val knownForDepartment: String? = null,

    // Other common fields
    val popularity: Double? = null,
    @SerialName("vote_average")
    val voteAverage: Double? = null,
    @SerialName("vote_count")
    val voteCount: Int? = null,
    @SerialName("original_language")
    val originalLanguage: String? = null
    // Add other fields as needed based on your API's search response
)

// Example for a paginated search response
@Serializable
data class PaginatedSearchResponseDto(
    val page: Int,
    val results: List<SearchResultDto>,
    @SerialName("total_pages")
    val totalPages: Int,
    @SerialName("total_results")
    val totalResults: Int
)
