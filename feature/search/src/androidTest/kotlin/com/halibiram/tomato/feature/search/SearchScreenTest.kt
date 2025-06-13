package com.halibiram.tomato.feature.search

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.halibiram.tomato.domain.model.Movie
import com.halibiram.tomato.domain.usecase.movie.GetExtensionMoviesUseCase // For FakeSearchViewModel
import com.halibiram.tomato.domain.usecase.movie.SearchMoviesUseCase // For FakeSearchViewModel
import com.halibiram.tomato.feature.extensions.api.MovieSourceItem // For extension results
import com.halibiram.tomato.feature.search.presentation.SearchScreen
import com.halibiram.tomato.feature.search.presentation.SearchUiState
import com.halibiram.tomato.feature.search.presentation.SearchViewModel
import com.halibiram.tomato.ui.theme.TomatoTheme
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
// import androidx.compose.ui.semantics.SemanticsProperties // Not used in this version
// import androidx.compose.ui.text.input.TextFieldValue // Not used directly

// Fake ViewModel for SearchScreen UI tests
class FakeSearchViewModel(
    initialState: SearchUiState,
    initialQuery: String = "",
    val mockSearchMoviesUseCase: SearchMoviesUseCase = mockk {
        coEvery { invoke(any(), any()) } returns flowOf(com.halibiram.tomato.core.common.result.Result.Success(emptyList()))
    },
    val mockGetExtensionMoviesUseCase: GetExtensionMoviesUseCase = mockk {
        coEvery { searchMovies(any(), any()) } returns com.halibiram.tomato.core.common.result.Result.Success(emptyList())
    }
) : SearchViewModel(
    mockSearchMoviesUseCase,
    mockGetExtensionMoviesUseCase
) {
    private val _fakeUiState = MutableStateFlow(initialState)
    override val uiState: StateFlow<SearchUiState> = _fakeUiState.asStateFlow()

    private val _fakeSearchQuery = MutableStateFlow(initialQuery)
    override val searchQuery: StateFlow<String> = _fakeSearchQuery.asStateFlow()

    var lastQueryChange: String? = null
    var clearQueryCalledCount = 0 // To count calls

    override fun onSearchQueryChanged(newQuery: String) {
        lastQueryChange = newQuery
        _fakeSearchQuery.value = newQuery
        if (newQuery.isBlank()) {
            _fakeUiState.value = _fakeUiState.value.copy(
                searchQuery = "", internalSearchResults = emptyList(), extensionSearchResults = emptyList(),
                noInternalResultsFound = false, noExtensionResultsFound = false,
                isLoadingInternalSearch = false, isLoadingExtensionSearch = false,
                errorInternalSearch = null, errorExtensionSearch = null
            )
        } else {
             _fakeUiState.value = _fakeUiState.value.copy(searchQuery = newQuery)
        }
        // Actual search triggering is handled by the flow collection in real VM.
        // For fake, test will set state for results.
    }

    override fun clearSearchQuery() {
        clearQueryCalledCount++
        _fakeSearchQuery.value = ""
        _fakeUiState.value = SearchUiState() // Reset to default initial state
    }

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
    private fun sampleExtensionMovie(id: String, title: String) = MovieSourceItem(id, title, "poster_url_ext_$id.jpg", "2024")


    @Test
    fun searchScreen_extensionResults_successState_displaysExtensionMovies() {
        val query = "find ext"
        val extensionResults = listOf(sampleExtensionMovie("e1", "Ext Movie Alpha"), sampleExtensionMovie("e2", "Ext Movie Beta"))
        fakeViewModel = FakeSearchViewModel(
            SearchUiState(searchQuery = query, isLoadingExtensionSearch = false, extensionSearchResults = extensionResults),
            initialQuery = query
        )
        // Ensure no internal results to isolate testing extension section
        fakeViewModel.setState(fakeViewModel.uiState.value.copy(internalSearchResults = emptyList(), noInternalResultsFound = true))


        composeTestRule.setContent {
            TomatoTheme {
                SearchScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToDetails = {}, onNavigateToExtensionDetails = {_,_->})
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Results from Extensions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ext Movie Alpha").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ext Movie Beta").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Ext Movie Alpha").assertIsDisplayed() // Checks ExtensionMovieCard's poster
    }

    @Test
    fun searchScreen_extensionResults_loadingState_showsLoading() {
        val query = "loading ext"
        fakeViewModel = FakeSearchViewModel(
            SearchUiState(searchQuery = query, isLoadingExtensionSearch = true, extensionSearchResults = emptyList()),
            initialQuery = query
        )
        // Assume internal search is not loading or has no results to simplify focus
        fakeViewModel.setState(fakeViewModel.uiState.value.copy(isLoadingInternalSearch = false, internalSearchResults = emptyList(), noInternalResultsFound = true))


        composeTestRule.setContent {
            TomatoTheme {
                SearchScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToDetails = {}, onNavigateToExtensionDetails = {_,_->})
            }
        }
        composeTestRule.waitForIdle()

        // If both internal and ext are loading, a single spinner is shown by current SearchScreen logic
        // To test ext section spinner specifically, ensure internal is not loading and has no results.
        // If SearchScreen shows section-specific spinners, this test would be different.
        // Current SearchScreen logic: showLoading if (isLoadingInternal || isLoadingExtension) && (both lists empty)
        // So, if internal is also loading and empty, one spinner shown.
        // If internal is loaded and empty, and ext is loading, still one spinner.
        composeTestRule.onNode(isProgressBar()).assertIsDisplayed()
    }

    @Test
    fun searchScreen_extensionResults_errorState_displaysError() {
        val query = "error ext"
        val errorMsg = "Extensions failed to search"
        fakeViewModel = FakeSearchViewModel(
            SearchUiState(searchQuery = query, isLoadingExtensionSearch = false, errorExtensionSearch = errorMsg),
            initialQuery = query
        )
        fakeViewModel.setState(fakeViewModel.uiState.value.copy(internalSearchResults = emptyList(), noInternalResultsFound = true))


        composeTestRule.setContent {
            TomatoTheme {
                SearchScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToDetails = {}, onNavigateToExtensionDetails = {_,_->})
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Extension Search Error: $errorMsg", substring = true).assertIsDisplayed()
        // Check for retry button within this error item
        composeTestRule.onNode(hasParent(hasText("Extension Search Error", substring = true)) and hasText("Retry")).assertIsDisplayed()
    }

    @Test
    fun searchScreen_extensionResults_emptyState_displaysEmptyMessage() {
        val query = "empty ext"
        fakeViewModel = FakeSearchViewModel(
            SearchUiState(searchQuery = query, isLoadingExtensionSearch = false, extensionSearchResults = emptyList(), noExtensionResultsFound = true),
            initialQuery = query
        )
        fakeViewModel.setState(fakeViewModel.uiState.value.copy(internalSearchResults = emptyList(), noInternalResultsFound = true))


        composeTestRule.setContent {
            TomatoTheme {
                SearchScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToDetails = {}, onNavigateToExtensionDetails = {_,_->})
            }
        }
        composeTestRule.waitForIdle()

        // If both internal and extension have no results for a query, the "No results found for '$query'" message appears.
        // If only extensions have no results (but internal might), a section-specific message appears.
        composeTestRule.onNodeWithText("No extension results for \"$query\".").assertIsDisplayed()
    }

    @Test
    fun searchScreen_bothResultsDisplayed_internalAndExtension() {
        val query = "both results"
        val internalMovies = listOf(sampleMovie("m1", "Internal Movie One"))
        val extensionMovies = listOf(sampleExtensionMovie("e1", "Extension Movie One"))
        fakeViewModel = FakeSearchViewModel(
            SearchUiState(
                searchQuery = query,
                internalSearchResults = internalMovies,
                extensionSearchResults = extensionMovies
            ),
            initialQuery = query
        )

        composeTestRule.setContent {
            TomatoTheme {
                SearchScreen(viewModel = fakeViewModel, onNavigateBack = {}, onNavigateToDetails = {}, onNavigateToExtensionDetails = {_,_->})
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Results from Library").assertIsDisplayed()
        composeTestRule.onNodeWithText("Internal Movie One").assertIsDisplayed()

        composeTestRule.onNodeWithText("Results from Extensions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Extension Movie One").assertIsDisplayed()
    }
}
