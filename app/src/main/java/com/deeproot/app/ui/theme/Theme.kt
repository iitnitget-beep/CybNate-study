package com.deeproot.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = RootGreen,
    onPrimary = Color.White,
    primaryContainer = RootSurfaceHigh,
    onPrimaryContainer = RootGreenBright,
    secondary = LeafGold,
    onSecondary = Color(0xFF1A1305),
    background = RootDark,
    onBackground = Color(0xFFE8F0EC),
    surface = RootSurface,
    onSurface = Color(0xFFE8F0EC),
    surfaceVariant = RootSurfaceHigh,
    onSurfaceVariant = BarkGray,
    error = WarnClay,
    outline = Color(0xFF2A3A34)
)

private val LightColors = lightColorScheme(
    primary = RootGreen,
    onPrimary = Color.White,
    background = Color(0xFFF6F9F7),
    surface = Color.White,
    onBackground = Color(0xFF14201B),
    onSurface = Color(0xFF14201B),
    secondary = LeafGold
)

@Composable
fun DeepRootTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
