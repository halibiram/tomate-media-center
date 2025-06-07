package com.halibiram.tomato.data.repository

import com.halibiram.tomato.core.database.dao.DownloadDao // Placeholder
import com.halibiram.tomato.core.database.entity.DownloadEntity // For mapping
import com.halibiram.tomato.domain.repository.DownloadItem
import com.halibiram.tomato.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    private val downloadDao: DownloadDao? // Nullable for placeholder
    // May also need WorkManager or a download service if not directly handled by DAO/Worker observation
) : DownloadRepository {

    override fun getDownloadsByStatusFlow(statusFilter: String): Flow<List<DownloadItem>> {
        return downloadDao?.getDownloadsByStatus(statusFilter)?.map { entities ->
            entities.map { entity -> mapToDomain(entity) }
        } ?: flow { emit(emptyList()) } // Emit empty if DAO is null
    }

    override suspend fun getDownloadById(mediaId: String): DownloadItem? {
        return downloadDao?.getDownloadById(mediaId)?.let { entityFlow ->
            // This is tricky if DAO returns Flow. Assuming a suspend fun getDownload(id) in DAO for simplicity.
            // If getDownloadById in DAO returns Flow, this method should also return Flow.
            // For now, let's assume a hypothetical suspend function in DAO or direct entity access for simplicity.
            // val entity = entityFlow.firstOrNull() // Not ideal for suspend fun
            // For placeholder, let's assume DAO has a suspend fun.
            // For a more realistic scenario with Flow from DAO:
            // return downloadDao?.getDownloadById(mediaId)?.firstOrNull()?.let { mapToDomain(it) }
             null // Placeholder due to DAO returning Flow
        }
    }


    override suspend fun addDownload(item: DownloadItem) {
        // downloadDao?.insertDownload(mapToEntity(item))
        // This would typically also trigger a DownloadWorker
    }

    override suspend fun updateDownloadStatus(mediaId: String, newStatus: String, downloadedBytes: Long?, progress: Int?) {
        // downloadDao?.updateDownloadStatus(mediaId, newStatus)
        // if (downloadedBytes != null && progress != null) {
        //    downloadDao?.updateDownloadProgress(mediaId, newStatus, downloadedBytes, progress)
        // }
    }
     override suspend fun updateDownloadSize(mediaId: String, totalSizeBytes: Long) {
        // This might be part of DownloadEntity update logic
        // downloadDao?.updateTotalSize(mediaId, totalSizeBytes) // Hypothetical DAO method
    }


    override suspend fun deleteDownload(mediaId: String): Boolean {
        // downloadDao?.deleteDownloadById(mediaId)
        // Also delete file from storage
        return true
    }

    override suspend fun clearAllDownloads() {
        // downloadDao?.deleteAllDownloads()
        // Also delete all files from storage
    }

    // --- Mappers ---
    private fun mapToDomain(entity: DownloadEntity): DownloadItem {
        return DownloadItem(
            mediaId = entity.mediaId,
            title = entity.title,
            posterPath = entity.posterPath,
            downloadSizeBytes = entity.downloadSizeBytes,
            downloadedSizeBytes = entity.downloadedSizeBytes,
            progressPercentage = entity.progressPercentage,
            downloadStatus = entity.downloadStatus,
            addedDate = entity.addedDate,
            mediaType = entity.mediaType,
            filePath = entity.downloadPath // Assuming downloadPath is the local file path
        )
    }

    private fun mapToEntity(domain: DownloadItem): DownloadEntity {
         return DownloadEntity(
            mediaId = domain.mediaId,
            title = domain.title,
            posterPath = domain.posterPath,
            downloadSizeBytes = domain.downloadSizeBytes,
            downloadedSizeBytes = domain.downloadedSizeBytes,
            progressPercentage = domain.progressPercentage,
            downloadStatus = domain.downloadStatus,
            addedDate = domain.addedDate,
            mediaType = domain.mediaType,
            downloadPath = domain.filePath ?: "", // downloadPath should be set when download starts
            // Other fields like seriesId, seasonNumber, episodeNumber if applicable
            seriesId = null,
            seasonNumber = null,
            episodeNumber = null
        )
    }
}
