package com.halibiram.tomato.feature.home.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halibiram.tomato.ui.components.TomatoCard
import com.halibiram.tomato.ui.theme.TomatoTheme

@Composable
fun TrendingSection(
    items: List<String>, // Replace String with your actual data model for trending content
    onItemClick: (itemId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Trending Now",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (items.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items) { item ->
                    TomatoCard(
                        modifier = Modifier
                            .width(180.dp) // Example width for trending items
                            .height(250.dp) // Example height
                            .clickable { onItemClick(item) } // Assuming item itself is the ID
                    ) {
                        // Content of the card
                        Text(text = item, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        } else {
            Text("No trending content available at the moment.")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrendingSectionPreview() {
    TomatoTheme {
        TrendingSection(
            items = listOf("Trending Show 1", "Trending Movie X", "Trending Show 2"),
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrendingSectionEmptyPreview() {
    TomatoTheme {
        TrendingSection(
            items = emptyList(),
            onItemClick = {}
        )
    }
}
