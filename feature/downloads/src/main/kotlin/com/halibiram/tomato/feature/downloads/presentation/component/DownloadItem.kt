package com.halibiram.tomato.feature.downloads.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// import coil.compose.AsyncImage // If using Coil
// import coil.request.ImageRequest // If using Coil
import com.halibiram.tomato.core.database.entity.DownloadEntity // For status constants
import com.halibiram.tomato.feature.downloads.presentation.UiDownloadItem
import com.halibiram.tomato.ui.theme.TomatoTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DownloadItem(
    item: UiDownloadItem,
    onItemClick: (mediaId: String, mediaType: String, status: String) -> Unit,
    onCancelClick: (mediaId: String) -> Unit,
    onPauseClick: (mediaId: String) -> Unit,
    onResumeClick: (mediaId: String) -> Unit,
    onDeleteClick: (mediaId: String, filePath: String?) -> Unit, // filePath might be part of UiDownloadItem or fetched
    onRetryClick: (mediaId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(item.mediaId, item.mediaType, item.downloadStatus) }
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
            //     modifier = Modifier.size(70.dp, 100.dp).clip(MaterialTheme.shapes.medium)
            // )
            Box(
                modifier = Modifier
                    .size(70.dp, 100.dp)
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Status: ${item.downloadStatus}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.downloadStatus == DownloadEntity.STATUS_DOWNLOADING || item.downloadStatus == DownloadEntity.STATUS_PAUSED) {
                    LinearProgressIndicator(
                        progress = item.progressPercentage / 100f,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    Text(
                        text = "${formatBytes(item.downloadedSizeBytes)} / ${formatBytes(item.downloadSizeBytes)} (${item.progressPercentage}%)",
                        style = MaterialTheme.typography.labelMedium
                    )
                } else if (item.downloadStatus == DownloadEntity.STATUS_COMPLETED) {
                     Text(
                        text = "Size: ${formatBytes(item.downloadSizeBytes)}",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Text(
                    text = "Added: ${dateFormatter.format(item.addedDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp)) // Space before action icons

            // Action Buttons Column
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (item.downloadStatus) {
                    DownloadEntity.STATUS_DOWNLOADING -> {
                        IconButton(onClick = { onPauseClick(item.mediaId) }) {
                            Icon(Icons.Default.PauseCircle, contentDescription = "Pause Download")
                        }
                        IconButton(onClick = { onCancelClick(item.mediaId) }) {
                            Icon(Icons.Default.Cancel, contentDescription = "Cancel Download")
                        }
                    }
                    DownloadEntity.STATUS_PAUSED -> {
                        IconButton(onClick = { onResumeClick(item.mediaId) }) {
                            Icon(Icons.Default.PlayCircle, contentDescription = "Resume Download")
                        }
                        IconButton(onClick = { onCancelClick(item.mediaId) }) {
                            Icon(Icons.Default.Cancel, contentDescription = "Cancel Download")
                        }
                    }
                    DownloadEntity.STATUS_COMPLETED -> {
                        IconButton(onClick = { /* Implemented by onItemClick to play */ }) {
                             Icon(Icons.Default.PlayCircle, contentDescription = "Play Downloaded Item", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { onDeleteClick(item.mediaId, null /* TODO: pass file path */) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Download")
                        }
                    }
                    DownloadEntity.STATUS_FAILED -> {
                        IconButton(onClick = { onRetryClick(item.mediaId) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry Download")
                        }
                        IconButton(onClick = { onDeleteClick(item.mediaId, null) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Download")
                        }
                    }
                    DownloadEntity.STATUS_PENDING -> {
                         IconButton(onClick = { onCancelClick(item.mediaId) }) {
                            Icon(Icons.Default.Cancel, contentDescription = "Cancel Download")
                        }
                    }
                     // STATUS_CANCELLED could just show a delete button or be removed from list quickly
                    DownloadEntity.STATUS_CANCELLED -> {
                        IconButton(onClick = { onDeleteClick(item.mediaId, null) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove from list")
                        }
                    }
                }
            }
        }
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val k = bytes / 1024
    if (k < 1024) return "$k KB"
    val m = k / 1024
    if (m < 1024) return "$m MB"
    val g = m / 1024
    return "$g GB"
}

@Preview(showBackground = true)
@Composable
fun DownloadItemPreview_Downloading() {
    val item = UiDownloadItem("1", "Movie Title Downloading", null, 1000, 500, 50, DownloadEntity.STATUS_DOWNLOADING, Date(), "movie")
    TomatoTheme {
        DownloadItem(item, {}, {}, {}, {}, { _, _ -> }, {})
    }
}

@Preview(showBackground = true)
@Composable
fun DownloadItemPreview_Completed() {
    val item = UiDownloadItem("2", "Series Episode Completed", "path/to/poster.jpg", 1000, 1000, 100, DownloadEntity.STATUS_COMPLETED, Date(), "episode")
    TomatoTheme {
        DownloadItem(item, { _, _, _ -> }, {}, {}, {}, { _, _ -> }, {})
    }
}

@Preview(showBackground = true)
@Composable
fun DownloadItemPreview_Failed() {
    val item = UiDownloadItem("3", "Failed Movie", null, 1000, 0, 0, DownloadEntity.STATUS_FAILED, Date(), "movie")
    TomatoTheme {
        DownloadItem(item, { _, _, _ -> }, {}, {}, {}, { _, _ -> }, {})
    }
}

@Preview(showBackground = true)
@Composable
fun DownloadItemPreview_Paused() {
    val item = UiDownloadItem("4", "Paused Show Download", null, 2000, 800, 40, DownloadEntity.STATUS_PAUSED, Date(), "episode")
    TomatoTheme {
        DownloadItem(item, { _, _, _ -> }, {}, {}, {}, { _, _ -> }, {})
    }
}
