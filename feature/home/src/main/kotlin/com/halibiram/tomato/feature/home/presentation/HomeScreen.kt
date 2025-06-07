package com.halibiram.tomato.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halibiram.tomato.feature.home.presentation.component.CategorySection
import com.halibiram.tomato.feature.home.presentation.component.FeaturedSection
import com.halibiram.tomato.feature.home.presentation.component.TrendingSection
import com.halibiram.tomato.ui.components.TomatoTopBar
import com.halibiram.tomato.ui.theme.TomatoTheme

@Composable
fun HomeScreen(
    // viewModel: HomeViewModel = hiltViewModel(), // Example with Hilt
    viewModel: HomeViewModel, // Pass ViewModel directly for preview or non-Hilt setup
    onNavigateToDetails: (mediaId: String, mediaType: String) -> Unit,
    onNavigateToCategoryList: (categoryId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TomatoTopBar(title = "Tomato Home")
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Error: ${uiState.error}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.onRetry() }) {
                        Text("Retry")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 16.dp, // Add space below TopBar
                    bottom = paddingValues.calculateBottomPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (uiState.featuredItems.isNotEmpty()) {
                    item {
                        FeaturedSection(
                            items = uiState.featuredItems,
                            onItemClick = { itemId ->
                                viewModel.onFeaturedItemClick(itemId)
                                onNavigateToDetails(itemId, "featured") // Example mediaType
                            }
                        )
                    }
                }

                if (uiState.trendingItems.isNotEmpty()) {
                    item {
                        TrendingSection(
                            items = uiState.trendingItems,
                            onItemClick = { itemId ->
                                viewModel.onTrendingItemClick(itemId)
                                onNavigateToDetails(itemId, "trending") // Example mediaType
                            }
                        )
                    }
                }

                uiState.categories.forEach { (categoryName, items) ->
                    item {
                        CategorySection(
                            categoryName = categoryName,
                            items = items,
                            onItemClick = { itemId ->
                                viewModel.onCategoryItemClick(categoryName, itemId)
                                onNavigateToDetails(itemId, categoryName) // Example mediaType
                            },
                            onViewMoreClick = {
                                onNavigateToCategoryList(categoryName)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview_Loading() {
    val loadingViewModel = HomeViewModel().apply {
        // Manually set state for preview if ViewModel has complex dependencies
        // This is a simplified way. For complex ViewModels, use a fake/mock.
    }
    TomatoTheme {
        HomeScreen(
            viewModel = loadingViewModel,
            onNavigateToDetails = { _, _ -> },
            onNavigateToCategoryList = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview_Error() {
    val errorViewModel = HomeViewModel().apply {
        // Simulate error state
        // This direct state manipulation is for preview convenience.
        // In a real app, this would be managed by the ViewModel's logic.
        // _uiState.value = HomeUiState(isLoading = false, error = "Network request failed")
    }
    // For preview, we'd need to find a way to set the state of the preview HomeViewModel
    // to show error. This is tricky without Hilt/DI in previews for complex ViewModels.
    // A simpler ViewModel or a fake implementation is better for previews.
    // Let's assume the default state of the preview HomeViewModel shows loading,
    // or we can modify the ViewModel to take initial state for previews.

    // For this placeholder, we'll just show the screen with a generic HomeViewModel.
    // To actually see the error state, you'd run the app or have a more sophisticated preview setup.
    TomatoTheme {
        // This preview will likely show the loading state or initial empty state
        // unless HomeViewModel is modified to accept initial state for previews.
        HomeScreen(
            viewModel = HomeViewModel(), // A new instance for preview
            onNavigateToDetails = { _, _ -> },
            onNavigateToCategoryList = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview_Data() {
    val dataViewModel = HomeViewModel().apply {
        // Simulate loaded data state for preview
        // Similar to error state, this is for preview convenience.
        // _uiState.value = HomeUiState(
        // isLoading = false,
        // featuredItems = listOf("Preview Featured 1", "Preview Featured 2"),
        //     trendingItems = listOf("Preview Trending A", "Preview Trending B"),
        //     categories = mapOf("Preview Action" to listOf("Action 1", "Action 2"))
        // )
    }
    TomatoTheme {
        HomeScreen(
            viewModel = dataViewModel, // A new instance for preview
            onNavigateToDetails = { _, _ -> },
            onNavigateToCategoryList = {}
        )
    }
}
