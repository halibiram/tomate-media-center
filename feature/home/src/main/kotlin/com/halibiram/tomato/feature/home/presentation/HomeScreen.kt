package com.halibiram.tomato.feature.home.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.halibiram.tomato.domain.model.Movie
import com.halibiram.tomato.domain.usecase.movie.MovieListType
import com.halibiram.tomato.feature.home.presentation.component.CategorySection
import com.halibiram.tomato.feature.home.presentation.component.FeaturedSection
import com.halibiram.tomato.feature.home.presentation.component.TrendingSection
import com.halibiram.tomato.feature.home.presentation.component.ExtensionMovieCard // Import new card
import com.halibiram.tomato.ui.components.TomatoTopBar
import com.halibiram.tomato.ui.theme.TomatoTheme
// import com.halibiram.tomato.domain.usecase.movie.GetMoviesUseCase // For FakeRepo in Preview
// import com.halibiram.tomato.domain.usecase.movie.GetExtensionMoviesUseCase // For FakeRepo in Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDetails: (movieId: String) -> Unit, // For regular movies
    onNavigateToExtensionMovieDetails: (movieSourceItemId: String, extensionId: String?) -> Unit, // For extension movies
    onNavigateToCategoryList: (categoryId: String, categoryName: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TomatoTopBar(title = "Tomato Home")
        }
    ) { innerPadding ->
        if (uiState.isLoadingPopular && uiState.isLoadingTrending && uiState.isLoadingExtensionPopular &&
            uiState.popularMovies.isEmpty() && uiState.trendingMovies.isEmpty() && uiState.extensionPopularMovies.isEmpty()
        ) {
             Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.screenError != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: ${uiState.screenError}", color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Popular Movies Section
                FeaturedSection(
                    title = "Popular Movies",
                    movies = uiState.popularMovies,
                    isLoading = uiState.isLoadingPopular,
                    error = uiState.errorPopular,
                    onMovieClick = onNavigateToDetails,
                    onRetry = { viewModel.onRetrySection(MovieListType.POPULAR) },
                    modifier = Modifier.padding(top = 16.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Trending Movies Section
                TrendingSection(
                    title = "Trending Today",
                    movies = uiState.trendingMovies,
                    isLoading = uiState.isLoadingTrending,
                    error = uiState.errorTrending,
                    onMovieClick = onNavigateToDetails,
                    onRetry = { viewModel.onRetrySection(MovieListType.TRENDING) }
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Extension Popular Movies Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "From Your Extensions",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                    )
                    when {
                        uiState.isLoadingExtensionPopular -> {
                            Box(modifier = Modifier.fillMaxWidth().height(230.dp), contentAlignment = Alignment.Center) { // Approx height of card + padding
                                CircularProgressIndicator()
                            }
                        }
                        uiState.errorExtensionPopular != null -> {
                            Column(
                                modifier = Modifier.fillMaxWidth().height(230.dp).padding(horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Error: ${uiState.errorExtensionPopular}", color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.onRetryExtensionPopular() }) { Text("Retry Extensions") }
                            }
                        }
                        uiState.extensionPopularMovies.isEmpty() -> {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) { // Reduced height for empty message
                                Text("No movies found from enabled extensions.")
                            }
                        }
                        else -> {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.extensionPopularMovies, key = { "${it.id}_${it.title}" }) { movieItem ->
                                    ExtensionMovieCard(
                                        movieItem = movieItem,
                                        onClick = {
                                            // To navigate to details, need to know which extension this item belongs to,
                                            // if detail fetching is also via extension.
                                            // MovieSourceItem might need an 'extensionId' field.
                                            // For now, just passing item id.
                                            viewModel.onExtensionMovieItemClick(movieItem) // For VM logic
                                            onNavigateToExtensionMovieDetails(movieItem.id, null /* Placeholder for extensionId */)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))


                // Dynamic Category Sections (from main repository)
                // Example for "Action"
                val actionCategoryDisplayName = "Action Packed"
                if (uiState.categoryMovies.containsKey("Action") || uiState.isLoadingCategory && uiState.categoryMovies.isEmpty()) {
                     CategorySection(
                        categoryTitle = actionCategoryDisplayName,
                        movies = uiState.categoryMovies["Action"] ?: emptyList(),
                        // For categories, isLoadingCategory and errorCategory are general.
                        // A more granular approach would have per-category loading/error states.
                        isLoading = uiState.isLoadingCategory && (uiState.categoryMovies["Action"] == null || uiState.categoryMovies["Action"]!!.isEmpty()),
                        error = if (uiState.categoryMovies["Action"] == null && !uiState.isLoadingCategory) uiState.errorCategory else null,
                        onMovieClick = onNavigateToDetails,
                        onViewMoreClick = { onNavigateToCategoryList("action_movies_placeholder_id", actionCategoryDisplayName) },
                        onRetry = { viewModel.onRetrySection(MovieListType.BY_CATEGORY, "action_movies_placeholder_id", actionCategoryDisplayName) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Preview requires providing fake UseCases to the ViewModel for meaningful preview.
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview_WithExtensionSection() {
    val fakeGetMoviesUseCase = GetMoviesUseCase(FakeMovieRepository())
    val fakeGetExtensionMoviesUseCase : GetExtensionMoviesUseCase = mockk {
        coEvery { getPopularMovies(any()) } returns com.halibiram.tomato.core.common.result.Result.Success(
            listOf(
                com.halibiram.tomato.feature.extensions.api.MovieSourceItem("e1", "Ext Movie 1", null, "2023"),
                com.halibiram.tomato.feature.extensions.api.MovieSourceItem("e2", "Ext Movie 2", null, "2024")
            )
        )
    }
    val previewViewModel = HomeViewModel(fakeGetMoviesUseCase, fakeGetExtensionMoviesUseCase)

    TomatoTheme {
        HomeScreen(
            viewModel = previewViewModel,
            onNavigateToDetails = {},
            onNavigateToExtensionMovieDetails = { _, _ -> },
            onNavigateToCategoryList = { _, _ -> }
        )
    }
}

// A simple FakeRepository for previewing HomeScreen (from previous step)
class FakeMovieRepository : com.halibiram.tomato.domain.repository.MovieRepository {
    override fun getPopularMovies(page: Int) = flowOf(com.halibiram.tomato.core.common.result.Result.Success(listOf(Movie("p1", "Popular Preview Movie", "", null, "2023", emptyList(), 7.7))))
    override fun getTrendingMovies(timeWindow: String) = flowOf(com.halibiram.tomato.core.common.result.Result.Success(listOf(Movie("t1", "Trending Preview Movie", "", null, "2023", emptyList(), 8.0))))
    override fun getMoviesByCategory(categoryId: String) = flowOf(com.halibiram.tomato.core.common.result.Result.Success(emptyList<Movie>()))
    override fun getMovieDetails(movieId: String) = flowOf(com.halibiram.tomato.core.common.result.Result.Success(null))
    override fun searchMovies(query: String, page: Int) = flowOf(com.halibiram.tomato.core.common.result.Result.Success(emptyList<Movie>()))
}
