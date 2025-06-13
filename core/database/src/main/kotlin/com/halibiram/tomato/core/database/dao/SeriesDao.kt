package com.halibiram.tomato.core.database.dao

import androidx.room.*
import com.halibiram.tomato.core.database.entity.SeriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeriesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesList(seriesList: List<SeriesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: SeriesEntity)

    @Query("SELECT * FROM series")
    fun getAllSeries(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE id = :seriesId")
    fun getSeriesById(seriesId: String): Flow<SeriesEntity?>

    @Query("DELETE FROM series WHERE id = :seriesId")
    suspend fun deleteSeriesById(seriesId: String)

    @Query("DELETE FROM series")
    suspend fun deleteAllSeries()

    @Query("SELECT * FROM series WHERE name LIKE :query")
    fun searchSeries(query: String): Flow<List<SeriesEntity>>
}
