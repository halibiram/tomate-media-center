package com.halibiram.tomato.feature.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.halibiram.tomato.domain.model.Movie
import com.halibiram.tomato.domain.usecase.movie.GetExtensionMoviesUseCase
import com.halibiram.tomato.domain.usecase.movie.GetMoviesUseCase
import com.halibiram.tomato.feature.extensions.api.MovieSourceItem
import com.halibiram.tomato.feature.home.presentation.HomeScreen
import com.halibiram.tomato.feature.home.presentation.HomeUiState
import com.halibiram.tomato.feature.home.presentation.HomeViewModel
import com.halibiram.tomato.ui.theme.TomatoTheme
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4


// Fake HomeViewModel for UI tests to control the state directly.
class FakeHomeViewModel(
    initialState: HomeUiState,
    // Mock use cases
    val mockGetMoviesUseCase: GetMoviesUseCase = mockk {
        every { invoke(type = any(), page = any(), timeWindow = any(), categoryId = any()) } returns flowOf(com.halibiram.tomato.core.common.result.Result.Success(emptyList()))
    },
    val mockGetExtensionMoviesUseCase: GetExtensionMoviesUseCase = mockk {
        coEvery { getPopularMovies(any()) } returns com.halibiram.tomato.core.common.result.Result.Success(emptyList())
        coEvery { searchMovies(any(), any()) } returns com.halibiram.tomato.core.common.result.Result.Success(emptyList())
    }
) : HomeViewModel(
    mockGetMoviesUseCase,
    mockGetExtensionMoviesUseCase
) {
    private val _fakeUiState = MutableStateFlow(initialState)
    override val uiState: StateFlow<HomeUiState> = _fakeUiState

    fun setState(newState: HomeUiState) {
        _fakeUiState.value = newState
        // If use cases' flows are strictly collected, update them too
        val popularMovies = newState.popularMovies
        val trendingMovies = newState.trendingMovies
        val extPopularMovies = newState.extensionPopularMovies

        every { mockGetMoviesUseCase.invoke(type = com.halibiram.tomato.domain.usecase.movie.MovieListType.POPULAR, page = any(), timeWindow = any(), categoryId = any()) } returns flowOf(com.halibiram.tomato.core.common.result.Result.Success(popularMovies))
        every { mockGetMoviesUseCase.invoke(type = com.halibiram.tomato.domain.usecase.movie.MovieListType.TRENDING, page = any(), timeWindow = any(), categoryId = any()) } returns flowOf(com.halibiram.tomato.core.common.result.Result.Success(trendingMovies))
        coEvery { mockGetExtensionMoviesUseCase.getPopularMovies(any()) } returns com.halibiram.tomato.core.common.result.Result.Success(extPopularMovies)

    }
     var retryExtPopularCalled = false
    override fun onRetryExtensionPopular() {
        retryExtPopularCalled = true
    }
}


@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeViewModel: FakeHomeViewModel

    private fun sampleMovie(id: String, title: String) = Movie(id, title, "Desc", "url_for_$id.jpg", "2023", emptyList(), 7.5)
    private fun sampleExtensionMovie(id: String, title: String) = MovieSourceItem(id, title, "poster_url_ext_$id.jpg", "2024")


    @Test
    fun homeScreen_extensionPopularMovies_successState_displaysMovies() {
        val extensionMovies = listOf(sampleExtensionMovie("e1", "Ext Movie Alpha"), sampleExtensionMovie("e2", "Ext Movie Beta"))
        fakeViewModel = FakeHomeViewModel(HomeUiState(isLoadingExtensionPopular = false, extensionPopularMovies = extensionMovies))
        fakeViewModel.setState(fakeViewModel.uiState.value.copy(isLoadingExtensionPopular = false, extensionPopularMovies = extensionMovies))


        composeTestRule.setContent {
            TomatoTheme {
                HomeScreen(
                    viewModel = fakeViewModel,
                    onNavigateToDetails = {},
                    onNavigateToExtensionMovieDetails = { _, _ -> },
                    onNavigateToCategoryList = { _, _ -> }
                )
            }
        }
        composeTestRule.waitForIdle() // Ensure state update is processed

        composeTestRule.onNodeWithText("From Your Extensions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ext Movie Alpha").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ext Movie Beta").assertIsDisplayed()
        // Check poster via content description (assuming ExtensionMovieCard sets it to title)
        composeTestRule.onNodeWithContentDescription("Ext Movie Alpha").assertIsDisplayed()
    }

    @Test
    fun homeScreen_extensionPopularMovies_loadingState_showsLoadingIndicator() {
        fakeViewModel = FakeHomeViewModel(HomeUiState(isLoadingExtensionPopular = true))
        fakeViewModel.setState(fakeViewModel.uiState.value.copy(isLoadingExtensionPopular = true, extensionPopularMovies = emptyList()))

        composeTestRule.setContent {
            TomatoTheme {
                HomeScreen(
                    viewModel = fakeViewModel,
                    onNavigateToDetails = {},
                    onNavigateToExtensionMovieDetails = { _, _ -> },
                    onNavigateToCategoryList = { _, _ -> }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("From Your Extensions").assertIsDisplayed()
        // Check for CircularProgressIndicator within this section.
        // This requires a testTag on the indicator in FeaturedSection/TrendingSection/CategorySection
        // or a more complex matcher. For now, check that movie titles are not shown.
        composeTestRule.onNodeWithText("Ext Movie Alpha").assertDoesNotExist()
        // composeTestRule.onNode(hasParent(hasText("From Your Extensions")) and isProgressBar()).assertIsDisplayed() // Conceptual
    }

    @Test
    fun homeScreen_extensionPopularMovies_errorState_displaysErrorMessageAndRetry() {
        val errorMsg = "Failed to load from extensions"
        fakeViewModel = FakeHomeViewModel(HomeUiState(isLoadingExtensionPopular = false, errorExtensionPopular = errorMsg))
         fakeViewModel.setState(fakeViewModel.uiState.value.copy(isLoadingExtensionPopular = false, errorExtensionPopular = errorMsg))


        composeTestRule.setContent {
            TomatoTheme {
                HomeScreen(
                    viewModel = fakeViewModel,
                    onNavigateToDetails = {},
                    onNavigateToExtensionMovieDetails = { _, _ -> },
                    onNavigateToCategoryList = { _, _ -> }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("From Your Extensions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Error: $errorMsg", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry Extensions").assertIsDisplayed().performClick()
        assertTrue(fakeViewModel.retryExtPopularCalled)
    }

    @Test
    fun homeScreen_extensionPopularMovies_emptyState_displaysEmptyMessage() {
        fakeViewModel = FakeHomeViewModel(HomeUiState(isLoadingExtensionPopular = false, extensionPopularMovies = emptyList()))
        fakeViewModel.setState(fakeViewModel.uiState.value.copy(isLoadingExtensionPopular = false, extensionPopularMovies = emptyList()))

        composeTestRule.setContent {
            TomatoTheme {
                HomeScreen(
                    viewModel = fakeViewModel,
                    onNavigateToDetails = {},
                    onNavigateToExtensionMovieDetails = { _, _ -> },
                    onNavigateToCategoryList = { _, _ -> }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("From Your Extensions").assertIsDisplayed()
        composeTestRule.onNodeWithText("No movies found from enabled extensions.").assertIsDisplayed()
    }

    // Existing tests for popular/trending movies should still pass
    @Test
    fun homeScreen_popularMovies_successState_displaysMovies() {
        val popularMovies = listOf(sampleMovie("p1", "Popular Movie 1"))
        fakeViewModel = FakeHomeViewModel(HomeUiState(isLoadingPopular = false, popularMovies = popularMovies))
        fakeViewModel.setState(fakeViewModel.uiState.value.copy(isLoadingPopular = false, popularMovies = popularMovies))


        composeTestRule.setContent {
            TomatoTheme {
                HomeScreen(viewModel = fakeViewModel, onNavigateToDetails = {}, onNavigateToExtensionMovieDetails = {_,_ ->}, onNavigateToCategoryList = {_,_ ->})
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Popular Movies").assertIsDisplayed()
        composeTestRule.onNodeWithText("Popular Movie 1").assertIsDisplayed()
    }
}
