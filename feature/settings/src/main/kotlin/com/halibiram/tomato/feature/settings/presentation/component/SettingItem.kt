package com.halibiram.tomato.feature.settings.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halibiram.tomato.ui.theme.TomatoTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ToggleOn

enum class SettingControlType {
    NONE, // For clickable items that navigate or open dialogs
    SWITCH,
    DROPDOWN, // Placeholder, would need more complex implementation
    TEXT_VALUE // To display a current value
}

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    icon: ImageVector?,
    title: String,
    subtitle: String? = null,
    controlType: SettingControlType = SettingControlType.NONE,
    isChecked: Boolean = false, // For SWITCH type
    currentValue: String? = null, // For TEXT_VALUE or to supplement DROPDOWN
    onClick: (() -> Unit)? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null // For SWITCH type
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null && controlType != SettingControlType.SWITCH) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null, // Decorative
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            subtitle?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        when (controlType) {
            SettingControlType.SWITCH -> {
                onCheckedChange?.let {
                    Switch(
                        checked = isChecked,
                        onCheckedChange = it
                    )
                }
            }
            SettingControlType.TEXT_VALUE -> {
                currentValue?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                }
            }
            SettingControlType.NONE -> {
                // If onClick is defined, show a chevron or similar indicator for navigation items
                if (onClick != null) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Navigate",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            SettingControlType.DROPDOWN -> { // Simplified display for dropdown
                Row(verticalAlignment = Alignment.CenterVertically) {
                    currentValue?.let {
                        Text(text = it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(end = 4.dp))
                    }
                     if (onClick != null) { // Assume onClick opens the dropdown
                        Icon(
                            Icons.Default.ChevronRight, // Or a dropdown arrow icon
                            contentDescription = "Open selection",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    Divider(modifier = Modifier.padding(start = if (icon != null) 56.dp else 16.dp))
}

@Preview(showBackground = true)
@Composable
fun SettingItemPreview_Clickable() {
    TomatoTheme {
        SettingItem(
            icon = Icons.Default.Palette,
            title = "Appearance",
            subtitle = "Change theme and display options",
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingItemPreview_Switch() {
    TomatoTheme {
        SettingItem(
            icon = Icons.Default.Notifications,
            title = "Enable Notifications",
            subtitle = "Receive updates and alerts",
            controlType = SettingControlType.SWITCH,
            isChecked = true,
            onCheckedChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingItemPreview_TextValue() {
    TomatoTheme {
        SettingItem(
            icon = Icons.Default.ToggleOn, // Example icon
            title = "Current Plan",
            controlType = SettingControlType.TEXT_VALUE,
            currentValue = "Premium"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingItemPreview_DropdownPlaceholder() {
    TomatoTheme {
        SettingItem(
            icon = Icons.Default.Palette,
            title = "Theme",
            subtitle = "Select app theme",
            controlType = SettingControlType.DROPDOWN,
            currentValue = "System Default",
            onClick = {} // To simulate opening a dropdown
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingItemPreview_NoIcon() {
    TomatoTheme {
        SettingItem(
            icon = null,
            title = "About",
            subtitle = "Version and legal information",
            onClick = {}
        )
    }
}
