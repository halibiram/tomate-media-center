package com.halibiram.tomato.data.remote.mapper

import com.halibiram.tomato.data.remote.dto.SearchResultDto
import com.halibiram.tomato.domain.model.Movie // Using existing domain models
import com.halibiram.tomato.domain.model.Series // Using existing domain models
// You might want a more generic Domain SearchResultItem if types are very diverse
// or map to specific domain types like Movie, Series, Person directly.

// For this example, let's assume we want to map SearchResultDto to either Movie or Series domain models.
// A more generic approach might be a sealed class DomainSearchResult.

fun SearchResultDto.toDomainMovie(): Movie? {
    if (this.mediaType != "movie") return null
    return Movie(
        id = this.id.toString(),
        title = this.title ?: this.name ?: "Unknown Title",
        description = this.overview ?: "",
        posterUrl = this.posterPath,
        releaseDate = this.releaseDate,
        genres = null, // Search results often don't include full genre names
        rating = this.voteAverage
    )
}

fun SearchResultDto.toDomainSeries(): Series? {
    if (this.mediaType != "tv") return null // "tv" is common for series in TMDB like APIs
    return Series(
        id = this.id.toString(),
        title = this.name ?: this.title ?: "Unknown Title",
        description = this.overview ?: "",
        posterUrl = this.posterPath,
        firstAirDate = this.firstAirDate,
        genres = null, // Search results often don't include full genre names
        rating = this.voteAverage,
        totalSeasons = null // Typically not in search results
    )
}

// It's also common to have a generic DomainSearchResult sealed class
// sealed class DomainSearchResult {
//     data class MovieResult(val movie: Movie) : DomainSearchResult()
//     data class SeriesResult(val series: Series) : DomainSearchResult()
//     data class PersonResult(val person: Person) : DomainSearchResult() // Assuming Person domain model
//     object UnknownResult : DomainSearchResult()
// }
//
// fun SearchResultDto.toDomainSearchResult(): DomainSearchResult {
//    return when (this.mediaType) {
//        "movie" -> this.toDomainMovie()?.let { DomainSearchResult.MovieResult(it) } ?: DomainSearchResult.UnknownResult
//        "tv"    -> this.toDomainSeries()?.let { DomainSearchResult.SeriesResult(it) } ?: DomainSearchResult.UnknownResult
//        // "person" -> ... map to Person domain model
//        else    -> DomainSearchResult.UnknownResult
//    }
// }
//
// fun List<SearchResultDto>.toDomainSearchResults(): List<DomainSearchResult> {
//    return this.mapNotNull { it.toDomainSearchResult() } // mapNotNull if UnknownResult should be filtered
// }

// For simplicity, if the use case/repository for search is expected to return List<Any> or handle mixed types,
// or if different use cases search for specific types (SearchMoviesUseCase, SearchSeriesUseCase),
// then specific mappers like toDomainMovie() and toDomainSeries() are fine.
// The SearchMoviesUseCase might use a repository method that internally filters for media_type='movie'
// and then uses toDomainMovie().
