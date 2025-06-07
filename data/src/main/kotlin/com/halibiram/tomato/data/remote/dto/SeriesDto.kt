package com.halibiram.tomato.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SeriesDto(
    val id: String, // Assuming API provides String ID, could be Int
    val name: String, // 'title' in domain model
    val overview: String?,
    @SerialName("poster_path")
    val posterPath: String?,
    @SerialName("backdrop_path")
    val backdropPath: String?,
    @SerialName("first_air_date")
    val firstAirDate: String?,
    @SerialName("genre_ids")
    val genreIds: List<Int>? = null, // Similar to MovieDto, may need mapping
    val genres: List<GenreDto>? = null, // If API provides full genre objects
    @SerialName("vote_average")
    val voteAverage: Double?, // 'rating' in domain model
    @SerialName("vote_count")
    val voteCount: Int? = null,
    @SerialName("origin_country")
    val originCountry: List<String>? = null,
    @SerialName("original_language")
    val originalLanguage: String? = null,
    @SerialName("original_name")
    val originalName: String? = null,
    val popularity: Double? = null,
    @SerialName("number_of_seasons")
    val numberOfSeasons: Int? = null, // 'totalSeasons' in domain model
    @SerialName("number_of_episodes")
    val numberOfEpisodes: Int? = null,
    val seasons: List<SeasonDto>? = null // For SeriesDetails
)

@Serializable
data class SeasonDto(
    @SerialName("air_date")
    val airDate: String?,
    @SerialName("episode_count")
    val episodeCount: Int,
    val id: Int, // API might use Int for season ID
    val name: String,
    val overview: String?,
    @SerialName("poster_path")
    val posterPath: String?,
    @SerialName("season_number")
    val seasonNumber: Int
)

// Example for a paginated response
@Serializable
data class PaginatedSeriesResponseDto(
    val page: Int,
    val results: List<SeriesDto>,
    @SerialName("total_pages")
    val totalPages: Int,
    @SerialName("total_results")
    val totalResults: Int
)
