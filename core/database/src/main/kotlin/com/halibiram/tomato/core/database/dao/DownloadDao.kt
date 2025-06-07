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

    // Specific update for progress, status, downloaded size, and optionally file path on completion
    @Query("UPDATE downloads SET progress = :progress, status = :status, downloadedSizeBytes = :downloadedSizeBytes, filePath = :filePath, totalSizeBytes = CASE WHEN :totalSizeBytes IS NOT NULL THEN :totalSizeBytes ELSE totalSizeBytes END, completedDateTimestamp = CASE WHEN :status = 'COMPLETED' THEN :completedTimestamp ELSE completedDateTimestamp END WHERE id = :id")
    suspend fun updateDownloadState(
        id: String,
        progress: Int,
        status: String,
        downloadedSizeBytes: Long?,
        totalSizeBytes: Long?, // To update if it becomes known during download
        filePath: String?, // Nullable, set on completion
        completedTimestamp: Long? // Set when status is COMPLETED
    )

    @Query("UPDATE downloads SET status = :status WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, status: String)


    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownloadById(id: String): DownloadEntity?

    // To observe a specific download task by its unique ID (if different from mediaId)
    @Query("SELECT * FROM downloads WHERE id = :id")
    fun getDownloadFlowById(id: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads ORDER BY addedDateTimestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY addedDateTimestamp DESC")
    fun getDownloadsByStatus(status: String): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'PENDING' OR status = 'DOWNLOADING' ORDER BY addedDateTimestamp ASC")
    fun getActiveDownloads(): Flow<List<DownloadEntity>>

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownload(id: String) // Changed parameter name for clarity

    @Query("DELETE FROM downloads")
    suspend fun clearDownloads()
}
