package com.halibiram.tomato.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieDto(
    val id: Int,
    val title: String,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("backdrop_path") val backdropPath: String?,
    @SerialName("release_date") val releaseDate: String?, // Consider custom date serializer
    val overview: String?,
    @SerialName("vote_average") val voteAverage: Double?,
    val adult: Boolean?,
    @SerialName("genre_ids") val genreIds: List<Int>?
    // Add other fields as per API response
)

// Example of a generic paginated response DTO, could be in a common DTO file
// @Serializable
// data class PaginatedResponseDto<T>(
//    val page: Int,
//    val results: List<T>,
//    @SerialName("total_pages") val totalPages: Int,
//    @SerialName("total_results") val totalResults: Int
// )
