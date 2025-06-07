package com.halibiram.tomato.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.halibiram.tomato.ui.theme.TomatoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TomatoTopBar(modifier: Modifier = Modifier, title: String) {
    TopAppBar(
        title = { Text(text = title) },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun TomatoTopBarPreview() {
    TomatoTheme {
        TomatoTopBar(title = "Tomato TopBar")
    }
}
