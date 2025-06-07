package com.halibiram.tomato.domain.usecase.download

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.halibiram.tomato.domain.model.Download
import com.halibiram.tomato.domain.model.DownloadMediaType
import com.halibiram.tomato.domain.model.DownloadStatus
import com.halibiram.tomato.domain.repository.DownloadRepository
import com.halibiram.tomato.feature.downloads.worker.DownloadWorker
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class DownloadMediaUseCaseTest {

    private lateinit var context: Context
    private lateinit var downloadRepository: DownloadRepository
    private lateinit var workManager: WorkManager
    private lateinit var downloadMediaUseCase: DownloadMediaUseCase

    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        downloadRepository = mockk()
        workManager = mockk(relaxed = true)

        // Mock WorkManager.getInstance(context)
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(context) } returns workManager

        downloadMediaUseCase = DownloadMediaUseCase(context, downloadRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(WorkManager::class)
    }

    @Test
    fun `invoke with new download adds to repository and enqueues worker`() = runTest {
        val mediaId = "movie1"
        val mediaType = DownloadMediaType.MOVIE
        val mediaUrl = "http://example.com/movie.mp4"
        val title = "Test Movie"
        val posterPath = "/poster.jpg"

        // Simulate no existing download
        coEvery { downloadRepository.getDownloads().first() } returns emptyList()
        coJustRun { downloadRepository.addDownload(any()) }
        every { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) } returns mockk()

        val downloadTaskId = downloadMediaUseCase.invoke(mediaId, mediaType, mediaUrl, title, posterPath)

        assertNotNull(downloadTaskId)
        coVerify { downloadRepository.addDownload(match {
            it.id == downloadTaskId &&
            it.mediaId == mediaId &&
            it.status == DownloadStatus.PENDING
        })}
        val slot = slot<OneTimeWorkRequest>()
        verify { workManager.enqueueUniqueWork(eq(downloadTaskId!!), any(), capture(slot)) }

        val workData = slot.captured.workSpec.input
        assertEquals(downloadTaskId, workData.getString(DownloadWorker.KEY_DOWNLOAD_ID))
        assertEquals(mediaUrl, workData.getString(DownloadWorker.KEY_MEDIA_URL))
        assertEquals(title, workData.getString(DownloadWorker.KEY_MEDIA_TITLE))
    }

    @Test
    fun `invoke with existing active download returns null and does not enqueue`() = runTest {
        val mediaId = "movie1"
        val existingDownload = Download(
            id = "existingTask1", mediaId = mediaId, mediaType = DownloadMediaType.MOVIE, title = "Existing",
            downloadUrl = "url", status = DownloadStatus.DOWNLOADING, progress = 50, filePath = null, addedDate = 0L
        )
        coEvery { downloadRepository.getDownloads().first() } returns listOf(existingDownload)

        val downloadTaskId = downloadMediaUseCase.invoke(mediaId, DownloadMediaType.MOVIE, "new_url", "New Title", null)

        assertNull(downloadTaskId)
        coVerify(exactly = 0) { downloadRepository.addDownload(any()) }
        verify(exactly = 0) { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `invoke with existing completed download returns null and does not enqueue`() = runTest {
        val mediaId = "movie1"
        val existingDownload = Download(
            id = "existingTask2", mediaId = mediaId, mediaType = DownloadMediaType.MOVIE, title = "Existing Completed",
            downloadUrl = "url", status = DownloadStatus.COMPLETED, progress = 100, filePath = "/file", addedDate = 0L
        )
        coEvery { downloadRepository.getDownloads().first() } returns listOf(existingDownload)

        val downloadTaskId = downloadMediaUseCase.invoke(mediaId, DownloadMediaType.MOVIE, "new_url", "New Title", null)

        assertNull(downloadTaskId)
    }
}
