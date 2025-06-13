package com.halibiram.tomato.core.database.dao

import androidx.room.*
import com.halibiram.tomato.core.database.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :downloadId")
    fun getDownloadById(downloadId: String): Flow<DownloadEntity?>

    @Query("DELETE FROM downloads WHERE id = :downloadId")
    suspend fun deleteDownload(downloadId: String)

    @Query("SELECT * FROM downloads WHERE downloadStatus = :status")
    fun getDownloadsByStatus(status: String): Flow<List<DownloadEntity>>
}
