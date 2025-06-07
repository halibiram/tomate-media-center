package com.halibiram.tomato.feature.search.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// import coil.compose.AsyncImage // Uncomment if you have Coil dependency
// import coil.request.ImageRequest // Uncomment if you have Coil dependency
import com.halibiram.tomato.feature.search.presentation.SearchResult
import com.halibiram.tomato.ui.theme.TomatoTheme

@Composable
fun SearchResultItem(
    result: SearchResult,
    onItemClick: (id: String, type: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(result.id, result.type) }
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for Image - using a simple Box
            // If using Coil:
            // AsyncImage(
            //     model = ImageRequest.Builder(LocalContext.current)
            //         .data(result.imageUrl)
            //         .crossfade(true)
            //         .build(),
            //     contentDescription = result.title,
            //     contentScale = ContentScale.Crop,
            //     modifier = Modifier
            //         .size(80.dp, 120.dp) // Example size
            //         .clip(MaterialTheme.shapes.medium)
            // )
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(80.dp, 120.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .padding(4.dp) // Simulating image padding
                    .then(if (result.imageUrl != null) Modifier else Modifier.size(0.dp)) // Hide if no image
            ) {
                // This is where AsyncImage would go. For now, a simple Text placeholder if no image.
                if (result.imageUrl == null) {
                     Text("No Image", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                } else {
                    // In a real app, AsyncImage would handle loading here.
                    // For preview, we can imagine it's loaded.
                    Text(result.imageUrl, style = MaterialTheme.typography.labelSmall, color = Color.Gray) // Placeholder text
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = result.title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    // overflow = TextOverflow.Ellipsis // Requires androidx.compose.ui.text.style.TextOverflow
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Type: ${result.type}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchResultItemPreview() {
    val sampleResult = SearchResult(
        id = "123",
        title = "Sample Movie Title",
        description = "This is a sample description of the movie. It's quite an interesting film with a lot of action and drama.",
        imageUrl = "https://example.com/image.jpg", // Coil would load this
        type = "Movie"
    )
    TomatoTheme {
        SearchResultItem(result = sampleResult, onItemClick = { _, _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun SearchResultItemNoImagePreview() {
    val sampleResult = SearchResult(
        id = "456",
        title = "Sample Series Title Without Image",
        description = "Another example, this time a series, and it doesn't have an image URL provided.",
        imageUrl = null,
        type = "Series"
    )
    TomatoTheme {
        SearchResultItem(result = sampleResult, onItemClick = { _, _ -> })
    }
}
