package com.halibiram.tomato.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.halibiram.tomato.ui.theme.TomatoTheme

@Composable
fun TomatoButton(onClick: () -> Unit, modifier: Modifier = Modifier, text: String) {
    Button(onClick = onClick, modifier = modifier) {
        Text(text = text)
    }
}

@Preview(showBackground = true)
@Composable
fun TomatoButtonPreview() {
    TomatoTheme {
        TomatoButton(onClick = {}, text = "Tomato Button")
    }
}
