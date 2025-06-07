package com.halibiram.tomato.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.halibiram.tomato.core.database.entity.EpisodeEntity
import com.halibiram.tomato.core.database.entity.SeriesEntity
import kotlinx.coroutines.flow.Flow

// SeriesDao
@Dao
interface SeriesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: SeriesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManySeries(seriesList: List<SeriesEntity>)

    @Query("SELECT * FROM series WHERE id = :seriesId")
    fun getSeriesById(seriesId: String): Flow<SeriesEntity?>

    @Query("SELECT * FROM series")
    fun getAllSeries(): Flow<List<SeriesEntity>>

    @Query("DELETE FROM series WHERE id = :seriesId")
    suspend fun deleteSeriesById(seriesId: String)

    // Episodes specific to a series
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: EpisodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)

    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY seasonNumber, episodeNumber")
    fun getEpisodesForSeries(seriesId: String): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE id = :episodeId")
    fun getEpisodeById(episodeId: String): Flow<EpisodeEntity?>

    @Transaction
    suspend fun insertSeriesWithEpisodes(series: SeriesEntity, episodes: List<EpisodeEntity>) {
        insertSeries(series)
        insertEpisodes(episodes)
    }
}
