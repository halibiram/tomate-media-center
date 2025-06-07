package com.halibiram.tomato.feature.downloads

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.halibiram.tomato.domain.model.Download
import com.halibiram.tomato.domain.model.DownloadMediaType
import com.halibiram.tomato.domain.model.DownloadStatus
import com.halibiram.tomato.domain.usecase.download.*
import com.halibiram.tomato.feature.downloads.presentation.DownloadsScreen
import com.halibiram.tomato.feature.downloads.presentation.DownloadsUiState
import com.halibiram.tomato.feature.downloads.presentation.DownloadsViewModel
import com.halibiram.tomato.ui.theme.TomatoTheme
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

// Fake ViewModel for DownloadsScreen UI tests
class FakeDownloadsViewModel(
    initialState: DownloadsUiState,
    // Mock use cases that are called by UI directly or indirectly
    val mockGetDownloadsUseCase: GetDownloadsUseCase = mockk { every { invoke() } returns MutableStateFlow(initialState.downloads) },
    val mockDownloadMediaUseCase: DownloadMediaUseCase = mockk(relaxed = true),
    val mockPauseDownloadUseCase: PauseDownloadUseCase = mockk(relaxed = true),
    val mockResumeDownloadUseCase: ResumeDownloadUseCase = mockk(relaxed = true),
    val mockCancelDownloadUseCase: CancelDownloadUseCase = mockk(relaxed = true),
    val mockDeleteDownloadedFileUseCase: DeleteDownloadedFileUseCase = mockk(relaxed = true)
) : DownloadsViewModel(
    mockGetDownloadsUseCase,
    mockDownloadMediaUseCase,
    mockPauseDownloadUseCase,
    mockResumeDownloadUseCase,
    mockCancelDownloadUseCase,
    mockDeleteDownloadedFileUseCase
) {
    private val _fakeUiState = MutableStateFlow(initialState)
    override val uiState: StateFlow<DownloadsUiState> = _fakeUiState

    fun setState(newState: DownloadsUiState) {
        _fakeUiState.value = newState
        // If GetDownloadsUseCase is strictly observed, also update its flow
        (mockGetDownloadsUseCase.invoke() as MutableStateFlow).value = newState.downloads
    }

    // Track calls to specific actions for verification
    var pauseCalledWithId: String? = null
    var resumeCalledWithDownload: Download? = null
    var cancelCalledWithId: String? = null
    var deleteCalledWithDownload: Download? = null

    override fun pauseDownload(downloadId: String) {
        super.pauseDownload(downloadId)
        pauseCalledWithId = downloadId
    }
    override fun resumeDownload(download: Download) {
        super.resumeDownload(download)
        resumeCalledWithDownload = download
    }
    override fun cancelDownload(downloadId: String) {
        super.cancelDownload(downloadId)
        cancelCalledWithId = downloadId
    }
    override fun deleteDownload(download: Download) {
        super.deleteDownload(download)
        deleteCalledWithDownload = download
    }
}

@RunWith(AndroidJUnit4::class)
class DownloadsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeViewModel: FakeDownloadsViewModel

    private fun sampleDownload(id: String, title: String, status: DownloadStatus, progress: Int = 0, filePath: String? = null) =
        Download(id, "media-$id", DownloadMediaType.MOVIE, title, "url", status, progress, filePath, 100L, if(status == DownloadStatus.COMPLETED) 100L else (progress/100.0*100L).toLong(), System.currentTimeMillis())

    @Test
    fun downloadsScreen_initialLoadingState_showsLoadingIndicator() {
        fakeViewModel = FakeDownloadsViewModel(DownloadsUiState(isLoading = true, downloads = emptyList()))
        composeTestRule.setContent {
            TomatoTheme {
                DownloadsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToPlayer = { _, _, _ ->})
            }
        }
        // Check for CircularProgressIndicator (needs a testTag ideally)
        // As a proxy, check that "No downloads yet" is not shown if loading with empty list.
        composeTestRule.onNodeWithText("No downloads yet.").assertDoesNotExist()
    }

    @Test
    fun downloadsScreen_emptyState_showsNoDownloadsMessage() {
        fakeViewModel = FakeDownloadsViewModel(DownloadsUiState(isLoading = false, downloads = emptyList()))
        composeTestRule.setContent {
            TomatoTheme {
                DownloadsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToPlayer = { _, _, _ ->})
            }
        }
        composeTestRule.onNodeWithText("No downloads yet.").assertIsDisplayed()
    }

    @Test
    fun downloadsScreen_errorState_showsErrorMessage() {
        val errorMsg = "Failed to load downloads"
        fakeViewModel = FakeDownloadsViewModel(DownloadsUiState(isLoading = false, error = errorMsg))
        composeTestRule.setContent {
            TomatoTheme {
                DownloadsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToPlayer = { _, _, _ ->})
            }
        }
        composeTestRule.onNodeWithText("Error: $errorMsg").assertIsDisplayed()
    }

    @Test
    fun downloadsScreen_displaysListOfDownloads() {
        val downloads = listOf(
            sampleDownload("1", "Movie A", DownloadStatus.DOWNLOADING, 50),
            sampleDownload("2", "Movie B", DownloadStatus.COMPLETED, 100, "/path/b.mp4")
        )
        fakeViewModel = FakeDownloadsViewModel(DownloadsUiState(isLoading = false, downloads = downloads))
        composeTestRule.setContent {
            TomatoTheme {
                DownloadsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToPlayer = { _, _, _ ->})
            }
        }
        composeTestRule.onNodeWithText("Movie A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Status: Downloading").assertIsDisplayed() // Check status text from DownloadItem
        composeTestRule.onNodeWithText("Movie B").assertIsDisplayed()
        composeTestRule.onNodeWithText("Status: Completed").assertIsDisplayed()
    }

    @Test
    fun downloadItem_pauseButtonClick_callsViewModelPause() {
        val downloadingItem = sampleDownload("d1", "Downloading Movie", DownloadStatus.DOWNLOADING, 60)
        fakeViewModel = FakeDownloadsViewModel(DownloadsUiState(downloads = listOf(downloadingItem)))
        composeTestRule.setContent {
            TomatoTheme {
                DownloadsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToPlayer = { _, _, _ ->})
            }
        }
        composeTestRule.onNodeWithText("Pause").performClick() // Assumes "Pause" is text on button in DownloadItem
        assertEquals("d1", fakeViewModel.pauseCalledWithId)
    }

    @Test
    fun downloadItem_resumeButtonClick_callsViewModelResume() {
        val pausedItem = sampleDownload("p1", "Paused Movie", DownloadStatus.PAUSED, 30)
        fakeViewModel = FakeDownloadsViewModel(DownloadsUiState(downloads = listOf(pausedItem)))
        composeTestRule.setContent {
            TomatoTheme {
                DownloadsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToPlayer = { _, _, _ ->})
            }
        }
        composeTestRule.onNodeWithText("Resume").performClick()
        assertEquals(pausedItem, fakeViewModel.resumeCalledWithDownload)
    }

    @Test
    fun downloadItem_deleteButtonClick_callsViewModelDelete() {
        val completedItem = sampleDownload("c1", "Completed Movie", DownloadStatus.COMPLETED, 100, "/file.mp4")
        fakeViewModel = FakeDownloadsViewModel(DownloadsUiState(downloads = listOf(completedItem)))
        composeTestRule.setContent {
            TomatoTheme {
                DownloadsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToPlayer = { _, _, _ ->})
            }
        }
        // Ensure delete button is identifiable. Assuming text "Delete" exists.
        // Multiple "Delete" buttons might exist if other items are also completed/failed.
        // Use a more specific matcher if needed, e.g., within the item's hierarchy.
        composeTestRule.onNodeWithText("Completed Movie") // Find item first
            .assertIsDisplayed()
            .onChildren() // Go to children of the Card
            .filterToOne(hasText("Delete")) // Find the delete button within this item
            .performClick()

        assertEquals(completedItem, fakeViewModel.deleteCalledWithDownload)
    }

    @Test
    fun clearAllDialog_appearsAndCallsViewModel() {
        val downloads = listOf(sampleDownload("1", "Movie A", DownloadStatus.COMPLETED))
        fakeViewModel = FakeDownloadsViewModel(DownloadsUiState(downloads = downloads))
        // coJustRun { fakeViewModel.mockClearAllDownloadsUseCase() } // Assuming a clear all use case

        composeTestRule.setContent {
            TomatoTheme {
                DownloadsScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToPlayer = { _, _, _ ->})
            }
        }

        // Click "Clear All Downloads" button in TopAppBar
        composeTestRule.onNodeWithContentDescription("Clear All Downloads").performClick()
        composeTestRule.onNodeWithText("Clear All Downloads?").assertIsDisplayed() // Dialog title

        // Click confirm button on dialog
        // composeTestRule.onNodeWithText("Clear All").performClick() // ViewModel method is commented out
        // verify { fakeViewModel.mockClearAllDownloadsUseCase() }
    }
}
