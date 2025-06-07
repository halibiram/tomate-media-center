package com.halibiram.tomato.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreditsDto(
    val id: Int? = null, // ID of the movie or series the credits belong to
    val cast: List<CastMemberDto>? = null,
    val crew: List<CrewMemberDto>? = null
)

@Serializable
data class CastMemberDto(
    val adult: Boolean? = null,
    val gender: Int? = null,
    val id: Int,
    @SerialName("known_for_department")
    val knownForDepartment: String? = null,
    val name: String,
    @SerialName("original_name")
    val originalName: String? = null,
    val popularity: Double? = null,
    @SerialName("profile_path")
    val profilePath: String? = null,
    @SerialName("cast_id")
    val castId: Int? = null, // Specific to cast role
    val character: String? = null,
    @SerialName("credit_id")
    val creditId: String? = null,
    val order: Int? = null // Order of appearance or importance
)

@Serializable
data class CrewMemberDto(
    val adult: Boolean? = null,
    val gender: Int? = null,
    val id: Int,
    @SerialName("known_for_department")
    val knownForDepartment: String? = null,
    val name: String,
    @SerialName("original_name")
    val originalName: String? = null,
    val popularity: Double? = null,
    @SerialName("profile_path")
    val profilePath: String? = null,
    @SerialName("credit_id")
    val creditId: String? = null,
    val department: String? = null, // e.g., "Directing", "Writing", "Sound"
    val job: String? = null // e.g., "Director", "Screenplay", "Original Music Composer"
)
