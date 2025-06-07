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
class CancelDownloadUseCaseTest {

    private lateinit var context: Context
    private lateinit var downloadRepository: DownloadRepository
    private lateinit var workManager: WorkManager
    private lateinit var cancelDownloadUseCase: CancelDownloadUseCase

    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        downloadRepository = mockk()
        workManager = mockk(relaxed = true)

        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(context) } returns workManager

        cancelDownloadUseCase = CancelDownloadUseCase(context, downloadRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(WorkManager::class)
    }

    private fun createSampleDownload(id: String, status: DownloadStatus) = Download(
        id = id, mediaId = "media-$id", mediaType = DownloadMediaType.MOVIE, title = "Title for $id",
        downloadUrl = "url", status = status, progress = 50, filePath = null, addedDate = 0L
    )

    @Test
    fun `invoke with active download cancels work and updates status to CANCELLED`() = runTest {
        val downloadId = "task_active"
        for (status in listOf(DownloadStatus.DOWNLOADING, DownloadStatus.PENDING, DownloadStatus.PAUSED)) {
            val downloadItem = createSampleDownload(downloadId, status)
            coEvery { downloadRepository.getDownload(downloadId) } returns downloadItem
            every { workManager.cancelUniqueWork(downloadId) } returns mockk()
            coJustRun { downloadRepository.updateDownloadStatus(downloadId, DownloadStatus.CANCELLED) }

            val result = cancelDownloadUseCase.invoke(downloadId)

            assertTrue(result.isSuccess, "Should succeed for status $status")
            coVerify(exactly = 1) { workManager.cancelUniqueWork(downloadId) }
            coVerify(exactly = 1) { downloadRepository.updateDownloadStatus(downloadId, DownloadStatus.CANCELLED) }
            clearMocks(workManager, downloadRepository, answers = false) // Clear calls for next iteration
            // Re-mock getDownload for next iteration if needed, or ensure it's part of relaxed mock
            coEvery { downloadRepository.getDownload(downloadId) } returns downloadItem
        }
    }

    @Test
    fun `invoke with item not in cancellable state returns failure`() = runTest {
        val downloadId = "task_completed"
        val downloadItem = createSampleDownload(downloadId, DownloadStatus.COMPLETED)
        coEvery { downloadRepository.getDownload(downloadId) } returns downloadItem

        val result = cancelDownloadUseCase.invoke(downloadId)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { workManager.cancelUniqueWork(any()) }
        coVerify(exactly = 0) { downloadRepository.updateDownloadStatus(any(), any()) }
    }

    @Test
    fun `invoke with non-existent item returns failure`() = runTest {
        val downloadId = "task_non_existent"
        coEvery { downloadRepository.getDownload(downloadId) } returns null

        val result = cancelDownloadUseCase.invoke(downloadId)

        assertTrue(result.isFailure)
    }
}
