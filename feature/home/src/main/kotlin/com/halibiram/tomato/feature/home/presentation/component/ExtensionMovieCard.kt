package com.halibiram.tomato.feature.home.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource // For placeholder
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.halibiram.tomato.feature.extensions.api.MovieSourceItem
import com.halibiram.tomato.feature.home.R // Assuming R class is in feature.home for the placeholder
import com.halibiram.tomato.ui.theme.TomatoTheme


@Composable
fun ExtensionMovieCard(
    modifier: Modifier = Modifier,
    movieItem: MovieSourceItem,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .width(150.dp) // Consistent with TomatoCard
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movieItem.posterUrl)
                    .crossfade(true)
                    .placeholder(painterResource(id = R.drawable.ic_placeholder_image)) // Using the created placeholder
                    .error(painterResource(id = R.drawable.ic_placeholder_image)) // Fallback to placeholder on error
                    .build(),
                contentDescription = movieItem.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = movieItem.title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                maxLines = 2, // Allow two lines for potentially longer titles from extensions
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            movieItem.year?.let {
                if (it.isNotBlank()) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExtensionMovieCardPreview() {
    val sampleItem = MovieSourceItem(
        id = "ext_movie_1",
        title = "Movie From Extension Source Title Is Long",
        posterUrl = null, // Test placeholder
        year = "2024"
    )
    TomatoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ExtensionMovieCard(movieItem = sampleItem, onClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExtensionMovieCardWithPosterPreview() {
    val sampleItemWithPoster = MovieSourceItem(
        id = "ext_movie_2",
        title = "Another Extension Movie",
        posterUrl = "https://example.com/some_poster.jpg", // Will not load in preview
        year = "2021"
    )
    TomatoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ExtensionMovieCard(movieItem = sampleItemWithPoster, onClick = {})
        }
    }
}
