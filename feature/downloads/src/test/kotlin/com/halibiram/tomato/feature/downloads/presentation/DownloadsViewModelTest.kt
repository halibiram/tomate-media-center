package com.halibiram.tomato.feature.downloads.presentation

import com.halibiram.tomato.core.common.result.TomatoException
import com.halibiram.tomato.domain.model.Download
import com.halibiram.tomato.domain.model.DownloadMediaType
import com.halibiram.tomato.domain.model.DownloadStatus
import com.halibiram.tomato.domain.usecase.download.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

// Assume MainCoroutineExtension is in a shared test utility module
@ExperimentalCoroutinesApi
class MainCoroutineExtension(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : org.junit.jupiter.api.extension.BeforeEachCallback, org.junit.jupiter.api.extension.AfterEachCallback {
    override fun beforeEach(context: org.junit.jupiter.api.extension.ExtensionContext?) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun afterEach(context: org.junit.jupiter.api.extension.ExtensionContext?) {
        Dispatchers.resetMain()
    }
}

@ExperimentalCoroutinesApi
@ExtendWith(MainCoroutineExtension::class)
class DownloadsViewModelTest {

    private lateinit var getDownloadsUseCase: GetDownloadsUseCase
    private lateinit var downloadMediaUseCase: DownloadMediaUseCase
    private lateinit var pauseDownloadUseCase: PauseDownloadUseCase
    private lateinit var resumeDownloadUseCase: ResumeDownloadUseCase
    private lateinit var cancelDownloadUseCase: CancelDownloadUseCase
    private lateinit var deleteDownloadedFileUseCase: DeleteDownloadedFileUseCase
    private lateinit var viewModel: DownloadsViewModel
    private lateinit var testDispatcher: TestDispatcher

    private val sampleDownloadsFlow = MutableStateFlow<List<Download>>(emptyList())

    @BeforeEach
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        getDownloadsUseCase = mockk()
        downloadMediaUseCase = mockk(co जस्टRun = true) // coJustRun for suspend fun ()
        pauseDownloadUseCase = mockk(coJustRun = true)
        resumeDownloadUseCase = mockk(coJustRun = true)
        cancelDownloadUseCase = mockk(coJustRun = true)
        deleteDownloadedFileUseCase = mockk(coJustRun = true)

        every { getDownloadsUseCase.invoke() } returns sampleDownloadsFlow

        viewModel = DownloadsViewModel(
            getDownloadsUseCase,
            downloadMediaUseCase,
            pauseDownloadUseCase,
            resumeDownloadUseCase,
            cancelDownloadUseCase,
            deleteDownloadedFileUseCase
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createSampleDownload(id: String, status: DownloadStatus, title: String = "Sample Download") = Download(
        id = id, mediaId = "media-$id", mediaType = DownloadMediaType.MOVIE, title = title,
        downloadUrl = "http://example.com/$id.mp4", status = status, progress = 0, filePath = null,
        addedDate = System.currentTimeMillis()
    )

    @Test
    fun `initial state is loading and then updates with downloads from GetDownloadsUseCase`() = runTest(testDispatcher.scheduler) {
        // Initial state from constructor (isLoading = true by default in StateFlow)
        var uiState = viewModel.uiState.value
        assertTrue(uiState.isLoading)
        assertTrue(uiState.downloads.isEmpty())

        // Simulate GetDownloadsUseCase emitting data
        val downloads = listOf(createSampleDownload("1", DownloadStatus.DOWNLOADING))
        sampleDownloadsFlow.value = downloads
        advanceUntilIdle() // Allow collection to process

        uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading) // Should be false after first emission
        assertEquals(downloads, uiState.downloads)
        assertNull(uiState.error)
    }

    @Test
    fun `GetDownloadsUseCase emitting error updates uiState`() = runTest(testDispatcher.scheduler) {
        val errorMessage = "Failed to fetch downloads"
        every { getDownloadsUseCase.invoke() } returns flowOf(throw TomatoException(errorMessage))

        // Re-initialize ViewModel to trigger new collection with erroring flow
         viewModel = DownloadsViewModel(
            getDownloadsUseCase, downloadMediaUseCase, pauseDownloadUseCase,
            resumeDownloadUseCase, cancelDownloadUseCase, deleteDownloadedFileUseCase
        )
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(errorMessage, uiState.error)
    }


    @Test
    fun `startNewDownload calls DownloadMediaUseCase`() = runTest(testDispatcher.scheduler) {
        val mediaId = "movie123"
        val mediaUrl = "http://example.com/movie.mp4"
        val title = "Test Movie"
        coEvery { downloadMediaUseCase.invoke(any(), any(), any(), any(), any()) } returns "downloadTaskId1"


        viewModel.startNewDownload(mediaId, mediaUrl, title, DownloadMediaType.MOVIE, null)
        advanceUntilIdle()

        coVerify { downloadMediaUseCase.invoke(mediaId, DownloadMediaType.MOVIE, mediaUrl, title, null) }
    }

    @Test
    fun `startNewDownload with existing download shows error`() = runTest(testDispatcher.scheduler) {
        val mediaId = "movie123"
        val mediaUrl = "http://example.com/movie.mp4"
        val title = "Test Movie"
        // Simulate use case returning null (meaning download already exists or couldn't start)
        coEvery { downloadMediaUseCase.invoke(any(), any(), any(), any(), any()) } returns null

        viewModel.startNewDownload(mediaId, mediaUrl, title, DownloadMediaType.MOVIE, null)
        advanceUntilIdle()

        coVerify { downloadMediaUseCase.invoke(mediaId, DownloadMediaType.MOVIE, mediaUrl, title, null) }
        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("already exists or could not be started"))
    }


    @Test
    fun `pauseDownload calls PauseDownloadUseCase`() = runTest(testDispatcher.scheduler) {
        val downloadId = "1"
        viewModel.pauseDownload(downloadId)
        advanceUntilIdle()
        coVerify { pauseDownloadUseCase(downloadId) }
    }

    @Test
    fun `resumeDownload calls ResumeDownloadUseCase`() = runTest(testDispatcher.scheduler) {
        val download = createSampleDownload("1", DownloadStatus.PAUSED)
        viewModel.resumeDownload(download)
        advanceUntilIdle()
        coVerify { resumeDownloadUseCase(download) }
    }

    @Test
    fun `cancelDownload calls CancelDownloadUseCase`() = runTest(testDispatcher.scheduler) {
        val downloadId = "1"
        viewModel.cancelDownload(downloadId)
        advanceUntilIdle()
        coVerify { cancelDownloadUseCase(downloadId) }
    }

    @Test
    fun `deleteDownload calls DeleteDownloadedFileUseCase`() = runTest(testDispatcher.scheduler) {
        val download = createSampleDownload("1", DownloadStatus.COMPLETED, title = "Test")
        download.copy(filePath = "/path/to/file") // Assume it has a file path
        viewModel.deleteDownload(download) // ViewModel's deleteDownload takes Download object
        advanceUntilIdle()
        coVerify { deleteDownloadedFileUseCase(download.id) }
    }

    @Test
    fun `retryDownload calls ResumeDownloadUseCase if status is FAILED`() = runTest(testDispatcher.scheduler) {
        val failedDownload = createSampleDownload("f1", DownloadStatus.FAILED)
        viewModel.retryDownload(failedDownload)
        advanceUntilIdle()
        coVerify { resumeDownloadUseCase(failedDownload) }
    }

    @Test
    fun `retryDownload does not call ResumeDownloadUseCase if status is not FAILED`() = runTest(testDispatcher.scheduler) {
        val completedDownload = createSampleDownload("c1", DownloadStatus.COMPLETED)
        viewModel.retryDownload(completedDownload)
        advanceUntilIdle()
        coVerify(exactly = 0) { resumeDownloadUseCase(any()) }
    }
}
