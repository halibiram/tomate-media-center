package com.halibiram.tomato.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.halibiram.tomato.core.database.entity.EpisodeEntity
import com.halibiram.tomato.core.database.entity.SeriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeriesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: SeriesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManySeries(seriesList: List<SeriesEntity>)

    @Update
    suspend fun updateSeries(series: SeriesEntity)

    @Query("SELECT * FROM series WHERE id = :id")
    suspend fun getSeriesById(id: String): SeriesEntity? // Changed to suspend fun

    @Query("SELECT * FROM series ORDER BY popularity DESC, title ASC") // Example order
    fun getPopularSeries(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE title LIKE '%' || :query || '%' ORDER BY firstAirDate DESC")
    fun searchSeries(query: String): Flow<List<SeriesEntity>>

    @Delete
    suspend fun deleteSeries(series: SeriesEntity) // Added general delete

    @Query("DELETE FROM series WHERE id = :id")
    suspend fun deleteSeriesById(id: String)

    @Query("DELETE FROM series")
    suspend fun clearSeries() // Renamed from deleteAllSeries

    // --- Episode specific methods within SeriesDao ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: EpisodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)

    @Update
    suspend fun updateEpisode(episode: EpisodeEntity) // Added update for episode

    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY seasonNumber ASC, episodeNumber ASC")
    fun getEpisodesForSeries(seriesId: String): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId AND seasonNumber = :seasonNumber ORDER BY episodeNumber ASC")
    fun getEpisodesForSeason(seriesId: String, seasonNumber: Int): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE id = :episodeId")
    suspend fun getEpisodeById(episodeId: String): EpisodeEntity? // Changed to suspend fun

    @Query("DELETE FROM episodes WHERE id = :episodeId")
    suspend fun deleteEpisodeById(episodeId: String) // Added specific delete

    @Query("DELETE FROM episodes WHERE seriesId = :seriesId")
    suspend fun deleteEpisodesForSeries(seriesId: String)

    @Query("DELETE FROM episodes WHERE seriesId = :seriesId AND seasonNumber = :seasonNumber")
    suspend fun deleteEpisodesForSeason(seriesId: String, seasonNumber: Int) // Added specific delete
}
