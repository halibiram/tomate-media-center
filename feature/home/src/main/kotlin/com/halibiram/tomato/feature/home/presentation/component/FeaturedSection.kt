package com.halibiram.tomato.feature.home.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halibiram.tomato.ui.components.TomatoCard
import com.halibiram.tomato.ui.theme.TomatoTheme

@Composable
fun FeaturedSection(
    items: List<String>, // Replace String with your actual data model for featured content
    onItemClick: (itemId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Featured",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // This is a placeholder. You'd typically use a LazyRow or a Pager for horizontal scrolling.
        // For simplicity, showing one item.
        if (items.isNotEmpty()) {
            val firstItem = items.first() // Show only the first item as a placeholder
            TomatoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Example height
                    .clickable { onItemClick(firstItem) } // Assuming item itself is the ID
            ) {
                // Content of the card, e.g., Text(firstItem)
                Text(text = "Featured: $firstItem", modifier = Modifier.padding(16.dp))
            }
        } else {
            Text("No featured content available.")
        }
        // If using LazyRow:
        // LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        //     items(items) { item ->
        //         TomatoCard(/*...*/) { Text(item) }
        //     }
        // }
    }
}

@Preview(showBackground = true)
@Composable
fun FeaturedSectionPreview() {
    TomatoTheme {
        FeaturedSection(
            items = listOf("Featured Movie Title 1", "Featured Show Title 2"),
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FeaturedSectionEmptyPreview() {
    TomatoTheme {
        FeaturedSection(
            items = emptyList(),
            onItemClick = {}
        )
    }
}
