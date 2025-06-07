package com.halibiram.tomato.domain.usecase.download

import android.content.Context
import androidx.work.WorkManager
import com.halibiram.tomato.domain.model.Download
import com.halibiram.tomato.domain.model.DownloadMediaType
import com.halibiram.tomato.domain.model.DownloadStatus
import com.halibiram.tomato.domain.repository.DownloadRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class PauseDownloadUseCaseTest {

    private lateinit var context: Context
    private lateinit var downloadRepository: DownloadRepository
    private lateinit var workManager: WorkManager
    private lateinit var pauseDownloadUseCase: PauseDownloadUseCase

    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        downloadRepository = mockk()
        workManager = mockk(relaxed = true)

        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(context) } returns workManager

        pauseDownloadUseCase = PauseDownloadUseCase(context, downloadRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(WorkManager::class)
    }

    @Test
    fun `invoke with downloading item cancels work and updates status to PAUSED`() = runTest {
        val downloadId = "task1"
        val downloadItem = Download(
            id = downloadId, mediaId = "m1", mediaType = DownloadMediaType.MOVIE, title = "Test",
            downloadUrl = "url", status = DownloadStatus.DOWNLOADING, progress = 50, filePath = null, addedDate = 0L
        )
        coEvery { downloadRepository.getDownload(downloadId) } returns downloadItem
        every { workManager.cancelUniqueWork(downloadId) } returns mockk() // Mock Operation
        coJustRun { downloadRepository.updateDownloadStatus(downloadId, DownloadStatus.PAUSED) }

        val result = pauseDownloadUseCase.invoke(downloadId)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { workManager.cancelUniqueWork(downloadId) }
        coVerify(exactly = 1) { downloadRepository.updateDownloadStatus(downloadId, DownloadStatus.PAUSED) }
    }

    @Test
    fun `invoke with pending item cancels work and updates status to PAUSED`() = runTest {
        val downloadId = "task_pending"
        val downloadItem = Download(
            id = downloadId, mediaId = "m_pending", mediaType = DownloadMediaType.MOVIE, title = "Test Pending",
            downloadUrl = "url_pending", status = DownloadStatus.PENDING, progress = 0, filePath = null, addedDate = 0L
        )
        coEvery { downloadRepository.getDownload(downloadId) } returns downloadItem
        every { workManager.cancelUniqueWork(downloadId) } returns mockk()
        coJustRun { downloadRepository.updateDownloadStatus(downloadId, DownloadStatus.PAUSED) }

        val result = pauseDownloadUseCase.invoke(downloadId)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { workManager.cancelUniqueWork(downloadId) }
        coVerify(exactly = 1) { downloadRepository.updateDownloadStatus(downloadId, DownloadStatus.PAUSED) }
    }


    @Test
    fun `invoke with item not in pausable state returns failure`() = runTest {
        val downloadId = "task2"
        val downloadItem = Download(
            id = downloadId, mediaId = "m2", mediaType = DownloadMediaType.MOVIE, title = "Test",
            downloadUrl = "url", status = DownloadStatus.COMPLETED, progress = 100, filePath = "/file", addedDate = 0L
        )
        coEvery { downloadRepository.getDownload(downloadId) } returns downloadItem

        val result = pauseDownloadUseCase.invoke(downloadId)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { workManager.cancelUniqueWork(any()) }
        coVerify(exactly = 0) { downloadRepository.updateDownloadStatus(any(), any()) }
    }

    @Test
    fun `invoke with non-existent item returns failure`() = runTest {
        val downloadId = "task3"
        coEvery { downloadRepository.getDownload(downloadId) } returns null

        val result = pauseDownloadUseCase.invoke(downloadId)

        assertTrue(result.isFailure)
    }
}
