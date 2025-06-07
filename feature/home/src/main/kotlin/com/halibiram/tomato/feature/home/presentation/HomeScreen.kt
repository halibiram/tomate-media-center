package com.halibiram.tomato.feature.home.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halibiram.tomato.domain.usecase.movie.MovieListType
import com.halibiram.tomato.feature.home.presentation.component.CategorySection
import com.halibiram.tomato.feature.home.presentation.component.FeaturedSection // Assuming this is for "Popular"
// import com.halibiram.tomato.feature.home.presentation.component.TrendingSection // Already imported by FeaturedSection, actually different.
import com.halibiram.tomato.feature.home.presentation.component.TrendingSection
import com.halibiram.tomato.ui.components.TomatoTopBar // Assuming this is your custom top bar
import com.halibiram.tomato.ui.theme.TomatoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetails: (movieId: String) -> Unit,
    onNavigateToCategoryList: (categoryId: String, categoryName: String) -> Unit
    // Add other navigation callbacks if needed, e.g., for "View More" on trending
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TomatoTopBar(
                title = "Tomato Home", // Or some dynamic title
                // scrollBehavior = scrollBehavior // If your TomatoTopBar supports it
            )
        }
    ) { innerPadding ->
        // Overall loading state for the whole screen, if all initial calls are critical
        if (uiState.isLoadingPopular && uiState.isLoadingTrending && uiState.popularMovies.isEmpty() && uiState.trendingMovies.isEmpty()) {
             Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.screenError != null) { // A general screen error
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${uiState.screenError}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                // Add a general retry button if applicable for screenError
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // Apply padding from scaffold
                    .verticalScroll(rememberScrollState()) // Make the whole screen scrollable
            ) {
                // Popular Movies Section (using FeaturedSection composable)
                FeaturedSection(
                    title = "Popular Movies",
                    movies = uiState.popularMovies,
                    isLoading = uiState.isLoadingPopular,
                    error = uiState.errorPopular,
                    onMovieClick = onNavigateToDetails,
                    onRetry = { viewModel.onRetrySection(MovieListType.POPULAR) },
                    modifier = Modifier.padding(top = 16.dp) // Add padding between sections
                )

                Spacer(modifier = Modifier.height(24.dp)) // Space between sections

                // Trending Movies Section
                TrendingSection(
                    title = "Trending Today", // Or pass timeWindow for dynamic title
                    movies = uiState.trendingMovies,
                    isLoading = uiState.isLoadingTrending,
                    error = uiState.errorTrending,
                    onMovieClick = onNavigateToDetails,
                    onRetry = { viewModel.onRetrySection(MovieListType.TRENDING) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Dynamic Category Sections
                // Example: Iterate over a predefined list of categories or fetched categories
                // For this placeholder, using the example category from HomeViewModel
                if (uiState.categoryMovies.containsKey("Action")) { // Assuming "Action" is a key used in VM
                     CategorySection(
                        categoryTitle = "Action Packed",
                        movies = uiState.categoryMovies["Action"] ?: emptyList(),
                        isLoading = uiState.isLoadingCategory, // This is a general flag, might need per-category flags
                        error = uiState.errorCategory, // General category error
                        onMovieClick = onNavigateToDetails,
                        onViewMoreClick = { onNavigateToCategoryList("action_movies_placeholder_id", "Action Packed") },
                        onRetry = { viewModel.onRetrySection(MovieListType.BY_CATEGORY, "action_movies_placeholder_id", "Action Packed") }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Add more sections as needed (e.g., different categories, genres, etc.)

                // Example for another category if your ViewModel loads multiple
                 if (uiState.categoryMovies.containsKey("Comedy")) {
                     CategorySection(
                        categoryTitle = "Comedy Greats",
                        movies = uiState.categoryMovies["Comedy"] ?: emptyList(),
                        isLoading = uiState.isLoadingCategory,
                        error = uiState.errorCategory,
                        onMovieClick = onNavigateToDetails,
                        onViewMoreClick = { onNavigateToCategoryList("comedy_movies_placeholder_id", "Comedy Greats") },
                        onRetry = { viewModel.onRetrySection(MovieListType.BY_CATEGORY, "comedy_movies_placeholder_id", "Comedy Greats") }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }


                Spacer(modifier = Modifier.height(16.dp)) // Padding at the bottom of the scrollable content
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview_Loading() {
    val mockViewModel = HomeViewModel(GetMoviesUseCase(FakeMovieRepository())) // Needs a fake repo for preview
    // Simulate loading state in mockViewModel if possible, or rely on its initial state
    // For a simple preview, we can't easily control the internal state of a Hilt VM.
    // It's better to pass a VM instance in previews that is pre-configured.
    // This preview will show the initial state of HomeViewModel.
    TomatoTheme {
        HomeScreen(
            viewModel = mockViewModel, // Provide a controlled or fake ViewModel for previews
            onNavigateToDetails = {},
            onNavigateToCategoryList = { _, _ -> }
        )
    }
}

// A simple FakeRepository for previewing HomeScreen
class FakeMovieRepository : com.halibiram.tomato.domain.repository.MovieRepository {
    override fun getPopularMovies(page: Int) = kotlinx.coroutines.flow.flowOf(com.halibiram.tomato.core.common.result.Result.Success(emptyList<com.halibiram.tomato.domain.model.Movie>()))
    override fun getTrendingMovies(timeWindow: String) = kotlinx.coroutines.flow.flowOf(com.halibiram.tomato.core.common.result.Result.Success(emptyList<com.halibiram.tomato.domain.model.Movie>()))
    override fun getMoviesByCategory(categoryId: String) = kotlinx.coroutines.flow.flowOf(com.halibiram.tomato.core.common.result.Result.Success(emptyList<com.halibiram.tomato.domain.model.Movie>()))
    override fun getMovieDetails(movieId: String) = kotlinx.coroutines.flow.flowOf(com.halibiram.tomato.core.common.result.Result.Success(null))
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview_WithData() {
    // This preview is more complex because HomeViewModel fetches data in init.
    // To show data, you'd need a HomeViewModel instance that is already in a loaded state,
    // or a fake GetMoviesUseCase that returns data immediately.
    val fakeRepo = FakeMovieRepository() // Replace with a repo that has data for preview
    val useCase = GetMoviesUseCase(fakeRepo)
    val dataViewModel = HomeViewModel(useCase)
    // To actually show data, you'd need to make fakeRepo return some movies.
    // For example, modify FakeMovieRepository to return mock movies.
    // This is becoming more involved than a simple @Preview.
    // For now, this will show the screen after initial (empty) load.
    TomatoTheme {
        HomeScreen(
            viewModel = dataViewModel,
            onNavigateToDetails = {},
            onNavigateToCategoryList = { _, _ -> }
        )
    }
}
