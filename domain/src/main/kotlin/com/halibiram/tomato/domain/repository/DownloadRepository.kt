package com.halibiram.tomato.domain.repository

import com.halibiram.tomato.domain.model.Download
import com.halibiram.tomato.domain.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun getDownloads(): Flow<List<Download>>
    suspend fun addDownload(mediaId: String, mediaType: String, title: String, downloadUrl: String) // Simplified for now
    suspend fun pauseDownload(mediaId: String)
    suspend fun resumeDownload(mediaId: String)
    suspend fun cancelDownload(mediaId: String)
    suspend fun updateDownloadStatus(mediaId: String, status: DownloadStatus, progress: Int)
    fun getDownloadById(mediaId: String): Flow<Download?>
}
