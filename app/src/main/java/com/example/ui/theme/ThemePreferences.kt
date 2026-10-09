package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppThemePreset(
    val title: String,
    val subtitle: String,
    val primaryHex: Long,
    val secondaryHex: Long
) {
    CYBER_GOLD(
        title = "Cyber Gold",
        subtitle = "Amber & Obsidian Chassis",
        primaryHex = 0xFFFFB800,
        secondaryHex = 0xFF00E5FF
    ),
    METALLIC_TITANIUM(
        title = "Metallic Chrome",
        subtitle = "Titanium & Brushed Steel",
        primaryHex = 0xFFCBD5E1,
        secondaryHex = 0xFF64748B
    ),
    NEON_CYBERPUNK(
        title = "Neon Cyberpunk",
        subtitle = "Fuchsia & Electric Cyan",
        primaryHex = 0xFFA855F7,
        secondaryHex = 0xFF06B6D4
    ),
    AURORA_EMERALD(
        title = "Aurora Emerald",
        subtitle = "Emerald & Mint Bio-Tech",
        primaryHex = 0xFF10B981,
        secondaryHex = 0xFF06B6D4
    ),
    DEEP_OCEAN(
        title = "Deep Ocean",
        subtitle = "Midnight Navy & Azure",
        primaryHex = 0xFF0284C7,
        secondaryHex = 0xFF06B6D4
    )
}

object ThemePreferences {
    private const val PREFS_NAME = "cybnate_theme_prefs"
    private const val KEY_THEME_MODE = "key_theme_mode"
    private const val KEY_THEME_PRESET = "key_theme_preset"
    private const val KEY_DYNAMIC_COLOR = "key_dynamic_color"

    private lateinit var prefs: SharedPreferences

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _themePreset = MutableStateFlow(AppThemePreset.CYBER_GOLD)
    val themePreset: StateFlow<AppThemePreset> = _themePreset.asStateFlow()

    private val _dynamicColor = MutableStateFlow(false)
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    fun init(context: Context) {
        if (!::prefs.isInitialized) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedModeName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
            _themeMode.value = try {
                ThemeMode.valueOf(savedModeName)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
            val savedPresetName = prefs.getString(KEY_THEME_PRESET, AppThemePreset.CYBER_GOLD.name) ?: AppThemePreset.CYBER_GOLD.name
            _themePreset.value = try {
                AppThemePreset.valueOf(savedPresetName)
            } catch (e: Exception) {
                AppThemePreset.CYBER_GOLD
            }
            _dynamicColor.value = prefs.getBoolean(KEY_DYNAMIC_COLOR, false)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        if (::prefs.isInitialized) {
            prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        }
    }

    fun setThemePreset(preset: AppThemePreset) {
        _themePreset.value = preset
        if (::prefs.isInitialized) {
            prefs.edit().putString(KEY_THEME_PRESET, preset.name).apply()
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        _dynamicColor.value = enabled
        if (::prefs.isInitialized) {
            prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        }
    }

    fun toggleDarkMode(currentIsDark: Boolean) {
        // Quick toggle between Light and Dark
        if (currentIsDark) {
            setThemeMode(ThemeMode.LIGHT)
        } else {
            setThemeMode(ThemeMode.DARK)
        }
    }
}
