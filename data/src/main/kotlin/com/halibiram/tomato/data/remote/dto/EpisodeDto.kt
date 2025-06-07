package com.halibiram.tomato.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EpisodeDto(
    val id: String, // Assuming API provides String ID, could be Int
    @SerialName("air_date")
    val airDate: String?,
    @SerialName("episode_number")
    val episodeNumber: Int,
    val name: String, // 'title' in domain model
    val overview: String?,
    @SerialName("production_code")
    val productionCode: String? = null,
    @SerialName("season_number")
    val seasonNumber: Int,
    @SerialName("still_path")
    val stillPath: String?,
    @SerialName("vote_average")
    val voteAverage: Double?,
    @SerialName("vote_count")
    val voteCount: Int?,
    // Crew and guest stars could be added if API provides them
    // val crew: List<CrewMemberDto>? = null,
    // @SerialName("guest_stars")
    // val guestStars: List<GuestStarDto>? = null
)

// Example for a response that might contain a list of episodes (e.g., when fetching a season's details)
@Serializable
data class SeasonDetailsWithEpisodesDto(
    @SerialName("_id") // Some APIs use _id
    val internalId: String? = null, // Or whatever structure your API uses for season details
    val id: Int, // TMDB like season ID
    @SerialName("air_date")
    val airDate: String?,
    val episodes: List<EpisodeDto>,
    val name: String,
    val overview: String?,
    @SerialName("poster_path")
    val posterPath: String?,
    @SerialName("season_number")
    val seasonNumber: Int
)
