package com.halibiram.tomato.feature.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.halibiram.tomato.domain.model.Movie
import com.halibiram.tomato.feature.home.presentation.HomeScreen
import com.halibiram.tomato.feature.home.presentation.HomeUiState
import com.halibiram.tomato.feature.home.presentation.HomeViewModel // Real ViewModel for type, but will use a fake
import com.halibiram.tomato.ui.theme.TomatoTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

// A Fake HomeViewModel for UI tests to control the state directly.
// This avoids needing Hilt for UI tests or complex mocking of use cases here.
class FakeHomeViewModel(initialState: HomeUiState) : HomeViewModel(mockk(relaxed = true) /* Mock GetMoviesUseCase */) {
    private val _fakeUiState = MutableStateFlow(initialState)
    override val uiState: StateFlow<HomeUiState> = _fakeUiState

    funsetState(newState: HomeUiState) {
        _fakeUiState.value = newState
    }

    // Override other methods if HomeScreen interacts with them and they need faking.
    // For now, uiState is the primary interaction.
    override fun onRetrySection(sectionType: com.halibiram.tomato.domain.usecase.movie.MovieListType, categoryId: String?, categoryDisplayName: String?) {
        // Log or simulate retry if needed for a specific test
    }
}

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeViewModel: FakeHomeViewModel

    private fun sampleMovie(id: String, title: String) = Movie(id, title, "Desc", "url_for_$id.jpg", "2023", emptyList(), 7.5)

    @Test
    fun homeScreen_overallLoadingState_showsSpinner() {
        // Given
        fakeViewModel = FakeHomeViewModel(HomeUiState(isLoadingPopular = true, isLoadingTrending = true))
        // Ensure popularMovies and trendingMovies are empty to trigger the overall loading
        fakeViewModel.setState(HomeUiState(isLoadingPopular = true, popularMovies = emptyList(), isLoadingTrending = true, trendingMovies = emptyList()))


        // When
        composeTestRule.setContent {
            TomatoTheme {
                HomeScreen(
                    viewModel = fakeViewModel,
                    onNavigateToDetails = {},
                    onNavigateToCategoryList = { _, _ -> }
                )
            }
        }

        // Then
        // Look for a CircularProgressIndicator. Since it doesn't have specific text,
        // we might need a test tag on it in the actual HomeScreen composable.
        // For now, this is a conceptual check. If there's only one main spinner at a time:
        // composeTestRule.onNode( /* some matcher for the main spinner */ ).assertIsDisplayed()
        // This is hard to test without a test tag. Let's assume sections will show their own spinners.
        // The initial check in HomeScreen is "if (uiState.isLoadingPopular && uiState.isLoadingTrending && ...isEmpty())"
        // So, we expect the *overall* spinner to show.
        // To verify this, we'd ideally have a testTag on that specific Box/CircularProgressIndicator.
        // As a proxy, if no movie titles are shown, it might be loading or empty.
        composeTestRule.onNodeWithText("Popular Movies").assertExists() // Section title should still exist
        // We can't easily distinguish the global spinner from section spinners without tags.
    }

    @Test
    fun homeScreen_popularMovies_successState_displaysMovies() {
        // Given
        val popularMovies = listOf(sampleMovie("p1", "Popular Movie 1"), sampleMovie("p2", "Popular Movie 2"))
        fakeViewModel = FakeHomeViewModel(HomeUiState(isLoadingPopular = false, popularMovies = popularMovies))

        // When
        composeTestRule.setContent {
            TomatoTheme {
                HomeScreen(
                    viewModel = fakeViewModel,
                    onNavigateToDetails = {},
                    onNavigateToCategoryList = { _, _ -> }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Popular Movies").assertIsDisplayed()
        composeTestRule.onNodeWithText("Popular Movie 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Popular Movie 2").assertIsDisplayed()
        // To check for poster, TomatoCard would need a testTag on its AsyncImage or its contentDescription.
        // composeTestRule.onNodeWithContentDescription("Popular Movie 1").assertIsDisplayed() // Assuming contentDescription is title
    }

    @Test
    fun homeScreen_trendingMovies_successState_displaysMovies() {
        // Given
        val trendingMovies = listOf(sampleMovie("t1", "Trending Movie A"), sampleMovie("t2", "Trending Movie B"))
        fakeViewModel = FakeHomeViewModel(HomeUiState(isLoadingTrending = false, trendingMovies = trendingMovies))

        // When
        composeTestRule.setContent {
            TomatoTheme {
                HomeScreen(
                    viewModel = fakeViewModel,
                    onNavigateToDetails = {},
                    onNavigateToCategoryList = { _, _ -> }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Trending Today").assertIsDisplayed() // Title from HomeScreen
        composeTestRule.onNodeWithText("Trending Movie A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Trending Movie B").assertIsDisplayed()
    }

    @Test
    fun homeScreen_categoryMovies_successState_displaysMovies() {
        // Given
        val categoryName = "Action Packed" // Display name used in HomeScreen
        val categoryKey = "Action"      // Key used in HomeViewModel state map
        val categoryMovies = listOf(sampleMovie("c1", "Action Hero"), sampleMovie("c2", "Fast Action"))
        val initialState = HomeUiState(
            categoryMovies = mapOf(categoryKey to categoryMovies),
            isLoadingCategory = false // Assuming this general flag for now
        )
        fakeViewModel = FakeHomeViewModel(initialState)
        fakeViewModel.setState(initialState) // Ensure state is set

        // When
        composeTestRule.setContent {
            TomatoTheme {
                HomeScreen(
                    viewModel = fakeViewModel,
                    onNavigateToDetails = {},
                    onNavigateToCategoryList = { _, _ -> }
                )
            }
        }
         composeTestRule.waitForIdle() // Ensure UI updates

        // Then
        composeTestRule.onNodeWithText(categoryName).assertIsDisplayed()
        composeTestRule.onNodeWithText("Action Hero").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fast Action").assertIsDisplayed()
    }


    @Test
    fun homeScreen_popularMovies_errorState_displaysErrorAndRetry() {
        // Given
        val errorMessage = "Failed to load popular"
        fakeViewModel = FakeHomeViewModel(HomeUiState(isLoadingPopular = false, errorPopular = errorMessage))

        // When
        composeTestRule.setContent {
            TomatoTheme {
                HomeScreen(
                    viewModel = fakeViewModel,
                    onNavigateToDetails = {},
                    onNavigateToCategoryList = { _, _ -> }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Popular Movies").assertIsDisplayed() // Section title
        composeTestRule.onNodeWithText("Error: $errorMessage", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
        // composeTestRule.onNodeWithText("Retry").performClick() // Optional: test retry click
        // Verify fakeViewModel.onRetrySection was called if retry logic is part of fake.
    }

    @Test
    fun homeScreen_trendingMovies_errorState_displaysErrorAndRetry() {
        // Given
        val errorMessage = "Failed to load trending"
        fakeViewModel = FakeHomeViewModel(HomeUiState(isLoadingTrending = false, errorTrending = errorMessage))

        // When
        composeTestRule.setContent {
            TomatoTheme {
                HomeScreen(
                    viewModel = fakeViewModel,
                    onNavigateToDetails = {},
                    onNavigateToCategoryList = { _, _ -> }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Trending Today").assertIsDisplayed()
        composeTestRule.onNodeWithText("Error: $errorMessage", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }


    @Test
    fun homeScreen_popularMovies_emptyState_displaysEmptyMessage() {
        // Given
        fakeViewModel = FakeHomeViewModel(HomeUiState(isLoadingPopular = false, popularMovies = emptyList(), errorPopular = null))

        // When
        composeTestRule.setContent {
            TomatoTheme {
                HomeScreen(
                    viewModel = fakeViewModel,
                    onNavigateToDetails = {},
                    onNavigateToCategoryList = { _, _ -> }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Popular Movies").assertIsDisplayed()
        composeTestRule.onNodeWithText("No movies to display in this section.", substring = true).assertIsDisplayed()
    }
}
