package com.halibiram.tomato.feature.home.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halibiram.tomato.ui.components.TomatoCard
import com.halibiram.tomato.ui.theme.TomatoTheme

@Composable
fun CategorySection(
    categoryName: String,
    items: List<String>, // Replace String with your actual data model for category items
    onItemClick: (itemId: String) -> Unit,
    onViewMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = onViewMoreClick) {
                Text("View More")
            }
        }

        if (items.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items) { item ->
                    TomatoCard(
                        modifier = Modifier
                            .width(150.dp) // Example width for category items
                            .height(220.dp) // Example height
                            .clickable { onItemClick(item) } // Assuming item itself is the ID
                    ) {
                        // Content of the card
                        Text(text = item, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        } else {
            Text("No items in this category.")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategorySectionPreview() {
    TomatoTheme {
        CategorySection(
            categoryName = "Action",
            items = listOf("Action Movie 1", "Action Series A", "Action Movie 2"),
            onItemClick = {},
            onViewMoreClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategorySectionEmptyPreview() {
    TomatoTheme {
        CategorySection(
            categoryName = "Documentaries",
            items = emptyList(),
            onItemClick = {},
            onViewMoreClick = {}
        )
    }
}
