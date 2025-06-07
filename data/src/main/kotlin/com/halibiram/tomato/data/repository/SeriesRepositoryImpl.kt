package com.halibiram.tomato.data.repository

import com.halibiram.tomato.core.database.dao.SeriesDao // Placeholder
// import com.halibiram.tomato.core.network.service.SeriesApiService // Placeholder
import com.halibiram.tomato.domain.repository.Episode
import com.halibiram.tomato.domain.repository.Series
import com.halibiram.tomato.domain.repository.SeriesDetails
import com.halibiram.tomato.domain.repository.SeriesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeriesRepositoryImpl @Inject constructor(
    // private val seriesApiService: SeriesApiService,
    private val seriesDao: SeriesDao? // Nullable for placeholder
) : SeriesRepository {

    override fun getPopularSeries(page: Int): Flow<List<Series>> = flow {
        // Placeholder: Fetch from API, map, cache, emit from cache
        emit(emptyList<Series>())
    }

    override fun getSeriesDetails(seriesId: String): Flow<SeriesDetails?> = flow {
        // Placeholder: Fetch from cache, if not found/stale, fetch from API, map, cache, emit
        emit(null)
    }

    override fun getEpisodesForSeason(seriesId: String, seasonNumber: Int): Flow<List<Episode>> = flow {
        // Placeholder: Fetch from cache (e.g., seriesDao.getEpisodes(seriesId, seasonNumber))
        // Or fetch from API if not available in cache
        emit(emptyList<Episode>())
    }

    override fun searchSeries(query: String, page: Int): Flow<List<Series>> = flow {
        // Placeholder: Fetch from API, map, emit
        emit(emptyList<Series>())
    }
}
