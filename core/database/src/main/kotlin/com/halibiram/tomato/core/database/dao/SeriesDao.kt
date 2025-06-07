package com.halibiram.tomato.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    @Query("SELECT * FROM series WHERE id = :seriesId")
    fun getSeriesById(seriesId: String): Flow<SeriesEntity?>

    @Query("SELECT * FROM series ORDER BY firstAirDate DESC")
    fun getAllSeries(): Flow<List<SeriesEntity>>

    @Query("DELETE FROM series WHERE id = :seriesId")
    suspend fun deleteSeriesById(seriesId: String)

    @Query("DELETE FROM series")
    suspend fun deleteAllSeries()

    // Episodes related to a series
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: EpisodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)

    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY seasonNumber, episodeNumber ASC")
    fun getEpisodesForSeries(seriesId: String): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId AND seasonNumber = :seasonNumber ORDER BY episodeNumber ASC")
    fun getEpisodesForSeason(seriesId: String, seasonNumber: Int): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE id = :episodeId")
    fun getEpisodeById(episodeId: String): Flow<EpisodeEntity?>

    @Query("DELETE FROM episodes WHERE seriesId = :seriesId")
    suspend fun deleteEpisodesForSeries(seriesId: String)

    // Example of a @Transaction method (though not strictly required for simple series+episodes)
    // data class SeriesWithEpisodes(
    // @Embedded val series: SeriesEntity,
    // @Relation(
    // parentColumn = "id",
    // entityColumn = "seriesId"
    // )
    // val episodes: List<EpisodeEntity>
    // )
    //
    // @Transaction
    // @Query("SELECT * FROM series WHERE id = :seriesId")
    // fun getSeriesWithEpisodes(seriesId: String): Flow<SeriesWithEpisodes?>
}
