package com.halibiram.tomato.feature.extensions.presentation

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halibiram.tomato.feature.extensions.presentation.component.ExtensionItem
import com.halibiram.tomato.ui.theme.TomatoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionsScreen(
    // viewModel: ExtensionsViewModel = hiltViewModel(), // With Hilt
    viewModel: ExtensionsViewModel, // Pass for preview or non-Hilt
    onNavigateBack: () -> Unit,
    onNavigateToAddExtension: () -> Unit, // For adding new extensions from file/URL
    onNavigateToExtensionDetails: (extensionId: String) -> Unit // For viewing/configuring specific extension
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Extensions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAddExtension) {
                        Icon(Icons.Default.Add, contentDescription = "Add Extension")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
            } else if (uiState.extensions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No extensions installed.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onNavigateToAddExtension) {
                            Text("Add your first extension")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.extensions, key = { it.id }) { extension ->
                        ExtensionItem(
                            item = extension,
                            onItemClick = { onNavigateToExtensionDetails(extension.id) },
                            onToggleEnable = viewModel::toggleExtensionEnabled,
                            onUninstallClick = { viewModel.uninstallExtension(extension.id) } // Simplified
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
    val emptyVM = ExtensionsViewModel()
    TomatoTheme {
        ExtensionsScreen(
            viewModel = emptyVM,
            onNavigateBack = {},
            onNavigateToAddExtension = {},
            onNavigateToExtensionDetails = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExtensionsScreenPreview_WithItems() {
    val vmWithItems = ExtensionsViewModel().apply {
        // Simulate having items (ViewModel needs modification for easy preview state setting)
        // _uiState.value = ExtensionsUiState(
        //     isLoading = false,
        //     extensions = listOf(
        //         UiExtension("com.example.ext1", "Movie Source X", "1.2.0", 1, isEnabled = true, source = "Installed"),
        //         UiExtension("com.example.ext2", "Series Archive Y", "0.8.5", 1, isEnabled = false, source = "External")
        //     )
        // )
    }
    TomatoTheme {
        ExtensionsScreen(
            viewModel = vmWithItems,
            onNavigateBack = {},
            onNavigateToAddExtension = {},
            onNavigateToExtensionDetails = {}
        )
    }
}
