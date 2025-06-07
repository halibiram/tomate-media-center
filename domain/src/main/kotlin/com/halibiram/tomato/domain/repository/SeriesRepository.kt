package com.halibiram.tomato.domain.repository

import kotlinx.coroutines.flow.Flow

// Placeholder data models
data class Series(val id: String, val name: String, val posterUrl: String?)
data class SeriesDetails(val id: String, val name: String, val overview: String, val posterUrl: String?, val backdropUrl: String?, val firstAirDate: String?, val genres: List<String>?)
data class Episode(val id: String, val seriesId: String, val seasonNumber: Int, val episodeNumber: Int, val name: String, val overview: String?)

interface SeriesRepository {
    fun getPopularSeries(page: Int): Flow<List<Series>>
    fun getSeriesDetails(seriesId: String): Flow<SeriesDetails?>
    fun getEpisodesForSeason(seriesId: String, seasonNumber: Int): Flow<List<Episode>>
    fun searchSeries(query: String, page: Int): Flow<List<Series>>
    // Add other series-related methods
}
