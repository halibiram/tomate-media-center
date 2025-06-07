package com.halibiram.tomato.data.repository

import com.halibiram.tomato.core.database.dao.DownloadDao
import com.halibiram.tomato.core.database.entity.DownloadEntity
import com.halibiram.tomato.domain.model.Download // Domain model
import com.halibiram.tomato.domain.model.DownloadMediaType // Domain enum
import com.halibiram.tomato.domain.model.DownloadStatus // Domain enum
import com.halibiram.tomato.domain.repository.DownloadRepository // Domain interface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File // For deleting file
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    private val downloadDao: DownloadDao
    // private val context: android.content.Context // For file deletion, if needed here
) : DownloadRepository {

    override suspend fun addDownload(download: Download) {
        downloadDao.insertDownload(mapDomainToEntity(download))
    }

    // Renamed from updateDownloadProgressAndStatus to match DAO more closely if preferred,
    // or keep the domain-facing name and map parameters. Let's use a more descriptive name.
    override suspend fun updateDownloadState(
        id: String,
        progress: Int,
        status: DownloadStatus,
        downloadedSizeBytes: Long?,
        totalSizeBytes: Long?,
        filePath: String?,
        completedTimestamp: Long?
    ) {
        downloadDao.updateDownloadState(
            id = id,
            progress = progress,
            status = status.name, // Convert enum to string for DB
            downloadedSizeBytes = downloadedSizeBytes,
            totalSizeBytes = totalSizeBytes,
            filePath = filePath,
            completedTimestamp = if (status == DownloadStatus.COMPLETED) System.currentTimeMillis() else null
        )
    }

    override suspend fun updateDownloadStatus(id: String, status: DownloadStatus) {
        downloadDao.updateDownloadStatus(id, status.name)
    }


    override fun getDownloads(): Flow<List<Download>> {
        return downloadDao.getAllDownloads().map { entities ->
            entities.map { mapEntityToDomain(it) }
        }
    }

    override fun getActiveDownloadsFlow(): Flow<List<Download>> {
        return downloadDao.getActiveDownloads().map { entities ->
            entities.map { mapEntityToDomain(it) }
        }
    }

    override suspend fun getDownload(id: String): Download? {
        return downloadDao.getDownloadById(id)?.let { mapEntityToDomain(it) }
    }

    override fun getDownloadFlow(id: String): Flow<Download?> {
        return downloadDao.getDownloadFlowById(id).map { entity ->
            entity?.let { mapEntityToDomain(it) }
        }
    }

    override suspend fun removeDownload(id: String): Boolean {
        val downloadEntity = downloadDao.getDownloadById(id)
        downloadEntity?.filePath?.let { path ->
            try {
                File(path).delete()
            } catch (e: Exception) {
                // Log file deletion error, but proceed to delete DB record
                // Log.e("DownloadRepository", "Error deleting file $path: ${e.message}")
            }
        }
        downloadDao.deleteDownload(id)
        return true // Assume success for now, or check rowsAffected from DAO
    }

    override suspend fun clearAllDownloads() {
        // This would require fetching all to delete files, which is heavy.
        // A better approach for "clear all" might be to mark as "TO_BE_DELETED"
        // and have a separate cleanup service, or only delete DB records.
        // For now, just clear DB. File cleanup needs careful consideration.
        downloadDao.clearDownloads()
    }

    // --- Mappers ---
    private fun mapEntityToDomain(entity: DownloadEntity): Download {
        return Download(
            id = entity.id,
            mediaId = entity.mediaId,
            mediaType = try { DownloadMediaType.valueOf(entity.mediaType) } catch (e: IllegalArgumentException) { DownloadMediaType.OTHER },
            title = entity.title,
            downloadUrl = entity.downloadUrl, // May not be needed in domain model long-term if only for worker
            status = try { DownloadStatus.valueOf(entity.status) } catch (e: IllegalArgumentException) { DownloadStatus.FAILED },
            progress = entity.progress,
            filePath = entity.filePath,
            totalSizeBytes = entity.totalSizeBytes ?: 0L,
            downloadedSizeBytes = entity.downloadedSizeBytes ?: 0L,
            addedDate = entity.addedDateTimestamp,
            posterPath = entity.posterPath
            // Map other fields like seriesIdForEpisode if they exist in domain Download model
        )
    }

    private fun mapDomainToEntity(domain: Download): DownloadEntity {
        return DownloadEntity(
            id = domain.id,
            mediaId = domain.mediaId,
            mediaType = domain.mediaType.name,
            title = domain.title,
            downloadUrl = domain.downloadUrl,
            filePath = domain.filePath,
            status = domain.status.name,
            progress = domain.progress,
            totalSizeBytes = domain.totalSizeBytes,
            downloadedSizeBytes = domain.downloadedSizeBytes,
            addedDateTimestamp = domain.addedDate,
            posterPath = domain.posterPath,
            completedDateTimestamp = if (domain.status == DownloadStatus.COMPLETED) System.currentTimeMillis() else null
            // Map other fields
        )
    }
}
