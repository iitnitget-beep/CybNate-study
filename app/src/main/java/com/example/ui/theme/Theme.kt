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

// Metallic Chrome Theme ColorSchemes (Titanium & Brushed Steel)
val MetallicDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE2E8F0), // Polished Chrome Platinum
    onPrimary = Color(0xFF0F172A), // Deep Carbon Gunmetal
    primaryContainer = Color(0xFF334155), // Slate Titanium
    onPrimaryContainer = Color(0xFFF8FAFC),
    secondary = Color(0xFF94A3B8), // Brushed Silver
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = Color(0xFF64748B),
    onTertiary = Color.White,
    background = Color(0xFF0B0E14), // Deep Space Metallic Black
    surface = Color(0xFF161B22), // Titanium Alloy Slate
    surfaceVariant = Color(0xFF21262D), // Machined Steel Plate
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF334155),
    error = MarksError,
    onError = Color.White
)

val MetallicLightColorScheme = lightColorScheme(
    primary = Color(0xFF334155), // Slate Titanium
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = Color(0xFF475569),
    onSecondary = Color.White,
    background = Color(0xFFF1F5F9), // Silver Mist
    surface = Color(0xFFFFFFFF), // Polished White Platinum
    surfaceVariant = Color(0xFFE2E8F0),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1),
    error = MarksError,
    onError = Color.White
)

// Neon Cyberpunk ColorSchemes
val NeonCyberpunkDarkColorScheme = darkColorScheme(
    primary = Color(0xFFA855F7), // Neon Violet
    onPrimary = Color.White,
    primaryContainer = Color(0xFF581C87),
    onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = Color(0xFFEC4899), // Neon Fuchsia
    onSecondary = Color.White,
    background = Color(0xFF0A0714),
    surface = Color(0xFF151026),
    surfaceVariant = Color(0xFF22173C),
    onBackground = Color(0xFFF5F3FF),
    onSurface = Color(0xFFF5F3FF),
    onSurfaceVariant = Color(0xFFE9D5FF),
    outline = Color(0xFF8B5CF6),
    outlineVariant = Color(0xFF4C1D95),
    error = MarksError,
    onError = Color.White
)

val NeonCyberpunkLightColorScheme = lightColorScheme(
    primary = Color(0xFF7C3AED),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E8FF),
    onPrimaryContainer = Color(0xFF581C87),
    secondary = Color(0xFFDB2777),
    onSecondary = Color.White,
    background = Color(0xFFFAF5FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF3E8FF),
    onBackground = Color(0xFF1E1B4B),
    onSurface = Color(0xFF1E1B4B),
    onSurfaceVariant = Color(0xFF4C1D95),
    outline = Color(0xFFA855F7),
    outlineVariant = Color(0xFFDDD6FE),
    error = MarksError,
    onError = Color.White
)

// Aurora Emerald ColorSchemes
val AuroraEmeraldDarkColorScheme = darkColorScheme(
    primary = Color(0xFF10B981), // Bio Emerald
    onPrimary = Color(0xFF022C22),
    primaryContainer = Color(0xFF065F46),
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = Color(0xFF06B6D4), // Cyan
    onSecondary = Color(0xFF083344),
    background = Color(0xFF05110E),
    surface = Color(0xFF0C1F1A),
    surfaceVariant = Color(0xFF132F28),
    onBackground = Color(0xFFECFDF5),
    onSurface = Color(0xFFECFDF5),
    onSurfaceVariant = Color(0xFFA7F3D0),
    outline = Color(0xFF059669),
    outlineVariant = Color(0xFF064E3B),
    error = MarksError,
    onError = Color.White
)

val AuroraEmeraldLightColorScheme = lightColorScheme(
    primary = Color(0xFF059669),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = Color(0xFF0891B2),
    onSecondary = Color.White,
    background = Color(0xFFF0FDF4),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE6F4EA),
    onBackground = Color(0xFF064E3B),
    onSurface = Color(0xFF064E3B),
    onSurfaceVariant = Color(0xFF047857),
    outline = Color(0xFF10B981),
    outlineVariant = Color(0xFFA7F3D0),
    error = MarksError,
    onError = Color.White
)

// Deep Ocean ColorSchemes
val DeepOceanDarkColorScheme = darkColorScheme(
    primary = Color(0xFF0284C7), // Ocean Azure
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFF06B6D4), // Electric Teal
    onSecondary = Color.White,
    background = Color(0xFF050D1A),
    surface = Color(0xFF0B172B),
    surfaceVariant = Color(0xFF13233F),
    onBackground = Color(0xFFF0F9FF),
    onSurface = Color(0xFFF0F9FF),
    onSurfaceVariant = Color(0xFFBAE6FD),
    outline = Color(0xFF0284C7),
    outlineVariant = Color(0xFF075985),
    error = MarksError,
    onError = Color.White
)

val DeepOceanLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF0D9488),
    onSecondary = Color.White,
    background = Color(0xFFF0F9FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE0F2FE),
    onBackground = Color(0xFF082F49),
    onSurface = Color(0xFF082F49),
    onSurfaceVariant = Color(0xFF0369A1),
    outline = Color(0xFF0284C7),
    outlineVariant = Color(0xFF7DD3FC),
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
fun CybNateTheme(
    darkTheme: Boolean? = null,
    dynamicColor: Boolean? = null,
    content: @Composable () -> Unit
) = MarksTheme(darkTheme, dynamicColor, content)

@Composable
fun MarksTheme(
    darkTheme: Boolean? = null,
    dynamicColor: Boolean? = null,
    content: @Composable () -> Unit
) {
    val themeMode by ThemePreferences.themeMode.collectAsState()
    val themePreset by ThemePreferences.themePreset.collectAsState()
    val prefsDynamicColor by ThemePreferences.dynamicColor.collectAsState()

    // Determine dark mode: either explicitly supplied parameter or computed from ThemePreferences
    val useDarkTheme = darkTheme ?: isDarkThemeActive(themeMode)
    val useDynamicColor = dynamicColor ?: prefsDynamicColor

    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> when (themePreset) {
            AppThemePreset.CYBER_GOLD -> if (useDarkTheme) BumblebeeDarkColorScheme else BumblebeeLightColorScheme
            AppThemePreset.METALLIC_TITANIUM -> if (useDarkTheme) MetallicDarkColorScheme else MetallicLightColorScheme
            AppThemePreset.NEON_CYBERPUNK -> if (useDarkTheme) NeonCyberpunkDarkColorScheme else NeonCyberpunkLightColorScheme
            AppThemePreset.AURORA_EMERALD -> if (useDarkTheme) AuroraEmeraldDarkColorScheme else AuroraEmeraldLightColorScheme
            AppThemePreset.DEEP_OCEAN -> if (useDarkTheme) DeepOceanDarkColorScheme else DeepOceanLightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
