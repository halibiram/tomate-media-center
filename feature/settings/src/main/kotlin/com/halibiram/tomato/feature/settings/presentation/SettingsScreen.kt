package com.halibiram.tomato.feature.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halibiram.tomato.core.datastore.preferences.AppTheme // Assuming this path
import com.halibiram.tomato.feature.settings.presentation.component.SettingItem
import com.halibiram.tomato.feature.settings.presentation.component.SettingControlType
import com.halibiram.tomato.ui.theme.TomatoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    // viewModel: SettingsViewModel = hiltViewModel(), // With Hilt
    viewModel: SettingsViewModel, // Pass for preview or non-Hilt
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit, // Example of a sub-navigation
    onNavigateToAccount: () -> Unit // Example
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings

    // For theme selection dialog
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Account Section
                SectionTitle("Account")
                SettingItem(
                    icon = Icons.Default.AccountCircle,
                    title = "Profile",
                    subtitle = settings.email ?: "Manage your account details",
                    onClick = onNavigateToAccount
                )

                // Appearance Section
                SectionTitle("Appearance")
                SettingItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = "Current: ${settings.appTheme.name.replace('_', ' ').capitalize()}",
                    controlType = SettingControlType.DROPDOWN, // Visually indicates it opens something
                    currentValue = settings.appTheme.name.replace('_', ' ').capitalize(),
                    onClick = { showThemeDialog = true }
                )

                // Player Settings Section
                SectionTitle("Player")
                SettingItem(
                    icon = Icons.Default.PlayCircleOutline,
                    title = "Auto-play next episode",
                    controlType = SettingControlType.SWITCH,
                    isChecked = settings.autoPlayNext,
                    onCheckedChange = viewModel::toggleAutoPlayNext
                )
                SettingItem(
                    icon = Icons.Default.Speed,
                    title = "Playback Speed",
                    controlType = SettingControlType.DROPDOWN, // Placeholder for actual dropdown
                    currentValue = "${settings.playbackSpeed}x",
                    onClick = { /* TODO: Show playback speed dialog */ }
                )
                SettingItem(
                    icon = Icons.Default.ClosedCaption,
                    title = "Preferred Subtitle Language",
                    controlType = SettingControlType.DROPDOWN, // Placeholder
                    currentValue = settings.preferredSubtitleLanguage.uppercase(),
                    onClick = { /* TODO: Show language selection dialog */ }
                )

                // General Section
                SectionTitle("General")
                SettingItem(
                    icon = Icons.Default.Notifications,
                    title = "Enable Notifications",
                    controlType = SettingControlType.SWITCH,
                    isChecked = settings.notificationsEnabled,
                    onCheckedChange = viewModel::toggleNotifications
                )
                SettingItem(
                    icon = Icons.Default.DataSaverOn,
                    title = "Data Saver Mode",
                    subtitle = "Reduce data usage while streaming on mobile network",
                    controlType = SettingControlType.SWITCH,
                    isChecked = settings.dataSaverMode,
                    onCheckedChange = viewModel::toggleDataSaver
                )
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "App version, licenses, and more",
                    onClick = onNavigateToAbout
                )

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        // viewModel.logoutUser()
                        // Potentially navigate after logout via an event from ViewModel
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Logout")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = settings.appTheme,
            onThemeSelected = { theme ->
                viewModel.updateAppTheme(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(top = 8.dp)
    )
    Divider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme") },
        text = {
            Column {
                AppTheme.values().forEach { theme ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = { onThemeSelected(theme) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(theme.name.replace('_', ' ').capitalize())
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val previewViewModel = SettingsViewModel() // Assumes default constructor or Hilt preview setup
    TomatoTheme {
        SettingsScreen(
            viewModel = previewViewModel,
            onNavigateBack = {},
            onNavigateToAbout = {},
            onNavigateToAccount = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview_Dark() {
    val previewViewModel = SettingsViewModel()
    TomatoTheme(darkTheme = true) {
        SettingsScreen(
            viewModel = previewViewModel,
            onNavigateBack = {},
            onNavigateToAbout = {},
            onNavigateToAccount = {}
        )
    }
}
