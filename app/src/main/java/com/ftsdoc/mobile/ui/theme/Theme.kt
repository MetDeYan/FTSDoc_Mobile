package com.ftsdoc.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E7F2),
    onPrimaryContainer = NavyDark,
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5F0E4),
    onSecondaryContainer = Color(0xFF083324),
    background = Ice,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE2E9EF),
    onSurfaceVariant = Color(0xFF3E4C59),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9FCFE6),
    onPrimary = Color(0xFF00344A),
    primaryContainer = Color(0xFF0E4D6C),
    onPrimaryContainer = Color(0xFFD3E7F2),
    secondary = Color(0xFF8FD6B5),
    onSecondary = Color(0xFF003824),
    secondaryContainer = Color(0xFF0F6B4C),
    onSecondaryContainer = Color(0xFFD5F0E4),
    background = Color(0xFF101418),
    onBackground = Color(0xFFE6EBEF),
    surface = Color(0xFF1A2128),
    onSurface = Color(0xFFE6EBEF),
    surfaceVariant = Color(0xFF2A343E),
    onSurfaceVariant = Color(0xFFC2CED8),
    error = Color(0xFFF2B8B5),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun FtsDocTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
