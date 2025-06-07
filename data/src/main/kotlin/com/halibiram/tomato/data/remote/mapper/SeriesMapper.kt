package com.halibiram.tomato.data.remote.mapper

import com.halibiram.tomato.data.remote.dto.SeasonDto
import com.halibiram.tomato.data.remote.dto.SeriesDto
import com.halibiram.tomato.domain.model.Series // Domain model for lists
import com.halibiram.tomato.domain.model.SeriesDetails // Domain model for details
import com.halibiram.tomato.domain.model.Season as DomainSeason // Alias to avoid name clash

// Assuming mapGenres is already defined in MovieMapper.kt or a shared file.
// If not, it should be defined here or imported. For this example, assume it's accessible.
// fun mapGenres(genreDtos: List<GenreDto>?): List<String>? { ... } from MovieMapper or common mapper util

fun SeriesDto.toDomain(): Series {
    return Series(
        id = this.id.toString(),
        title = this.name, // DTO uses 'name', Domain uses 'title'
        description = this.overview,
        posterUrl = this.posterPath,
        firstAirDate = this.firstAirDate,
        genres = this.genres?.let { mapGenres(it) }, // mapGenres from MovieMapper or common
        rating = this.voteAverage,
        totalSeasons = this.numberOfSeasons
    )
}

fun SeriesDto.toDomainDetails(): SeriesDetails {
    return SeriesDetails(
        id = this.id.toString(),
        title = this.name,
        overview = this.overview ?: "",
        posterUrl = this.posterPath,
        backdropUrl = this.backdropPath,
        firstAirDate = this.firstAirDate,
        genres = this.genres?.let { mapGenres(it) },
        rating = this.voteAverage,
        numberOfSeasons = this.numberOfSeasons,
        numberOfEpisodes = this.numberOfEpisodes,
        seasons = this.seasons?.map { it.toDomain() }
    )
}

fun SeasonDto.toDomain(): DomainSeason {
    return DomainSeason(
        id = this.id.toString(), // Or use seasonNumber if ID is not stable/meaningful
        seasonNumber = this.seasonNumber,
        name = this.name,
        overview = this.overview,
        posterUrl = this.posterPath,
        airDate = this.airDate,
        episodeCount = this.episodeCount
    )
}

fun List<SeriesDto>.toDomain(): List<Series> {
    return this.map { it.toDomain() }
}
