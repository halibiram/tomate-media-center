package com.halibiram.tomato.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp), // Default was 4.dp, using 8.dp for a bit more rounded look
    large = RoundedCornerShape(12.dp) // Default was 0.dp, using 12.dp
)
