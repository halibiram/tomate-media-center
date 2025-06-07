package com.halibiram.tomato.feature.extensions.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halibiram.tomato.feature.extensions.presentation.UiExtension
import com.halibiram.tomato.ui.theme.TomatoTheme

@Composable
fun ExtensionItem(
    item: UiExtension,
    onItemClick: (extensionId: String) -> Unit, // For viewing details or settings
    onToggleEnable: (extensionId: String, currentIsEnabled: Boolean) -> Unit,
    onUninstallClick: ((extensionId: String) -> Unit)?, // Nullable if uninstall is not always available
    modifier: Modifier = Modifier
) {
    val showUninstallConfirmDialog = remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(item.id) }
            .padding(vertical = 4.dp)
            .alpha(if (item.isEnabled) 1f else 0.6f), // Visually indicate disabled state
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Extension, // Replace with actual extension icon if available from item.iconUrl
                contentDescription = "Extension Icon",
                modifier = Modifier.size(40.dp),
                tint = if (item.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Version: ${item.version} (API: ${item.apiVersion}) - Source: ${item.source}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                item.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        // overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = item.isEnabled,
                    onCheckedChange = { onToggleEnable(item.id, item.isEnabled) },
                    thumbContent = if (item.isEnabled) {
                        { Icon(Icons.Filled.Extension, contentDescription = "Enabled", Modifier.size(SwitchDefaults.IconSize)) }
                    } else null
                )
                if (onUninstallClick != null) {
                    IconButton(onClick = { showUninstallConfirmDialog.value = true }, enabled = true /* For APKs, uninstall is always possible */) {
                        Icon(Icons.Default.Delete, contentDescription = "Uninstall Extension", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (showUninstallConfirmDialog.value && onUninstallClick != null) {
        AlertDialog(
            onDismissRequest = { showUninstallConfirmDialog.value = false },
            title = { Text("Uninstall Extension") },
            text = { Text("Are you sure you want to uninstall '${item.name}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUninstallClick(item.id)
                        showUninstallConfirmDialog.value = false
                    }
                ) { Text("Uninstall", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showUninstallConfirmDialog.value = false }) { Text("Cancel") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExtensionItemPreview_Enabled() {
    val item = UiExtension("com.example.ext1", "Movie Source X", "1.2.0", 1, "Provides access to Movie Source X content.", null, true, "Installed")
    TomatoTheme {
        ExtensionItem(item = item, onItemClick = {}, onToggleEnable = { _, _ -> }, onUninstallClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ExtensionItemPreview_Disabled() {
    val item = UiExtension("com.example.ext2", "Series Archive Y", "0.8.5", 1, "Old archive, currently disabled.", null, false, "External")
    TomatoTheme {
        ExtensionItem(item = item, onItemClick = {}, onToggleEnable = { _, _ -> }, onUninstallClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ExtensionItemPreview_NoUninstall() {
    val item = UiExtension("com.example.ext3", "Built-in Utility", "1.0.0", 1, "Cannot be uninstalled.", null, true, "System")
    TomatoTheme {
        ExtensionItem(item = item, onItemClick = {}, onToggleEnable = { _, _ -> }, onUninstallClick = null)
    }
}
