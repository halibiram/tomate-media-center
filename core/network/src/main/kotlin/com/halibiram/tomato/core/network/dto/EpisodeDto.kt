package com.halibiram.tomato.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EpisodeDto(
    @SerialName("id") val id: String,
    @SerialName("season_number") val seasonNumber: Int,
    @SerialName("episode_number") val episodeNumber: Int,
    @SerialName("name") val name: String,
    @SerialName("overview") val overview: String?,
    @SerialName("still_path") val stillPath: String?,
    @SerialName("air_date") val airDate: String?
)
