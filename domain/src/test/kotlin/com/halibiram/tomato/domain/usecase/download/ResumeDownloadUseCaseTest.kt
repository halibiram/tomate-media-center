package com.halibiram.tomato.domain.usecase.download

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.halibiram.tomato.domain.model.Download
import com.halibiram.tomato.domain.model.DownloadMediaType
import com.halibiram.tomato.domain.model.DownloadStatus
import com.halibiram.tomato.domain.repository.DownloadRepository
import com.halibiram.tomato.feature.downloads.worker.DownloadWorker
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class ResumeDownloadUseCaseTest {

    private lateinit var context: Context
    private lateinit var downloadRepository: DownloadRepository
    private lateinit var workManager: WorkManager
    private lateinit var resumeDownloadUseCase: ResumeDownloadUseCase

    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        downloadRepository = mockk()
        workManager = mockk(relaxed = true)

        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(context) } returns workManager

        resumeDownloadUseCase = ResumeDownloadUseCase(context, downloadRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(WorkManager::class)
    }

    private fun createSampleDownload(id: String, status: DownloadStatus, url: String = "http://example.com/video.mp4") = Download(
        id = id, mediaId = "media-$id", mediaType = DownloadMediaType.MOVIE, title = "Title for $id",
        downloadUrl = url, status = status, progress = 50, filePath = null, addedDate = 0L
    )

    @Test
    fun `invoke with paused download updates status and enqueues worker`() = runTest {
        val download = createSampleDownload("task_paused", DownloadStatus.PAUSED)
        coJustRun { downloadRepository.updateDownloadStatus(download.id, DownloadStatus.PENDING) }
        every { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) } returns mockk()

        val resultId = resumeDownloadUseCase.invoke(download)

        assertEquals(download.id, resultId)
        coVerify { downloadRepository.updateDownloadStatus(download.id, DownloadStatus.PENDING) }
        val slot = slot<OneTimeWorkRequest>()
        verify { workManager.enqueueUniqueWork(eq(download.id), any(), capture(slot)) }
        assertEquals(download.id, slot.captured.workSpec.input.getString(DownloadWorker.KEY_DOWNLOAD_ID))
        assertEquals(download.downloadUrl, slot.captured.workSpec.input.getString(DownloadWorker.KEY_MEDIA_URL))
    }

    @Test
    fun `invoke with failed download updates status and enqueues worker`() = runTest {
        val download = createSampleDownload("task_failed", DownloadStatus.FAILED)
        coJustRun { downloadRepository.updateDownloadStatus(download.id, DownloadStatus.PENDING) }
        every { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) } returns mockk()

        val resultId = resumeDownloadUseCase.invoke(download)

        assertEquals(download.id, resultId)
        coVerify { downloadRepository.updateDownloadStatus(download.id, DownloadStatus.PENDING) }
        verify { workManager.enqueueUniqueWork(eq(download.id), any(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `invoke with download not in resumable state returns null`() = runTest {
        val download = createSampleDownload("task_downloading", DownloadStatus.DOWNLOADING)

        val resultId = resumeDownloadUseCase.invoke(download)

        assertNull(resultId)
        coVerify(exactly = 0) { downloadRepository.updateDownloadStatus(any(), any()) }
        verify(exactly = 0) { workManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }
}
