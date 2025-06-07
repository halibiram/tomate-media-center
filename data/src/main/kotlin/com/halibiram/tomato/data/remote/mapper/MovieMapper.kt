package com.halibiram.tomato.data.remote.mapper

import com.halibiram.tomato.data.remote.dto.GenreDto
import com.halibiram.tomato.data.remote.dto.MovieDto
import com.halibiram.tomato.domain.model.Movie // Assuming Movie is the primary domain model for lists
import com.halibiram.tomato.domain.model.MovieDetails // For detailed views

// Helper function to map GenreDto to genre name string
fun mapGenres(genreDtos: List<GenreDto>?): List<String>? {
    return genreDtos?.map { it.name }
}

// Helper function to map genre IDs if you have a local genre map/service
// fun mapGenreIdsToNames(genreIds: List<Int>?, genreMap: Map<Int, String>): List<String>? {
//     return genreIds?.mapNotNull { id -> genreMap[id] }
// }

fun MovieDto.toDomain(): Movie {
    return Movie(
        id = this.id.toString(), // Ensure ID is string if API gives Int
        title = this.title,
        description = this.overview ?: "", // Use overview for description
        posterUrl = this.posterPath, // Assuming full path is constructed by repository/service or this is already full
        releaseDate = this.releaseDate,
        // genres = mapGenreIdsToNames(this.genreIds, localGenreMap) ?: this.genres?.let { mapGenres(it) },
        genres = this.genres?.let { mapGenres(it) }, // Simpler mapping if full GenreDto is available
        rating = this.voteAverage
    )
}

fun MovieDto.toDomainDetails(): MovieDetails {
    return MovieDetails(
        id = this.id.toString(),
        title = this.title,
        overview = this.overview ?: "",
        posterUrl = this.posterPath,
        backdropUrl = this.backdropPath,
        releaseDate = this.releaseDate,
        // genres = mapGenreIdsToNames(this.genreIds, localGenreMap) ?: this.genres?.let { mapGenres(it) },
        genres = this.genres?.let { mapGenres(it) },
        rating = this.voteAverage,
        runtimeMinutes = this.runtime
    )
}

fun List<MovieDto>.toDomain(): List<Movie> {
    return this.map { it.toDomain() }
}
