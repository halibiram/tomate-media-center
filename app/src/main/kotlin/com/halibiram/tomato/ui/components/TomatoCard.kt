package com.halibiram.tomato.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.halibiram.tomato.domain.model.Movie // Import the domain model
import com.halibiram.tomato.ui.theme.TomatoTheme

@Composable
fun TomatoCard(
    modifier: Modifier = Modifier,
    movie: Movie, // Accept a Movie object
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .width(150.dp) // Common width for movie posters in a row
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.posterUrl) // Use movie's posterUrl
                    .crossfade(true)
                    // .placeholder(R.drawable.placeholder_poster) // Optional: if you have a placeholder drawable
                    // .error(R.drawable.error_poster) // Optional: if you have an error drawable
                    .build(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop, // Crop to fit the aspect ratio
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f) // Typical poster aspect ratio (e.g., 150dp width / 225dp height)
                    .background(MaterialTheme.colorScheme.surfaceVariant) // Placeholder color
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            movie.releaseDate?.let {
                 Text(
                    text = it.split("-").firstOrNull() ?: "", // Display only year from YYYY-MM-DD
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
             Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TomatoCardPreview() {
    val sampleMovie = Movie(
        id = "1",
        title = "Awesome Movie Title That Is Quite Long",
        description = "This is a great movie.",
        posterUrl = "https://example.com/poster.jpg", // Replace with a real image URL for preview if internet is on
        releaseDate = "2023-10-26",
        genres = listOf("Action", "Adventure"),
        rating = 8.5
    )
    TomatoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TomatoCard(movie = sampleMovie)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TomatoCardPreview_NoPoster() {
    val sampleMovieNoPoster = Movie(
        id = "2",
        title = "Movie Without Poster",
        description = "Description here.",
        posterUrl = null, // No poster
        releaseDate = "2022-01-15",
        genres = listOf("Drama"),
        rating = 7.0
    )
    TomatoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TomatoCard(movie = sampleMovieNoPoster)
        }
    }
}
