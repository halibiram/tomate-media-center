package com.halibiram.tomato.data.repository

import com.halibiram.tomato.core.common.Resource
import com.halibiram.tomato.core.database.dao.EpisodeDao
import com.halibiram.tomato.core.database.dao.SeriesDao
import com.halibiram.tomato.core.network.service.SeriesApiService
import com.halibiram.tomato.data.mapper.toDomain
import com.halibiram.tomato.data.mapper.toEntity
import com.halibiram.tomato.domain.model.Episode
import com.halibiram.tomato.domain.model.Series
import com.halibiram.tomato.domain.repository.SeriesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeriesRepositoryImpl @Inject constructor(
    private val seriesDao: SeriesDao,
    private val episodeDao: EpisodeDao,
    private val seriesApiService: SeriesApiService
) : SeriesRepository {

    private val CACHE_EXPIRY_HOURS = 6L
    private val API_KEY_PLACEHOLDER = "dummy_api_key"

    override fun getPopularSeries(page: Int, forceRefresh: Boolean): Flow<Resource<List<Series>>> = flow {
        val localSeriesFlow = seriesDao.getAllSeries().map { entities -> entities.map { it.toDomain() } }
        val currentLocalSeries = localSeriesFlow.firstOrNull() ?: emptyList()

        val firstSeriesEntity = if(currentLocalSeries.isNotEmpty()) seriesDao.getSeriesById(currentLocalSeries.first().id).firstOrNull() else null
        var needsRefresh = forceRefresh || isCacheExpired(firstSeriesEntity?.lastRefreshed) || currentLocalSeries.isEmpty()

        if (page > 1 && currentLocalSeries.isEmpty()) {
            needsRefresh = true
        }

        if (currentLocalSeries.isNotEmpty() && !needsRefresh && page == 1) {
            emit(Resource.Success(currentLocalSeries))
        } else if (needsRefresh) {
            if (currentLocalSeries.isEmpty() || page == 1) {
                 emit(Resource.Loading())
            } else {
                 emit(Resource.Loading(currentLocalSeries))
            }
            try {
                val response = seriesApiService.getPopularSeries(page, API_KEY_PLACEHOLDER)
                if (response.success && response.data != null) {
                    val seriesEntities = response.data.map { it.toEntity() }
                     if (page == 1) seriesDao.deleteAllSeries()
                    seriesDao.insertSeriesList(seriesEntities)
                    localSeriesFlow.collect{ updatedSeries -> emit(Resource.Success(updatedSeries)) }
                } else {
                    emit(Resource.Error(response.message ?: "Failed to fetch popular series", if(currentLocalSeries.isEmpty()) null else currentLocalSeries))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Network error", if(currentLocalSeries.isEmpty()) null else currentLocalSeries))
            }
        } else if (page > 1 && currentLocalSeries.isNotEmpty() && !needsRefresh) {
             localSeriesFlow.collect{ updatedSeries -> emit(Resource.Success(updatedSeries)) }
        }
    }

    override fun getSeriesById(seriesId: String, forceRefresh: Boolean): Flow<Resource<Series>> = flow {
        val localSeriesFlow = seriesDao.getSeriesById(seriesId).map { entity -> entity?.toDomain() }
        var localSeries = localSeriesFlow.firstOrNull()

        val seriesEntity = seriesDao.getSeriesById(seriesId).firstOrNull()
        val needsRefresh = forceRefresh || isCacheExpired(seriesEntity?.lastRefreshed) || localSeries == null

        if (localSeries != null && !needsRefresh) {
            emit(Resource.Success(localSeries))
            return@flow
        }

        if (localSeries == null) {
            emit(Resource.Loading())
        } else {
            emit(Resource.Loading(localSeries))
        }

        try {
            val response = seriesApiService.getSeriesDetails(seriesId, API_KEY_PLACEHOLDER)
            if (response.success && response.data != null) {
                seriesDao.insertSeries(response.data.toEntity())
                localSeriesFlow.collect { updatedSeries ->
                    if(updatedSeries != null) emit(Resource.Success(updatedSeries))
                }
            } else {
                emit(Resource.Error(response.message ?: "Failed to fetch series details", localSeries))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error", localSeries))
        }
    }

    override fun getEpisodesForSeries(seriesId: String, seasonNumber: Int, forceRefresh: Boolean): Flow<Resource<List<Episode>>> = flow {

        val localEpisodesFlow = episodeDao.getEpisodesForSeries(seriesId)
            .map{ entities ->
                entities.filter{ it.seasonNumber == seasonNumber }.map { it.toDomain() }
            }
        var localEpisodes = localEpisodesFlow.firstOrNull() ?: emptyList()

        val seriesEntity = seriesDao.getSeriesById(seriesId).firstOrNull()
        val needsRefresh = forceRefresh || isCacheExpired(seriesEntity?.lastRefreshed) || localEpisodes.isEmpty()

        if (localEpisodes.isNotEmpty() && !needsRefresh) {
             emit(Resource.Success(localEpisodes))
             return@flow
        }

        emit(Resource.Loading(if(localEpisodes.isEmpty()) null else localEpisodes))

        try {
            val response = seriesApiService.getEpisodesForSeason(seriesId, seasonNumber, API_KEY_PLACEHOLDER)
            if (response.success && response.data != null) {
                val episodeEntities = response.data.map { it.toEntity(seriesId) } // Pass seriesId for context
                episodeDao.insertEpisodes(episodeEntities)
                localEpisodesFlow.collect{ updatedEpisodes -> emit(Resource.Success(updatedEpisodes)) }
            } else {
                emit(Resource.Error(response.message ?: "Failed to fetch episodes", if(localEpisodes.isEmpty()) null else localEpisodes))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error", if(localEpisodes.isEmpty()) null else localEpisodes))
        }
    }

    override fun searchSeries(query: String, page: Int): Flow<Resource<List<Series>>> = flow {
        emit(Resource.Loading())
        try {
            val response = seriesApiService.searchSeries(query, page, API_KEY_PLACEHOLDER)
            if (response.success && response.data != null) {
                emit(Resource.Success(response.data.map { it.toDomain() }))
            } else {
                emit(Resource.Error(response.message ?: "Search failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error during search"))
        }
    }

    private fun isCacheExpired(lastRefreshed: Long?): Boolean {
        if (lastRefreshed == null) return true
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastRefreshed) > TimeUnit.HOURS.toMillis(CACHE_EXPIRY_HOURS)
    }
}
