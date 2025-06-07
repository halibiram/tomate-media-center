package com.halibiram.tomato.feature.bookmarks.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// import coil.compose.AsyncImage // If using Coil
// import coil.request.ImageRequest // If using Coil
import com.halibiram.tomato.core.database.entity.BookmarkEntity
import com.halibiram.tomato.feature.bookmarks.presentation.UiBookmarkItem
import com.halibiram.tomato.ui.theme.TomatoTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BookmarkItem(
    item: UiBookmarkItem,
    onItemClick: (mediaId: String, mediaType: String) -> Unit,
    onRemoveClick: (mediaId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(item.mediaId, item.mediaType) }
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for Image
            // AsyncImage(
            //     model = ImageRequest.Builder(LocalContext.current).data(item.posterPath).crossfade(true).build(),
            //     contentDescription = item.title,
            //     contentScale = ContentScale.Crop,
            //     modifier = Modifier.size(80.dp, 120.dp).clip(MaterialTheme.shapes.medium) // Adjusted size
            // )
            Box(
                modifier = Modifier
                    .size(80.dp, 120.dp) // Standard poster aspect ratio
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                 if (item.posterPath == null) {
                    Text("No Image", modifier = Modifier.align(Alignment.Center), style = MaterialTheme.typography.labelSmall)
                }
                // Else, AsyncImage would be here
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title ?: "Unknown Title",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2, // Allow more lines for titles
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Type: ${item.mediaType.capitalize()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bookmarked: ${dateFormatter.format(item.bookmarkedDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp)) // Space before action icon

            IconButton(onClick = { onRemoveClick(item.mediaId) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove Bookmark",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkItemPreview_Movie() {
    val item = UiBookmarkItem(
        mediaId = "movie1",
        title = "The Best Movie Ever Made This Year",
        posterPath = "/path/to/poster.jpg",
        mediaType = BookmarkEntity.TYPE_MOVIE,
        bookmarkedDate = Date()
    )
    TomatoTheme {
        BookmarkItem(item = item, onItemClick = { _, _ -> }, onRemoveClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkItemPreview_Series() {
    val item = UiBookmarkItem(
        mediaId = "series1",
        title = "An Incredible Series About Everything",
        posterPath = null, // Test with no poster
        mediaType = BookmarkEntity.TYPE_SERIES,
        bookmarkedDate = Date(System.currentTimeMillis() - 100000000) // An older date
    )
    TomatoTheme {
        BookmarkItem(item = item, onItemClick = { _, _ -> }, onRemoveClick = {})
    }
}
