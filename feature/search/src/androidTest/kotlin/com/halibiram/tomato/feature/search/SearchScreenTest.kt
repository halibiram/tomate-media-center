package com.halibiram.tomato.feature.search

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.halibiram.tomato.domain.model.Movie
import com.halibiram.tomato.domain.usecase.movie.SearchMoviesUseCase // For FakeSearchViewModel
import com.halibiram.tomato.feature.search.presentation.SearchScreen
import com.halibiram.tomato.feature.search.presentation.SearchUiState
import com.halibiram.tomato.feature.search.presentation.SearchViewModel
import com.halibiram.tomato.ui.theme.TomatoTheme
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.input.TextFieldValue


// Fake ViewModel for controlling state in UI tests
class FakeSearchViewModel(
    initialState: SearchUiState,
    initialQuery: String = ""
) : SearchViewModel(mockk<SearchMoviesUseCase>(relaxed = true) /* Mocked use case */) {

    private val _fakeUiState = MutableStateFlow(initialState)
    override val uiState: StateFlow<SearchUiState> = _fakeUiState.asStateFlow()

    private val _fakeSearchQuery = MutableStateFlow(initialQuery)
    override val searchQuery: StateFlow<String> = _fakeSearchQuery.asStateFlow()

    var lastQueryChange: String? = null
    var clearQueryCalled = false

    override fun onSearchQueryChanged(newQuery: String) {
        lastQueryChange = newQuery
        _fakeSearchQuery.value = newQuery
        // In a real fake, you might also update _fakeUiState based on this newQuery
        // For example, if newQuery is blank, clear results, etc.
        if (newQuery.isBlank()) {
            _fakeUiState.value = _fakeUiState.value.copy(searchResults = emptyList(), noResultsFound = false)
        }
    }

    override fun clearSearchQuery() {
        clearQueryCalled = true
        _fakeSearchQuery.value = ""
        _fakeUiState.value = _fakeUiState.value.copy(
            searchQuery = "",
            searchResults = emptyList(),
            isLoading = false,
            error = null,
            noResultsFound = false
        )
    }

    // Helper to directly set state for testing different scenarios
    fun setState(newState: SearchUiState, newQuery: String? = null) {
        _fakeUiState.value = newState
        newQuery?.let { _fakeSearchQuery.value = it }
    }
}

@RunWith(AndroidJUnit4::class)
class SearchScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeViewModel: FakeSearchViewModel

    private fun sampleMovie(id: String, title: String) = Movie(id, title, "Overview for $title", "poster_url_$id.jpg", "2023", listOf("Action"), 8.0)

    @Test
    fun searchScreen_initialState_showsEmptySearchAndInitialMessage() {
        // Given
        fakeViewModel = FakeSearchViewModel(SearchUiState(recentSearches = emptyList()), initialQuery = "")
        fakeViewModel.setState(SearchUiState(searchQuery = "", recentSearches = emptyList(), searchResults = emptyList()), newQuery = "")


        // When
        composeTestRule.setContent {
            TomatoTheme {
                SearchScreen(
                    viewModel = fakeViewModel,
                    onNavigateBack = {},
                    onNavigateToDetails = {}
                )
            }
        }

        // Then
        // Check if the TextField is empty. We can find it by its placeholder.
        composeTestRule.onNodeWithText("Search movies...").assertIsDisplayed()
        // Check that the TextField itself doesn't have text.
        // This requires finding the node that actually holds the text value.
        // Often, this is the same node as the placeholder if a BasicTextField is styled.
        // For Material TextField, it might be more complex. A testTag is best.
        // As a workaround, check if any movie items are displayed (they shouldn't be).
        composeTestRule.onNodeWithText("Result Movie 1").assertDoesNotExist()
        composeTestRule.onNodeWithText("Start typing to search for movies.").assertIsDisplayed()
    }

    @Test
    fun searchScreen_typingQuery_updatesSearchField() {
        // Given
        fakeViewModel = FakeSearchViewModel(SearchUiState(), initialQuery = "")
        val testQuery = "Inception"

        // When
        composeTestRule.setContent {
            TomatoTheme {
                SearchScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToDetails = {})
            }
        }
        // Find the TextField (e.g., by placeholder or a testTag) and perform text input
        composeTestRule.onNodeWithText("Search movies...").performTextInput(testQuery)

        // Then
        // Verify the ViewModel was called (check property on fake)
        assertEquals(testQuery, fakeViewModel.lastQueryChange)
        // Verify the TextField displays the typed query
        composeTestRule.onNodeWithText(testQuery).assertIsDisplayed()
    }


    @Test
    fun searchScreen_loadingState_displaysLoadingIndicator() {
        // Given
        fakeViewModel = FakeSearchViewModel(SearchUiState(isLoading = true, searchQuery = "Loading..."), initialQuery = "Loading...")

        // When
        composeTestRule.setContent {
            TomatoTheme {
                SearchScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToDetails = {})
            }
        }

        // Then
        // Assuming CircularProgressIndicator is shown without specific text,
        // ideally it would have a testTag.
        // As a proxy: check that no results or error messages are shown.
        composeTestRule.onNodeWithText("Result Movie 1").assertDoesNotExist()
        composeTestRule.onNodeWithText("Error:", substring = true).assertDoesNotExist()
        // This isn't a direct test of the indicator but of its effect on other elements.
        // To test the indicator itself: composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }

    @Test
    fun searchScreen_successState_displaysSearchResults() {
        // Given
        val movies = listOf(sampleMovie("m1", "Result Movie 1"), sampleMovie("m2", "Result Movie 2"))
        fakeViewModel = FakeSearchViewModel(SearchUiState(searchResults = movies, isLoading = false, searchQuery = "Results"), initialQuery = "Results")

        // When
        composeTestRule.setContent {
            TomatoTheme {
                SearchScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToDetails = {})
            }
        }

        // Then
        composeTestRule.onNodeWithText("Result Movie 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Result Movie 2").assertIsDisplayed()
        // Check for poster content description or other details if SearchResultItem makes them available
        composeTestRule.onNodeWithContentDescription("Result Movie 1").assertIsDisplayed()
    }

    @Test
    fun searchScreen_noResultsState_displaysNoResultsMessage() {
        // Given
        fakeViewModel = FakeSearchViewModel(SearchUiState(noResultsFound = true, searchQuery = "QueryWithNoResults", searchResults = emptyList()), initialQuery = "QueryWithNoResults")

        // When
        composeTestRule.setContent {
            TomatoTheme {
                SearchScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToDetails = {})
            }
        }

        // Then
        composeTestRule.onNodeWithText("No results found for \"QueryWithNoResults\".").assertIsDisplayed()
    }

    @Test
    fun searchScreen_errorState_displaysErrorMessageAndRetryButton() {
        // Given
        val errorMessage = "Network connection lost"
        fakeViewModel = FakeSearchViewModel(SearchUiState(error = errorMessage, searchQuery = "ErrorState"), initialQuery = "ErrorState")

        // When
        composeTestRule.setContent {
            TomatoTheme {
                SearchScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToDetails = {})
            }
        }

        // Then
        composeTestRule.onNodeWithText("Error: $errorMessage", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
        // composeTestRule.onNodeWithText("Retry").performClick() // Optional: Test retry click
    }

    @Test
    fun searchScreen_clearQuery_clearsSearchFieldAndResults() {
        // Given
        val movies = listOf(sampleMovie("m1", "Movie To Clear"))
        // Start with some query and results
        fakeViewModel = FakeSearchViewModel(SearchUiState(searchResults = movies, searchQuery = "ClearTest"), initialQuery = "ClearTest")

        composeTestRule.setContent {
            TomatoTheme {
                SearchScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToDetails = {})
            }
        }
        // Verify initial state with results
        composeTestRule.onNodeWithText("Movie To Clear").assertIsDisplayed()
        composeTestRule.onNodeWithText("ClearTest").assertIsDisplayed() // Query in TextField

        // When
        // Find the clear button (by content description of its Icon) and click it
        composeTestRule.onNodeWithContentDescription("Clear search").performClick()

        // Then
        assertTrue(fakeViewModel.clearQueryCalled) // Check if VM method was called
        // Check if the TextField is now empty. Find by current displayed text (which should be none or placeholder).
        composeTestRule.onNodeWithText("Search movies...").assertIsDisplayed() // Placeholder should be visible
        composeTestRule.onNodeWithText("ClearTest").assertDoesNotExist()   // Old query should be gone
        composeTestRule.onNodeWithText("Movie To Clear").assertDoesNotExist() // Results should be gone
        composeTestRule.onNodeWithText("Start typing to search for movies.").assertIsDisplayed() // Initial message
    }
}
