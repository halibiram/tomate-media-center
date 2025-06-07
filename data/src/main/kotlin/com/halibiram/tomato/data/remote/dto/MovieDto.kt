package com.halibiram.tomato.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieDto(
    val id: String, // Assuming API provides String ID, could be Int
    val title: String,
    val overview: String?, // Changed from description to match domain model standardization
    @SerialName("poster_path")
    val posterPath: String?,
    @SerialName("backdrop_path")
    val backdropPath: String?, // For MovieDetails
    @SerialName("release_date")
    val releaseDate: String?,
    @SerialName("genre_ids") // APIs often send genre IDs
    val genreIds: List<Int>?, // These would need mapping to genre names
    val genres: List<GenreDto>? = null, // Some APIs might send full genre objects
    @SerialName("vote_average")
    val voteAverage: Double?, // 'rating' in domain model
    @SerialName("vote_count")
    val voteCount: Int? = null,
    val adult: Boolean? = false,
    @SerialName("original_language")
    val originalLanguage: String? = null,
    @SerialName("original_title")
    val originalTitle: String? = null,
    val popularity: Double? = null,
    @SerialName("video") // If API indicates trailer availability
    val video: Boolean? = false,
    val runtime: Int? = null // For MovieDetails (in minutes)
)

@Serializable
data class GenreDto(
    val id: Int,
    val name: String
)

// Example for a paginated response if your API returns it like this
@Serializable
data class PaginatedMovieResponseDto(
    val page: Int,
    val results: List<MovieDto>,
    @SerialName("total_pages")
    val totalPages: Int,
    @SerialName("total_results")
    val totalResults: Int
)
