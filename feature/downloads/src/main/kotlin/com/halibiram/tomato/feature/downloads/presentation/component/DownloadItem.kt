package com.halibiram.tomato.feature.downloads.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Import all for convenience
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
// import androidx.compose.ui.layout.ContentScale // For AsyncImage if used
// import androidx.compose.ui.platform.LocalContext // For AsyncImage if used
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// import coil.compose.AsyncImage // If using Coil for poster
import com.halibiram.tomato.domain.model.Download
import com.halibiram.tomato.domain.model.DownloadStatus
import com.halibiram.tomato.ui.theme.TomatoTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DownloadItem(
    download: Download,
    onPauseClick: (String) -> Unit,
    onResumeClick: (Download) -> Unit,
    onCancelClick: (String) -> Unit,
    onDeleteClick: (Download) -> Unit, // Pass full Download object for context
    onPlayClick: (Download) -> Unit,
    onRetryClick: (Download) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = download.status == DownloadStatus.COMPLETED) {
                if (download.status == DownloadStatus.COMPLETED) onPlayClick(download)
            }
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Placeholder for Poster Image (using an Icon for now)
                Icon(
                    imageVector = if (download.mediaType == com.halibiram.tomato.domain.model.DownloadMediaType.MOVIE) Icons.Default.Movie else Icons.Default.Tv,
                    contentDescription = download.mediaType.name,
                    modifier = Modifier
                        .size(60.dp) // Smaller size for list item icon
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp), // Padding inside background
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // If using Coil for poster:
                // AsyncImage(
                // model = download.posterPath,
                // contentDescription = download.title,
                // contentScale = ContentScale.Crop,
                // modifier = Modifier.size(70.dp, 100.dp).clip(MaterialTheme.shapes.medium)
                // )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = download.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusAndProgress(download) // Extracted for clarity
                    Text(
                        text = "Added: ${dateFormatter.format(Date(download.addedDate))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            ActionButtons(download, onPauseClick, onResumeClick, onCancelClick, onDeleteClick, onPlayClick, onRetryClick)
        }
    }
}

@Composable
private fun StatusAndProgress(download: Download) {
    Column {
        Text(
            text = "Status: ${download.status.name.replace('_', ' ').lowercase().capitalize()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (download.status == DownloadStatus.DOWNLOADING || download.status == DownloadStatus.PAUSED) {
            LinearProgressIndicator(
                progress = { download.progress / 100f }, // For M3 LinearProgressIndicator
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${formatBytes(download.downloadedSizeBytes)} / ${formatBytes(download.totalSizeBytes)}",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "${download.progress}%",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        } else if (download.status == DownloadStatus.COMPLETED) {
            Text(
                text = "Size: ${formatBytes(download.totalSizeBytes)}",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ActionButtons(
    download: Download,
    onPause: (String) -> Unit,
    onResume: (Download) -> Unit,
    onCancel: (String) -> Unit,
    onDelete: (Download) -> Unit,
    onPlay: (Download) -> Unit,
    onRetry: (Download) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End, // Align buttons to the end
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (download.status) {
            DownloadStatus.DOWNLOADING -> {
                ActionButton("Pause", Icons.Default.Pause, onClick = { onPause(download.id) })
                ActionButton("Cancel", Icons.Default.Cancel, onClick = { onCancel(download.id) })
            }
            DownloadStatus.PAUSED -> {
                ActionButton("Resume", Icons.Default.PlayArrow, onClick = { onResume(download) })
                ActionButton("Cancel", Icons.Default.Cancel, onClick = { onCancel(download.id) })
            }
            DownloadStatus.COMPLETED -> {
                ActionButton("Play", Icons.Default.PlayCircleFilled, isPrimary = true, onClick = { onPlay(download) })
                ActionButton("Delete", Icons.Default.DeleteOutline, onClick = { onDelete(download) })
            }
            DownloadStatus.FAILED -> {
                ActionButton("Retry", Icons.Default.Refresh, onClick = { onRetry(download) })
                ActionButton("Delete", Icons.Default.DeleteOutline, onClick = { onDelete(download) })
            }
            DownloadStatus.PENDING -> {
                // Text("Queued...", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(end = 8.dp))
                ActionButton("Cancel", Icons.Default.Cancel, onClick = { onCancel(download.id) })
            }
            DownloadStatus.CANCELLED -> {
                ActionButton("Delete", Icons.Default.DeleteOutline, onClick = { onDelete(download) })
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Icon(icon, contentDescription = text, modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text, color = if (isPrimary) MaterialTheme.colorScheme.primary else LocalContentColor.current)
    }
}


fun formatBytes(bytes: Long): String {
    if (bytes < 0) return "0 B"
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.1f KB".format(kb)
    val mb = kb / 1024.0
    if (mb < 1024) return "%.1f MB".format(mb)
    val gb = mb / 1024.0
    return "%.1f GB".format(gb)
}

// --- Previews ---
@Preview(showBackground = true, widthDp = 360)
@Composable
fun DownloadItemPreview_Downloading() {
    val item = Download("d1", "m1", DownloadMediaType.MOVIE, "Movie Downloading", "url", DownloadStatus.DOWNLOADING, 50, null, 100000000, 50000000, System.currentTimeMillis())
    TomatoTheme { Surface { DownloadItem(item, {}, {}, {}, {}, {}, {}) } }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun DownloadItemPreview_Completed() {
    val item = Download("d2", "e1", DownloadMediaType.SERIES_EPISODE, "Episode Completed", "url", DownloadStatus.COMPLETED, 100, "/path/file.mp4", 20000000, 20000000, System.currentTimeMillis())
    TomatoTheme { Surface { DownloadItem(item, {}, {}, {}, {}, {}, {}) } }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun DownloadItemPreview_Failed() {
    val item = Download("d3", "m2", DownloadMediaType.MOVIE, "Movie Failed To Download Very Long Title Example", "url", DownloadStatus.FAILED, 0, null, 150000000, 0, System.currentTimeMillis())
    TomatoTheme { Surface { DownloadItem(item, {}, {}, {}, {}, {}, {}) } }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun DownloadItemPreview_Paused() {
    val item = Download("d4", "m3", DownloadMediaType.MOVIE, "Paused Download", "url", DownloadStatus.PAUSED, 25, null, 300000000, 75000000, System.currentTimeMillis())
    TomatoTheme { Surface { DownloadItem(item, {}, {}, {}, {}, {}, {}) } }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun DownloadItemPreview_Pending() {
    val item = Download("d5", "m4", DownloadMediaType.MOVIE, "Download Is Pending", "url", DownloadStatus.PENDING, 0, null, 0, 0, System.currentTimeMillis())
    TomatoTheme { Surface { DownloadItem(item, {}, {}, {}, {}, {}, {}) } }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun DownloadItemPreview_Cancelled() {
    val item = Download("d6", "m5", DownloadMediaType.MOVIE, "Cancelled Download", "url", DownloadStatus.CANCELLED, 30, null, 100, 30, System.currentTimeMillis())
    TomatoTheme { Surface { DownloadItem(item, {}, {}, {}, {}, {}, {}) } }
}
