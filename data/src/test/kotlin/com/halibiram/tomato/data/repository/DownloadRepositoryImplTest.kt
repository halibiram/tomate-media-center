package com.halibiram.tomato.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.halibiram.tomato.core.database.dao.DownloadDao
import com.halibiram.tomato.core.database.entity.DownloadEntity
import com.halibiram.tomato.domain.model.Download
import com.halibiram.tomato.domain.model.DownloadMediaType
import com.halibiram.tomato.domain.model.DownloadStatus
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [30])
class DownloadRepositoryImplTest {

    private lateinit var mockDownloadDao: DownloadDao
    private lateinit var downloadRepository: DownloadRepositoryImpl
    private lateinit var context: Context


    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext() // For file operations
        mockDownloadDao = mockk(relaxed = true)
        downloadRepository = DownloadRepositoryImpl(mockDownloadDao)

        // Ensure downloads directory exists for file deletion tests
        File(context.filesDir, "downloads_repo_test").mkdirs()
    }

    private fun createSampleDomainDownload(id: String, status: DownloadStatus, filePath: String? = null) = Download(
        id = id, mediaId = "media-$id", mediaType = DownloadMediaType.MOVIE, title = "Title $id",
        downloadUrl = "http://example.com/$id.mp4", status = status, progress = if(status == DownloadStatus.COMPLETED) 100 else 50,
        filePath = filePath, totalSizeBytes = 1000L, downloadedSizeBytes = if(status == DownloadStatus.COMPLETED) 1000L else 500L,
        addedDate = System.currentTimeMillis(), posterPath = null
    )

    private fun createSampleEntityDownload(id: String, status: String, filePath: String? = null) = DownloadEntity(
        id = id, mediaId = "media-$id", mediaType = DownloadMediaType.MOVIE.name, title = "Title $id",
        downloadUrl = "http://example.com/$id.mp4", status = status, progress = if(status == DownloadEntity.STATUS_COMPLETED) 100 else 50,
        filePath = filePath, totalSizeBytes = 1000L, downloadedSizeBytes = if(status == DownloadEntity.STATUS_COMPLETED) 1000L else 500L,
        addedDateTimestamp = System.currentTimeMillis(), posterPath = null
    )

    @Test
    fun `addDownload maps domain to entity and calls DAO insert`() = runTest {
        val domainDownload = createSampleDomainDownload("d1", DownloadStatus.PENDING)
        coJustRun { mockDownloadDao.insertDownload(any()) }

        downloadRepository.addDownload(domainDownload)

        val entitySlot = slot<DownloadEntity>()
        coVerify { mockDownloadDao.insertDownload(capture(entitySlot)) }
        assertEquals(domainDownload.id, entitySlot.captured.id)
        assertEquals(domainDownload.status.name, entitySlot.captured.status)
    }

    @Test
    fun `updateDownloadState calls DAO updateDownloadState`() = runTest {
        coJustRun { mockDownloadDao.updateDownloadState(any(), any(), any(), any(), any(), any(), any()) }
        val id = "d1"
        val progress = 75
        val status = DownloadStatus.DOWNLOADING
        val downloadedBytes = 750L
        val totalBytes = 1000L
        val filePath = null
        val completedTimestamp = null


        downloadRepository.updateDownloadState(id, progress, status, downloadedBytes, totalBytes, filePath, completedTimestamp)

        coVerify { mockDownloadDao.updateDownloadState(id, progress, status.name, downloadedBytes,totalBytes, filePath, completedTimestamp) }
    }

    @Test
    fun `updateDownloadStatus calls DAO updateDownloadStatus`() = runTest {
        val id = "d1"
        val status = DownloadStatus.PAUSED
        coJustRun { mockDownloadDao.updateDownloadStatus(id, status.name) }

        downloadRepository.updateDownloadStatus(id, status)

        coVerify { mockDownloadDao.updateDownloadStatus(id, status.name) }
    }


    @Test
    fun `getDownloads maps entities from DAO to domain models`() = runTest {
        val entity1 = createSampleEntityDownload("e1", DownloadEntity.STATUS_DOWNLOADING)
        val entity2 = createSampleEntityDownload("e2", DownloadEntity.STATUS_COMPLETED, "/path/e2.mp4")
        every { mockDownloadDao.getAllDownloads() } returns flowOf(listOf(entity1, entity2))

        val result = downloadRepository.getDownloads().first()

        assertEquals(2, result.size)
        assertEquals(entity1.id, result[0].id)
        assertEquals(DownloadStatus.DOWNLOADING, result[0].status)
        assertEquals(entity2.id, result[1].id)
        assertEquals(DownloadStatus.COMPLETED, result[1].status)
        assertEquals(entity2.filePath, result[1].filePath)
    }

    @Test
    fun `getDownload maps entity from DAO to domain model`() = runTest {
        val entity = createSampleEntityDownload("e1", DownloadEntity.STATUS_PAUSED)
        coEvery { mockDownloadDao.getDownloadById("e1") } returns entity

        val result = downloadRepository.getDownload("e1")

        assertNotNull(result)
        assertEquals(entity.id, result!!.id)
        assertEquals(DownloadStatus.PAUSED, result.status)
    }

     @Test
    fun `getDownloadFlow maps entity from DAO to domain model`() = runTest {
        val entity = createSampleEntityDownload("e1", DownloadEntity.STATUS_FAILED)
        every { mockDownloadDao.getDownloadFlowById("e1") } returns flowOf(entity)

        val result = downloadRepository.getDownloadFlow("e1").first()

        assertNotNull(result)
        assertEquals(entity.id, result!!.id)
        assertEquals(DownloadStatus.FAILED, result.status)
    }


    @Test
    fun `removeDownload deletes file and calls DAO delete`() = runTest {
        val downloadId = "del1"
        // Create a dummy file to test deletion
        val tempDir = File(context.filesDir, "downloads_repo_test")
        val dummyFile = File(tempDir, "$downloadId.mp4")
        dummyFile.createNewFile()
        assertTrue(dummyFile.exists())

        val entity = createSampleEntityDownload(downloadId, DownloadEntity.STATUS_COMPLETED, dummyFile.absolutePath)
        coEvery { mockDownloadDao.getDownloadById(downloadId) } returns entity
        coJustRun { mockDownloadDao.deleteDownload(downloadId) }

        val success = downloadRepository.removeDownload(downloadId)

        assertTrue(success)
        assertFalse("File should be deleted", dummyFile.exists()) // Verify file deletion
        coVerify { mockDownloadDao.deleteDownload(downloadId) }

        tempDir.deleteRecursively() // Clean up test directory
    }

    @Test
    fun `removeDownload no file path just calls DAO delete`() = runTest {
        val downloadId = "del2"
        val entity = createSampleEntityDownload(downloadId, DownloadEntity.STATUS_FAILED, filePath = null)
        coEvery { mockDownloadDao.getDownloadById(downloadId) } returns entity
        coJustRun { mockDownloadDao.deleteDownload(downloadId) }

        val success = downloadRepository.removeDownload(downloadId)

        assertTrue(success)
        coVerify { mockDownloadDao.deleteDownload(downloadId) }
    }
}
