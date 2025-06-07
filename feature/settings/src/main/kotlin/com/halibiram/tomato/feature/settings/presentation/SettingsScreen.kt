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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halibiram.tomato.core.datastore.preferences.AppThemePreference // Import from correct path
import com.halibiram.tomato.feature.settings.presentation.component.DialogSettingItem
import com.halibiram.tomato.feature.settings.presentation.component.SettingItem
import com.halibiram.tomato.feature.settings.presentation.component.SwitchSettingItem
import com.halibiram.tomato.ui.theme.TomatoTheme // Ensure this path is correct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit, // Example for navigation
    onNavigateToAccount: () -> Unit // Example for navigation
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val appPrefs = uiState.appPrefs
    val playerPrefs = uiState.playerPrefs
    val userPrefs = uiState.userPrefs

    var showThemeDialog by remember { mutableStateOf(false) }
    var showSubtitleLangDialog by remember { mutableStateOf(false) }
    // Add other dialog states as needed, e.g., for preferred resolution, seek increment

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
        if (uiState.isLoading && (appPrefs == AppPreferencesData() && playerPrefs == PlayerPreferencesData() && userPrefs == UserPreferencesData())) { // Show loading only on initial empty load
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp), contentAlignment = Alignment.Center) {
                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { /* viewModel.retryLoadSettings() */ viewModel.clearSettingsError() }) { Text("Dismiss") } // Or Retry
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Appearance Section
                SectionTitle("Appearance")
                DialogSettingItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    currentValue = appPrefs.appTheme.name.replace('_', ' ').capitalize(),
                    onClick = { showThemeDialog = true }
                )
                // Example for Theme Color - assuming it's a string for now
                // DialogSettingItem(
                //     icon = Icons.Default.ColorLens,
                //     title = "Theme Color",
                //     currentValue = appPrefs.selectedThemeColor ?: "Default",
                //     onClick = { /* TODO: Show theme color picker dialog */ }
                // )

                // Player Settings Section
                SectionTitle("Player")
                SwitchSettingItem(
                    icon = Icons.Default.SkipNext,
                    title = "Auto-play next episode",
                    summary = if (playerPrefs.autoPlayNext) "Enabled" else "Disabled",
                    checked = playerPrefs.autoPlayNext,
                    onCheckedChanged = { viewModel.updateAutoPlayNext(it) }
                )
                DialogSettingItem(
                    icon = Icons.Default.ClosedCaption,
                    title = "Default Subtitle Language",
                    currentValue = Locale(playerPrefs.defaultSubtitleLanguage).displayName,
                    onClick = { showSubtitleLangDialog = true }
                )
                // Example: Preferred Resolution
                // DialogSettingItem(
                //     icon = Icons.Default.SettingsEthernet, // Placeholder icon
                //     title = "Preferred Resolution",
                //     currentValue = playerPrefs.preferredResolution,
                //     onClick = { /* TODO: Show resolution selection dialog */ }
                // )
                // Example: Seek Increment
                // DialogSettingItem(
                //     icon = Icons.Default.FastForward,
                //     title = "Seek Increment",
                //     currentValue = "${playerPrefs.seekIncrementSeconds} seconds",
                //     onClick = { /* TODO: Show seek increment dialog */ }
                // )


                // Data & Sync Section
                SectionTitle("Data & Synchronization")
                SwitchSettingItem(
                    icon = Icons.Default.DataSaverOn,
                    title = "Data Saver Mode",
                    summary = if (appPrefs.dataSaverMode) "Reduce data usage" else "Disabled",
                    checked = appPrefs.dataSaverMode,
                    onCheckedChanged = { viewModel.updateDataSaverMode(it) }
                )
                SettingItem( // Example of a setting that might just display info or navigate
                    icon = Icons.Default.Sync,
                    title = "Last Synced",
                    summary = appPrefs.lastSyncTimestamp?.let { "At: ${Date(it).toLocaleString()}" } ?: "Never",
                    onClick = { viewModel.updateLastSyncTimestamp(System.currentTimeMillis()) } // Example action: force sync
                )

                // Account Section
                SectionTitle("Account")
                SettingItem(
                    icon = Icons.Default.AccountCircle,
                    title = userPrefs.username ?: "Account Details",
                    summary = if(userPrefs.isLoggedIn) userPrefs.email ?: "Manage your account" else "Not logged in",
                    onClick = onNavigateToAccount // Navigate to a dedicated account screen
                )
                if (userPrefs.isLoggedIn) {
                    SettingItem(
                        icon = Icons.Default.Logout,
                        title = "Logout",
                        onClick = { viewModel.logoutUser() /* TODO: Handle navigation post-logout */ }
                    )
                }


                // About Section
                SectionTitle("About")
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "About Tomato",
                    summary = "Version, licenses, and more",
                    onClick = onNavigateToAbout
                )

                Spacer(Modifier.height(32.dp)) // Add some space at the bottom
            }
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog( // Assuming this was defined in previous SettingsScreen or common place
            currentTheme = appPrefs.appTheme,
            onThemeSelected = { theme ->
                viewModel.updateAppTheme(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showSubtitleLangDialog) {
        // Example: Placeholder for subtitle language selection dialog
        ListSelectionDialog(
            title = "Select Subtitle Language",
            items = listOf("en", "es", "fr", "de").map { Locale(it).displayName to it }, // Display Name to Code
            selectedItemCode = playerPrefs.defaultSubtitleLanguage,
            onItemSelected = { code -> viewModel.updateDefaultSubtitleLanguage(code) },
            onDismiss = { showSubtitleLangDialog = false }
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) // M3 Divider
}

// Re-usable ListSelectionDialog (example)
@Composable
fun ListSelectionDialog(
    title: String,
    items: List<Pair<String, String>>, // List of (Display Name, Value Code)
    selectedItemCode: String,
    onItemSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn {
                items(items) { (displayName, code) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onItemSelected(code)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (code == selectedItemCode),
                            onClick = {
                                onItemSelected(code)
                                onDismiss()
                            }
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


// Preview
@Preview(showBackground = true, name = "Settings Screen Light")
@Composable
fun SettingsScreenPreview_Light() {
    val mockViewModel: SettingsViewModel = mockk(relaxed = true)
    every { mockViewModel.uiState } returns MutableStateFlow(
        SettingsUiState(isLoading = false)
    )
    TomatoTheme(darkTheme = false) {
        Surface {
            SettingsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onNavigateToAbout = {},
                onNavigateToAccount = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Settings Screen Dark")
@Composable
fun SettingsScreenPreview_Dark() {
     val mockViewModel: SettingsViewModel = mockk(relaxed = true)
    every { mockViewModel.uiState } returns MutableStateFlow(
        SettingsUiState(
            isLoading = false,
            appPrefs = AppPreferencesData(appTheme = AppThemePreference.DARK, dataSaverMode = true, selectedThemeColor = null, lastSyncTimestamp = System.currentTimeMillis()),
            playerPrefs = PlayerPreferencesData(defaultSubtitleLanguage = "es", preferredResolution = "1080p", autoPlayNext = false, seekIncrementSeconds = 15, playbackSpeed = 1.25f),
            userPrefs = UserPreferencesData(userId = "123", isLoggedIn = true, authToken = "token", username = "TomatoUser")
        )
    )
    TomatoTheme(darkTheme = true) {
         Surface {
            SettingsScreen(
                viewModel = mockViewModel,
                onNavigateBack = {},
                onNavigateToAbout = {},
                onNavigateToAccount = {}
            )
        }
    }
}

// Re-using ThemeSelectionDialog from previous version of SettingsScreen.kt
@Composable
fun ThemeSelectionDialog(
    currentTheme: AppThemePreference,
    onThemeSelected: (AppThemePreference) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme") },
        text = {
            Column {
                AppThemePreference.values().forEach { theme ->
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
