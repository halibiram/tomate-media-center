package com.halibiram.tomato.feature.bookmarks.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie // For Movie type
import androidx.compose.material.icons.filled.Tv // For Series type
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.halibiram.tomato.domain.model.Bookmark
import com.halibiram.tomato.domain.model.BookmarkMediaType
import com.halibiram.tomato.ui.theme.TomatoTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BookmarkItem(
    bookmark: Bookmark,
    onRemoveClick: (mediaId: String, mediaType: BookmarkMediaType) -> Unit,
    onItemClick: (bookmark: Bookmark) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(bookmark) }
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .aspectRatio(2f / 3f) // Poster aspect ratio
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (bookmark.posterUrl.isNullOrBlank()) {
                    Icon(
                        imageVector = when (bookmark.mediaType) {
                            BookmarkMediaType.MOVIE -> Icons.Filled.Movie
                            BookmarkMediaType.SERIES -> Icons.Filled.Tv
                        },
                        contentDescription = bookmark.mediaType.name,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(bookmark.posterUrl)
                            .crossfade(true)
                            // .placeholder(R.drawable.placeholder_poster) // Optional
                            // .error(R.drawable.error_poster) // Optional
                            .build(),
                        contentDescription = bookmark.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }


            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bookmark.title ?: "Unknown Title",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (bookmark.mediaType) {
                            BookmarkMediaType.MOVIE -> Icons.Filled.Movie
                            BookmarkMediaType.SERIES -> Icons.Filled.Tv
                        },
                        contentDescription = bookmark.mediaType.name,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = bookmark.mediaType.name.lowercase().replaceFirstChar { it.titlecase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bookmarked: ${dateFormatter.format(Date(bookmark.addedDate))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { onRemoveClick(bookmark.mediaId, bookmark.mediaType) }) {
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
    val item = Bookmark(
        mediaId = "movie1",
        title = "The Best Movie Ever Made This Year",
        posterUrl = "https://example.com/poster.jpg", // Will not load in preview without internet & Coil setup for preview
        mediaType = BookmarkMediaType.MOVIE,
        addedDate = System.currentTimeMillis()
    )
    TomatoTheme {
        Box(modifier=Modifier.padding(8.dp)) {
            BookmarkItem(item = item, onItemClick = { _ -> }, onRemoveClick = { _, _ -> })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkItemPreview_Series_NoPoster() {
    val item = Bookmark(
        mediaId = "series1",
        title = "An Incredible Series About Everything And More With A Very Long Title",
        posterUrl = null,
        mediaType = BookmarkMediaType.SERIES,
        addedDate = System.currentTimeMillis() - 100000000
    )
    TomatoTheme {
         Box(modifier=Modifier.padding(8.dp)) {
            BookmarkItem(item = item, onItemClick = { _ -> }, onRemoveClick = { _, _ -> })
        }
    }
}
