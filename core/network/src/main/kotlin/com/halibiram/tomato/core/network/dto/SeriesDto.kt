package com.halibiram.tomato.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SeriesDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("overview") val overview: String?,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("backdrop_path") val backdropPath: String?,
    @SerialName("first_air_date") val firstAirDate: String?,
    @SerialName("vote_average") val voteAverage: Double?,
    @SerialName("genre_ids") val genreIds: List<Int>? = null,
    @SerialName("number_of_seasons") val numberOfSeasons: Int?,
    @SerialName("number_of_episodes") val numberOfEpisodes: Int?
)
