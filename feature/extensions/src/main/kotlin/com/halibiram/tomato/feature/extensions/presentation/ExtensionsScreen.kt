package com.halibiram.tomato.feature.extensions.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.platform.LocalContext // Not directly needed now
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halibiram.tomato.domain.model.Extension
// import com.halibiram.tomato.feature.extensions.api.ExtensionManifest // No longer needed for Dummy
import com.halibiram.tomato.feature.extensions.presentation.component.ExtensionItem
import com.halibiram.tomato.ui.theme.TomatoTheme
import kotlinx.coroutines.launch

// DummyExtensionManifest is no longer needed here as the ViewModel and UseCase handle manifest loading (simulated).

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionsScreen(
    viewModel: ExtensionsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToExtensionSettings: ((extensionId: String) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    // val coroutineScope = rememberCoroutineScope() // Not used directly here anymore
    // val context = LocalContext.current // Not used directly here anymore

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                // ViewModel's installExtension now only needs the URI
                viewModel.installExtension(it.toString())
            }
        }
    )

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearUserMessages()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long, actionLabel = "Dismiss")
            viewModel.clearUserMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Manage Extensions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                pickFileLauncher.launch(arrayOf("application/vnd.android.package-archive", "*/*")) // APKs or any file
            }) {
                Icon(Icons.Default.Add, contentDescription = "Install New Extension")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading && uiState.extensions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else if (uiState.extensions.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("No extensions installed yet.", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap the '+' button to add a new extension.", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.extensions, key = { it.id }) { extension ->
                        ExtensionItem(
                            extension = extension,
                            onToggleEnabled = { id, isEnabled -> viewModel.toggleExtensionEnabled(id, isEnabled) },
                            onUninstall = { id -> viewModel.uninstallExtension(id) },
                            onSettingsClick = if (false /* TODO: Check if extension has settings */) {
                                { id -> onNavigateToExtensionSettings?.invoke(id) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExtensionsScreenPreview_Empty() {
    val mockViewModel: ExtensionsViewModel = mockk(relaxed = true)
    every { mockViewModel.uiState } returns MutableStateFlow(ExtensionsUiState(isLoading = false, extensions = emptyList()))
    TomatoTheme {
        ExtensionsScreen(
            viewModel = mockViewModel,
            onNavigateBack = {},
            onNavigateToExtensionSettings = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExtensionsScreenPreview_WithItems() {
     val mockViewModel: ExtensionsViewModel = mockk(relaxed = true)
    val sampleExtensions = listOf(
        Extension("com.example.ext1", "Movie Source X", "com.example.ext1", "1.2.0", "/some/uri", "Provides access to Movie Source X content.", true, null, 1, "Tomato Devs", "Store"),
        Extension("com.example.ext2", "Series Archive Y", "com.example.ext2", "0.8.5", null, "Old archive, currently disabled.", false, "/icon.png", 1, "Tomato Devs", "File")
    )
    every { mockViewModel.uiState } returns MutableStateFlow(ExtensionsUiState(isLoading = false, extensions = sampleExtensions))

    TomatoTheme {
        ExtensionsScreen(
            viewModel = mockViewModel,
            onNavigateBack = {},
            onNavigateToExtensionSettings = {}
        )
    }
}
