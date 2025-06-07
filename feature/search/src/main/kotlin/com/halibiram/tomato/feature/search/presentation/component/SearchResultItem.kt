package com.halibiram.tomato.feature.search.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.halibiram.tomato.domain.model.Movie // Changed to use domain Movie model
import com.halibiram.tomato.ui.theme.TomatoTheme

@Composable
fun SearchResultItem(
    movie: Movie, // Changed from SearchResult to Movie
    onItemClick: (movieId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(movie.id) }
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.Top // Align items to the top for better text flow
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.posterUrl)
                    .crossfade(true)
                    // .placeholder(R.drawable.placeholder_poster) // Optional placeholder
                    // .error(R.drawable.error_poster) // Optional error placeholder
                    .build(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(90.dp) // Slightly wider for list item
                    .aspectRatio(2f / 3f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                movie.releaseDate?.let {
                    val year = it.split("-").firstOrNull() ?: ""
                    if (year.isNotEmpty()) {
                        Text(
                            text = "Year: $year",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Text(
                    text = movie.description, // Using description from Movie model
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Optionally, display rating or genres if available and desired
                // movie.rating?.let { Text("Rating: $it", style = MaterialTheme.typography.labelSmall) }
                // movie.genres?.take(2)?.joinToString()?.let { Text("Genres: $it", style = MaterialTheme.typography.labelSmall) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchResultItemPreview_Movie() {
    val sampleMovie = Movie(
        id = "123",
        title = "Sample Movie Title That Is Quite Long And Might Wrap",
        description = "This is a sample description of the movie. It's quite an interesting film with a lot of action, drama, and suspense that will keep you on the edge of your seat.",
        posterUrl = "https://example.com/image.jpg",
        releaseDate = "2023-10-26",
        genres = listOf("Action", "Adventure"),
        rating = 8.5
    )
    TomatoTheme {
        Box(modifier = Modifier.padding(8.dp)) {
            SearchResultItem(movie = sampleMovie, onItemClick = { _ -> })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchResultItemPreview_MovieNoPoster() {
    val sampleMovieNoPoster = Movie(
        id = "456",
        title = "Movie Without Poster",
        description = "Another example, this time a movie, and it doesn't have an image URL provided, but the description is still here to see how it renders.",
        posterUrl = null,
        releaseDate = "2022-01-15",
        genres = listOf("Drama"),
        rating = 7.0
    )
    TomatoTheme {
         Box(modifier = Modifier.padding(8.dp)) {
            SearchResultItem(movie = sampleMovieNoPoster, onItemClick = { _ -> })
        }
    }
}
