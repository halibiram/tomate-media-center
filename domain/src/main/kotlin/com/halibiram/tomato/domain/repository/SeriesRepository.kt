package com.halibiram.tomato.domain.repository

import com.halibiram.tomato.domain.model.Series
import com.halibiram.tomato.domain.model.Episode
import com.halibiram.tomato.core.common.Resource
import kotlinx.coroutines.flow.Flow

interface SeriesRepository {
    fun getPopularSeries(page: Int, forceRefresh: Boolean = false): Flow<Resource<List<Series>>>
    fun getSeriesById(seriesId: String, forceRefresh: Boolean = false): Flow<Resource<Series>>
    fun getEpisodesForSeries(seriesId: String, seasonNumber: Int, forceRefresh: Boolean = false): Flow<Resource<List<Episode>>>
    fun searchSeries(query: String, page: Int): Flow<Resource<List<Series>>>
}
