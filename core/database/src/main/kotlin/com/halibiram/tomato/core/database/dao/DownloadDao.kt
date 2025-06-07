package com.halibiram.tomato.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.halibiram.tomato.core.database.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

// DownloadDao
@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Query("SELECT * FROM downloads WHERE mediaId = :mediaId")
    fun getDownloadById(mediaId: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads ORDER BY title")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("DELETE FROM downloads WHERE mediaId = :mediaId")
    suspend fun deleteDownload(mediaId: String)
}
