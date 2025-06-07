package com.halibiram.tomato.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SeriesDto(
    val id: Int,
    @SerialName("name") val name: String, // Series often use 'name' instead of 'title'
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("backdrop_path") val backdropPath: String?,
    @SerialName("first_air_date") val firstAirDate: String?, // Consider custom date serializer
    val overview: String?,
    @SerialName("vote_average") val voteAverage: Double?,
    @SerialName("genre_ids") val genreIds: List<Int>?
    // Add other fields like seasons, number_of_episodes, etc.
)
