package com.vincent.forbiddenseal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SealColors = darkColorScheme(
    primary = Color(0xFFD8BE76),
    onPrimary = Color(0xFF302B1B),
    primaryContainer = Color(0xFF4C4327),
    background = Color(0xFF121716),
    surface = Color(0xFF1A211F),
    surfaceVariant = Color(0xFF252E2B),
    onSurface = Color(0xFFE7ECE8),
    onSurfaceVariant = Color(0xFFB8C2BD),
    outline = Color(0xFF89938E),
)

@Composable
fun ForbiddenSealTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SealColors,
        content = content,
    )
}
