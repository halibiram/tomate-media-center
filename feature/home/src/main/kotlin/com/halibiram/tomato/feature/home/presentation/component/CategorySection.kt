package com.halibiram.tomato.feature.home.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button // Added for retry
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halibiram.tomato.domain.model.Movie
import com.halibiram.tomato.ui.components.TomatoCard // Assuming TomatoCard is in this path
import com.halibiram.tomato.ui.theme.TomatoTheme

@Composable
fun CategorySection(
    modifier: Modifier = Modifier,
    categoryTitle: String,
    movies: List<Movie>,
    isLoading: Boolean, // Specific loading state for this category's movies
    error: String?,     // Specific error state for this category's movies
    onMovieClick: (movieId: String) -> Unit,
    onViewMoreClick: () -> Unit, // Callback for "View More" action
    onRetry: (() -> Unit)? = null // Optional retry for this specific category
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp), // Adjusted padding for button
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = categoryTitle,
                style = MaterialTheme.typography.headlineSmall // Or titleLarge if preferred for categories
            )
            TextButton(onClick = onViewMoreClick) {
                Text("View More")
            }
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                 Column(
                    modifier = Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                    onRetry?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = it) { Text("Retry") }
                    }
                }
            }
            movies.isEmpty() -> {
                Box(modifier = Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                    Text("No movies found in '$categoryTitle'.")
                }
            }
            else -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(movies, key = { it.id }) { movie ->
                        TomatoCard(
                            movie = movie,
                            onClick = { onMovieClick(movie.id) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategorySectionPreview_Loaded() {
    val sampleMovies = listOf(
        Movie("5", "Action Movie 1", "Desc 5", null, "2023", null, 7.8),
        Movie("6", "Action Movie 2", "Desc 6", null, "2022", null, 6.5)
    )
    TomatoTheme {
        CategorySection(
            categoryTitle = "Action Thrillers",
            movies = sampleMovies,
            isLoading = false,
            error = null,
            onMovieClick = {},
            onViewMoreClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategorySectionPreview_Loading() {
    TomatoTheme {
        CategorySection(
            categoryTitle = "Comedy Movies",
            movies = emptyList(),
            isLoading = true,
            error = null,
            onMovieClick = {},
            onViewMoreClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategorySectionPreview_Error() {
    TomatoTheme {
        CategorySection(
            categoryTitle = "Sci-Fi Adventures",
            movies = emptyList(),
            isLoading = false,
            error = "Failed to load Sci-Fi movies",
            onMovieClick = {},
            onViewMoreClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategorySectionPreview_Empty() {
    TomatoTheme {
        CategorySection(
            categoryTitle = "Documentaries",
            movies = emptyList(),
            isLoading = false,
            error = null,
            onMovieClick = {},
            onViewMoreClick = {}
        )
    }
}
