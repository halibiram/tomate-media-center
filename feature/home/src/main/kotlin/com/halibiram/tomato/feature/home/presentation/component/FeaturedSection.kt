package com.halibiram.tomato.feature.home.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halibiram.tomato.domain.model.Movie
import com.halibiram.tomato.ui.components.TomatoCard
import com.halibiram.tomato.ui.theme.TomatoTheme

@Composable
fun FeaturedSection(
    modifier: Modifier = Modifier,
    title: String,
    movies: List<Movie>,
    isLoading: Boolean,
    error: String?,
    onMovieClick: (movieId: String) -> Unit,
    onRetry: (() -> Unit)? = null // Optional retry callback
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        )

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
                    Text("No movies to display in this section.")
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
fun FeaturedSectionPreview_Loaded() {
    val sampleMovies = listOf(
        Movie("1", "Movie 1", "Desc 1", null, "2023", null, 7.0),
        Movie("2", "Movie 2", "Desc 2", null, "2022", null, 8.0)
    )
    TomatoTheme {
        FeaturedSection(
            title = "Popular Movies",
            movies = sampleMovies,
            isLoading = false,
            error = null,
            onMovieClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FeaturedSectionPreview_Loading() {
    TomatoTheme {
        FeaturedSection(
            title = "Popular Movies",
            movies = emptyList(),
            isLoading = true,
            error = null,
            onMovieClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FeaturedSectionPreview_Error() {
    TomatoTheme {
        FeaturedSection(
            title = "Popular Movies",
            movies = emptyList(),
            isLoading = false,
            error = "Network request failed",
            onMovieClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FeaturedSectionPreview_Empty() {
    TomatoTheme {
        FeaturedSection(
            title = "Popular Movies",
            movies = emptyList(),
            isLoading = false,
            error = null,
            onMovieClick = {}
        )
    }
}
