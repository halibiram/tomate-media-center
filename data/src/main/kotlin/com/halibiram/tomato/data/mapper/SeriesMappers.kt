package com.halibiram.tomato.data.mapper

import com.halibiram.tomato.core.database.entity.SeriesEntity
import com.halibiram.tomato.core.network.dto.SeriesDto
import com.halibiram.tomato.domain.model.Series

fun SeriesDto.toDomain(): Series {
    return Series(
        id = this.id,
        name = this.name,
        overview = this.overview,
        posterPath = this.posterPath,
        backdropPath = this.backdropPath,
        firstAirDate = this.firstAirDate,
        voteAverage = this.voteAverage,
        genres = this.genreIds?.map { it.toString() } ?: emptyList(),
        numberOfSeasons = this.numberOfSeasons,
        numberOfEpisodes = this.numberOfEpisodes
    )
}

fun SeriesDto.toEntity(): SeriesEntity {
    return SeriesEntity(
        id = this.id,
        name = this.name,
        overview = this.overview,
        posterPath = this.posterPath,
        backdropPath = this.backdropPath,
        firstAirDate = this.firstAirDate,
        voteAverage = this.voteAverage,
        genres = this.genreIds?.map { it.toString() } ?: emptyList(),
        numberOfSeasons = this.numberOfSeasons,
        numberOfEpisodes = this.numberOfEpisodes,
        lastRefreshed = System.currentTimeMillis()
    )
}

fun SeriesEntity.toDomain(): Series {
    return Series(
        id = this.id,
        name = this.name,
        overview = this.overview,
        posterPath = this.posterPath,
        backdropPath = this.backdropPath,
        firstAirDate = this.firstAirDate,
        voteAverage = this.voteAverage,
        genres = this.genres,
        numberOfSeasons = this.numberOfSeasons,
        numberOfEpisodes = this.numberOfEpisodes
    )
}
