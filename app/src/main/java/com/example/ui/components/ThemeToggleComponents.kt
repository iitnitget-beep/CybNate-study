package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MarksPrimary
import com.example.ui.theme.MarksSecondary
import com.example.ui.theme.MarksSuccess
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePreferences
import com.example.ui.theme.isDarkThemeActive

/**
 * An animated Quick Toggle IconButton for the top app bars.
 * Tapping toggles directly between Light and Dark mode.
 */
@Composable
fun ThemeToggleIconButton(
    modifier: Modifier = Modifier
) {
    val themeMode by ThemePreferences.themeMode.collectAsState()
    val isDark = isDarkThemeActive(themeMode)

    val rotation by animateFloatAsState(
        targetValue = if (isDark) 360f else 0f,
        label = "theme_icon_rotation"
    )

    IconButton(
        onClick = {
            ThemePreferences.toggleDarkMode(isDark)
        },
        modifier = modifier.testTag("theme_toggle_button")
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                tint = if (isDark) Color(0xFFFBBF24) else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotation)
            )
        }
    }
}

/**
 * A comprehensive Theme Selection Card for Settings and Profile screens.
 * Allows switching between System, Light, and Dark mode, plus toggling Android 12+ Dynamic Wallpaper Colors.
 */
@Composable
fun ThemeSettingsCard(
    modifier: Modifier = Modifier
) {
    val themeMode by ThemePreferences.themeMode.collectAsState()
    val dynamicColor by ThemePreferences.dynamicColor.collectAsState()
    val isDark = isDarkThemeActive(themeMode)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("theme_settings_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Theme & Appearance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (themeMode) {
                                ThemeMode.SYSTEM -> "Auto (Follows Device)"
                                ThemeMode.LIGHT -> "Always Light"
                                ThemeMode.DARK -> "Always Dark"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDark) Color(0xFF282D42) else Color(0xFFEEF2FF)
                ) {
                    Text(
                        text = if (isDark) "🌙 Dark" else "☀️ Light",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFE0E7FF) else MarksPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Mode Chips: System, Light, Dark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeModeOption(
                    title = "System",
                    icon = Icons.Default.SettingsBrightness,
                    selected = themeMode == ThemeMode.SYSTEM,
                    onClick = { ThemePreferences.setThemeMode(ThemeMode.SYSTEM) },
                    modifier = Modifier.weight(1f),
                    testTag = "theme_option_system"
                )
                ThemeModeOption(
                    title = "Light",
                    icon = Icons.Default.LightMode,
                    selected = themeMode == ThemeMode.LIGHT,
                    onClick = { ThemePreferences.setThemeMode(ThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f),
                    testTag = "theme_option_light"
                )
                ThemeModeOption(
                    title = "Dark",
                    icon = Icons.Default.DarkMode,
                    selected = themeMode == ThemeMode.DARK,
                    onClick = { ThemePreferences.setThemeMode(ThemeMode.DARK) },
                    modifier = Modifier.weight(1f),
                    testTag = "theme_option_dark"
                )
            }
        }
    }
}

@Composable
private fun ThemeModeOption(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
