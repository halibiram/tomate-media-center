package com.halibiram.tomato.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.halibiram.tomato.core.database.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Query("SELECT * FROM downloads WHERE mediaId = :mediaId")
    fun getDownloadById(mediaId: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads ORDER BY addedDate DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE downloadStatus = :status ORDER BY addedDate DESC")
    fun getDownloadsByStatus(status: String): Flow<List<DownloadEntity>>

    @Query("DELETE FROM downloads WHERE mediaId = :mediaId")
    suspend fun deleteDownloadById(mediaId: String)

    @Query("DELETE FROM downloads")
    suspend fun deleteAllDownloads()

    @Query("UPDATE downloads SET downloadStatus = :newStatus, downloadedSizeBytes = :downloadedBytes, progressPercentage = :progress WHERE mediaId = :mediaId")
    suspend fun updateDownloadProgress(mediaId: String, newStatus: String, downloadedBytes: Long, progress: Int)

    @Query("UPDATE downloads SET downloadStatus = :newStatus WHERE mediaId = :mediaId")
    suspend fun updateDownloadStatus(mediaId: String, newStatus: String)
}
