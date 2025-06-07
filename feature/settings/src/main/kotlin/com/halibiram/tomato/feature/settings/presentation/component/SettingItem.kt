package com.halibiram.tomato.feature.settings.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halibiram.tomato.ui.theme.TomatoTheme

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String,
    summary: String? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val itemModifier = if (onClick != null && trailingContent == null) {
        modifier.clickable(onClick = onClick) // Make whole row clickable if no specific trailing interactive content
    } else {
        modifier
    }

    Row(
        modifier = itemModifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp), // Increased vertical padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null, // Decorative if title is present
                modifier = Modifier.padding(end = 16.dp).size(24.dp), // Consistent size
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            summary?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        // If trailingContent is provided, it takes precedence over the default chevron for onClick.
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(16.dp)) // Space before trailing content
            Box(contentAlignment = Alignment.CenterEnd) { // Align trailing content to the end
                trailingContent()
            }
        } else if (onClick != null) { // Show chevron only if onClick is present and no custom trailing content
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Navigate or select", // More descriptive
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    // Consider adding a Divider here or outside this composable for lists
    // Divider(modifier = Modifier.padding(start = if (icon != null) 56.dp else 16.dp))
}

@Composable
fun SwitchSettingItem(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String,
    summary: String?,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit
) {
    SettingItem(
        modifier = modifier,
        icon = icon,
        title = title,
        summary = summary,
        // Make the row itself clickable to toggle the switch for better UX
        // This is not standard Material behavior but often requested.
        // If only switch should be clickable, set onClick = null here.
        // For now, let's make the whole row toggle the switch.
        onClick = { onCheckedChanged(!checked) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChanged
            )
        }
    )
}

// Example for a DialogSettingItem structure (dialog itself not implemented here)
@Composable
fun DialogSettingItem(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String,
    currentValue: String?, // Display the current selected value
    onClick: () -> Unit // This onClick will trigger the dialog
) {
    SettingItem(
        modifier = modifier,
        icon = icon,
        title = title,
        summary = currentValue ?: "Not set", // Show current value as summary
        onClick = onClick, // Chevron will be shown by default if no trailingContent
        // If you want to show current value also at trailing end:
        // trailingContent = {
        //     Row(verticalAlignment = Alignment.CenterVertically) {
        //         currentValue?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary) }
        //         Spacer(Modifier.width(8.dp))
        //         Icon(Icons.Filled.ChevronRight, contentDescription = "Select", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        //     }
        // }
    )
}


// --- Previews ---

@Preview(showBackground = true)
@Composable
fun SettingItemPreview_Clickable() {
    TomatoTheme {
        SettingItem(
            icon = Icons.Default.Palette,
            title = "Appearance",
            summary = "Change theme and display options",
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SwitchSettingItemPreview() {
    varisChecked by remember { mutableStateOf(true) }
    TomatoTheme {
        SwitchSettingItem(
            icon = Icons.Default.Notifications,
            title = "Enable Notifications",
            summary = "Receive updates and alerts",
            checked = isChecked,
            onCheckedChanged = { isChecked = it }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DialogSettingItemPreview() {
    TomatoTheme {
        DialogSettingItem(
            icon = Icons.Default.Palette,
            title = "Theme Color",
            currentValue = "Blueberry",
            onClick = { /* Show dialog */ }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingItemPreview_NoIcon_NoSummary_NoTrailing() {
    TomatoTheme {
        SettingItem(
            title = "Advanced Settings",
            onClick = {} // Just a clickable item
        )
    }
}
