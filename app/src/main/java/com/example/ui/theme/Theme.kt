package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Cybertron Bumblebee Dark Theme:
 * - Pure Cybertron Armor deep space black & gunmetal chassis
 * - Radiant Autobot Bumblebee Yellow primary accents (#FFB800)
 * - Energon Neon Blue secondary accents (#00E5FF)
 * - Laser Red alerts & Emerald Green targeting lock
 */
val BumblebeeDarkColorScheme = darkColorScheme(
    primary = BumblebeeYellowBright,
    onPrimary = CybertronObsidian,
    primaryContainer = BumblebeeYellowMuted,
    onPrimaryContainer = Color(0xFFFEF3C7),

    secondary = EnergonBlueBright,
    onSecondary = CybertronObsidian,
    secondaryContainer = Color(0xFF0369A1),
    onSecondaryContainer = Color(0xFFE0F2FE),

    tertiary = MarksPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF581C87),
    onTertiaryContainer = Color(0xFFF3E8FF),

    background = CybertronObsidian,
    surface = CybertronArmor,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),

    surfaceVariant = CybertronCard,
    onSurfaceVariant = Color(0xFFCBD5E1),
    surfaceTint = BumblebeeYellow,

    inverseSurface = Color(0xFFF1F5F9),
    inverseOnSurface = CybertronObsidian,

    outline = CybertronSteel,
    outlineVariant = CybertronBorder,

    error = MarksError,
    onError = Color.White
)

/**
 * Bumblebee High-Contrast Light Theme:
 * - Bright and polished yellow & Autobot stinger black accents
 * - Clean titanium chassis with high legibility
 */
val BumblebeeLightColorScheme = lightColorScheme(
    primary = Color(0xFFD97706), // Rich deep Amber/Yellow for AA readability on white
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),

    secondary = Color(0xFF0284C7), // Energon Blue readable on light
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),

    tertiary = Color(0xFF7E22CE),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF3E8FF),
    onTertiaryContainer = Color(0xFF581C87),

    background = BumblebeeLightBg,
    surface = BumblebeeLightSurface,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),

    surfaceVariant = BumblebeeLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF334155),
    surfaceTint = BumblebeeYellowDark,

    inverseSurface = CybertronObsidian,
    inverseOnSurface = Color(0xFFF8FAFC),

    outline = BumblebeeLightBorder,
    outlineVariant = BumblebeeLightOutline,

    error = MarksError,
    onError = Color.White
)

// Legacy alias references for backward compatibility
val DarkColorScheme = BumblebeeDarkColorScheme
val LightColorScheme = BumblebeeLightColorScheme

/**
 * Calculates whether the dark theme should be active given the current ThemeMode and system setting.
 */
@Composable
fun isDarkThemeActive(themeMode: ThemeMode): Boolean {
    return when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}

@Composable
fun MarksTheme(
    darkTheme: Boolean? = null,
    dynamicColor: Boolean? = null,
    content: @Composable () -> Unit
) {
    val themeMode by ThemePreferences.themeMode.collectAsState()
    val prefsDynamicColor by ThemePreferences.dynamicColor.collectAsState()

    // Determine dark mode: either explicitly supplied parameter or computed from ThemePreferences
    val useDarkTheme = darkTheme ?: isDarkThemeActive(themeMode)
    val useDynamicColor = dynamicColor ?: prefsDynamicColor

    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> BumblebeeDarkColorScheme
        else -> BumblebeeLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
