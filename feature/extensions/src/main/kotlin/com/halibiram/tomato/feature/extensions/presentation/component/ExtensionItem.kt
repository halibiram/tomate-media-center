package com.halibiram.tomato.feature.extensions.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension // Default icon
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning // For error indication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.halibiram.tomato.domain.model.Extension // Domain model
import com.halibiram.tomato.ui.theme.TomatoTheme

@Composable
fun ExtensionItem(
    extension: Extension,
    onToggleEnabled: (id: String, currentIsEnabled: Boolean) -> Unit,
    onUninstall: (id: String) -> Unit,
    onSettingsClick: ((id: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showUninstallConfirmDialog by remember { mutableStateOf(false) }
    // var showMoreMenu by remember { mutableStateOf(false) } // Keep if more actions are added

    val itemAlpha = if (extension.isEnabled && extension.loadingError == null) 1f else 0.6f
    val titleColor = if (extension.loadingError != null) MaterialTheme.colorScheme.error else LocalContentColor.current


    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(itemAlpha),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (extension.loadingError != null) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)) else CardDefaults.cardColors()

    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (extension.iconUrl.isNullOrBlank()) {
                        Icon(
                            imageVector = if (extension.loadingError != null) Icons.Filled.Warning else Icons.Filled.Extension,
                            contentDescription = "${extension.name} icon",
                            modifier = Modifier.size(32.dp),
                            tint = if (extension.loadingError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(extension.iconUrl)
                                .crossfade(true)
                                .error(if (extension.loadingError != null) Icons.Filled.Warning else Icons.Filled.Extension)
                                .build(),
                            contentDescription = extension.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            colorFilter = if (!extension.isEnabled || extension.loadingError != null) ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(0f) }) else null
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = extension.name, style = MaterialTheme.typography.titleMedium, color = titleColor)
                    Text(
                        text = "v${extension.version} by ${extension.author ?: "Unknown author"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (extension.loadingError != null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (extension.loadingError == null) {
                        Text(
                            text = "API: ${extension.apiVersion} • Source: ${extension.source}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = extension.isEnabled,
                    onCheckedChange = { onToggleEnabled(extension.id, extension.isEnabled) },
                    modifier = Modifier.align(Alignment.Top),
                    enabled = extension.loadingError == null // Disable switch if extension itself had loading error
                )
            }

            extension.description?.let {
                if (extension.loadingError == null) { // Show description if no loading error
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            extension.loadingError?.let { errorMsg ->
                 Text(
                    text = "Error: $errorMsg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onSettingsClick != null && extension.loadingError == null) { // Hide settings if error
                    TextButton(onClick = { onSettingsClick(extension.id) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Settings")
                    }
                }
                TextButton(onClick = { showUninstallConfirmDialog = true }) {
                     Icon(Icons.Default.Delete, contentDescription = "Uninstall", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(ButtonDefaults.IconSize))
                     Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Uninstall", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showUninstallConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showUninstallConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = "Warning") },
            title = { Text("Uninstall Extension?") },
            text = { Text("Are you sure you want to uninstall '${extension.name}'? This action may delete associated data and cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUninstall(extension.id)
                        showUninstallConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Uninstall") }
            },
            dismissButton = {
                TextButton(onClick = { showUninstallConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}


@Preview(showBackground = true, widthDp = 360)
@Composable
fun ExtensionItemPreview_Enabled() {
    val item = Extension("com.example.ext1", "Movie Source X", "com.example.ext1", "1.2.0", "/some/uri", "Provides access to Movie Source X content.", true, null, 1, "Tomato Devs", "Store", "com.example.ext1.MainClass")
    TomatoTheme {
        ExtensionItem(item = item, onToggleEnabled = { _, _ -> }, onUninstall = {}, onSettingsClick = {})
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ExtensionItemPreview_Disabled() {
    val item = Extension("com.example.ext2", "Series Archive Y", "com.example.ext2", "0.8.5", null, "Old archive, currently disabled.", false, "/icon.png", 1, "Tomato Devs", "File", "com.example.ext2.MainClass")
    TomatoTheme {
        ExtensionItem(item = item, onToggleEnabled = { _, _ -> }, onUninstall = {})
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ExtensionItemPreview_LoadingError() {
    val item = Extension("com.example.ext3", "Broken Extension", "com.example.ext3", "1.0.0", null, "This extension failed to load properly.", true, null, 1, "Tomato Devs", "File", "com.example.ext3.MainClass", loadingError = "ClassNotFoundException: com.example.ext3.MainClass")
    TomatoTheme {
        ExtensionItem(item = item, onToggleEnabled = { _, _ -> }, onUninstall = {}, onSettingsClick = {})
    }
}
