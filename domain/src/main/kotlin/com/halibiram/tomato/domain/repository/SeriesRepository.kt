package com.halibiram.tomato.domain.repository

import com.halibiram.tomato.domain.model.Episode
import com.halibiram.tomato.domain.model.Series
import kotlinx.coroutines.flow.Flow

interface SeriesRepository {
    fun getPopularSeries(page: Int): Flow<List<Series>>
    fun getSeriesDetails(seriesId: String): Flow<Series?>
    fun getEpisodesForSeries(seriesId: String, seasonNumber: Int): Flow<List<Episode>>
    // Add other series related repository methods
}
