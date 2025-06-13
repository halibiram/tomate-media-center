package com.halibiram.tomato.data.mapper

import com.halibiram.tomato.core.database.entity.MovieEntity
import com.halibiram.tomato.core.network.dto.MovieDto
import com.halibiram.tomato.domain.model.Movie

fun MovieDto.toDomain(): Movie {
    return Movie(
        id = this.id,
        title = this.title,
        overview = this.overview,
        posterPath = this.posterPath,
        backdropPath = this.backdropPath,
        releaseDate = this.releaseDate,
        voteAverage = this.voteAverage,
        genres = this.genreIds?.map { it.toString() } ?: emptyList()
    )
}

fun MovieDto.toEntity(): MovieEntity {
    return MovieEntity(
        id = this.id,
        title = this.title,
        overview = this.overview,
        posterPath = this.posterPath,
        backdropPath = this.backdropPath,
        releaseDate = this.releaseDate,
        voteAverage = this.voteAverage,
        genres = this.genreIds?.map { it.toString() } ?: emptyList(),
        lastRefreshed = System.currentTimeMillis()
    )
}

fun MovieEntity.toDomain(): Movie {
    return Movie(
        id = this.id,
        title = this.title,
        overview = this.overview,
        posterPath = this.posterPath,
        backdropPath = this.backdropPath,
        releaseDate = this.releaseDate,
        voteAverage = this.voteAverage,
        genres = this.genres
    )
}
