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

object ThemePreferences {
    private const val PREFS_NAME = "cybnate_theme_prefs"
    private const val KEY_THEME_MODE = "key_theme_mode"
    private const val KEY_DYNAMIC_COLOR = "key_dynamic_color"

    private lateinit var prefs: SharedPreferences

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _dynamicColor = MutableStateFlow(true)
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
            _dynamicColor.value = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        if (::prefs.isInitialized) {
            prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
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
