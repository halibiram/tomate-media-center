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
    suspend fun updateDownload(download: DownloadEntity) // General update

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownloadById(id: String): DownloadEntity? // Changed to suspend fun, query by 'id' (PK of DownloadEntity)

    @Query("SELECT * FROM downloads WHERE mediaId = :mediaId")
    fun getDownloadsByMediaId(mediaId: String): Flow<List<DownloadEntity>> // To observe all download tasks for a specific media

    @Query("SELECT * FROM downloads ORDER BY addedDate DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY addedDate DESC")
    fun getDownloadsByStatus(status: String): Flow<List<DownloadEntity>>

    // Method to get active (PENDING or DOWNLOADING) downloads
    @Query("SELECT * FROM downloads WHERE status = :pendingStatus OR status = :downloadingStatus ORDER BY addedDate ASC")
    fun getActiveDownloads(pendingStatus: String = DownloadEntity.STATUS_PENDING, downloadingStatus: String = DownloadEntity.STATUS_DOWNLOADING): Flow<List<DownloadEntity>>

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: String)

    @Query("DELETE FROM downloads")
    suspend fun clearDownloads() // Renamed from deleteAllDownloads

    // Specific updates
    @Query("UPDATE downloads SET progress = :progress, downloadedSizeBytes = :downloadedBytes, status = :status WHERE id = :id")
    suspend fun updateDownloadProgressAndStatus(id: String, progress: Int, downloadedSizeBytes: Long, status: String)

    @Query("UPDATE downloads SET status = :newStatus WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, newStatus: String)

    @Query("UPDATE downloads SET filePath = :filePath, status = :status, completedDate = :completedTimestamp WHERE id = :id")
    suspend fun markAsCompleted(id: String, filePath: String, status: String = DownloadEntity.STATUS_COMPLETED, completedTimestamp: Long)

    @Query("UPDATE downloads SET totalSizeBytes = :totalSizeBytes WHERE id = :id")
    suspend fun updateTotalSizeBytes(id: String, totalSizeBytes: Long)
}
