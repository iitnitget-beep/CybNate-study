package com.example.util

import android.content.Context
import android.content.SharedPreferences

data class UserSession(
    val isLoggedIn: Boolean,
    val userName: String,
    val userId: String,
    val isGuest: Boolean,
    val userEmail: String = "",
    val authMethod: String = "",
    val lastLoginTimestamp: Long = 0L
)

object UserSessionManager {
    private const val PREFS_NAME = "cybnate_user_session_prefs"
    private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
    private const val KEY_USER_NAME = "key_user_name"
    private const val KEY_USER_ID = "key_user_id"
    private const val KEY_IS_GUEST = "key_is_guest"
    private const val KEY_USER_EMAIL = "key_user_email"
    private const val KEY_AUTH_METHOD = "key_auth_method"
    private const val KEY_LAST_LOGIN = "key_last_login_timestamp"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(
        context: Context,
        userName: String,
        isGuest: Boolean,
        userId: String,
        userEmail: String = "",
        authMethod: String = ""
    ) {
        val cleanName = userName.ifBlank { if (isGuest) "Guest Aspirant" else "Aspirant" }
        getPrefs(context).edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_NAME, cleanName)
            .putString(KEY_USER_ID, userId)
            .putBoolean(KEY_IS_GUEST, isGuest)
            .putString(KEY_USER_EMAIL, userEmail)
            .putString(KEY_AUTH_METHOD, authMethod)
            .putLong(KEY_LAST_LOGIN, System.currentTimeMillis())
            .apply()
    }

    fun getSession(context: Context): UserSession {
        val prefs = getPrefs(context)
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val userName = prefs.getString(KEY_USER_NAME, "") ?: ""
        val userId = prefs.getString(KEY_USER_ID, "") ?: ""
        val isGuest = prefs.getBoolean(KEY_IS_GUEST, false)
        val userEmail = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val authMethod = prefs.getString(KEY_AUTH_METHOD, "") ?: ""
        val lastLogin = prefs.getLong(KEY_LAST_LOGIN, 0L)
        return UserSession(
            isLoggedIn = isLoggedIn,
            userName = userName,
            userId = userId,
            isGuest = isGuest,
            userEmail = userEmail,
            authMethod = authMethod,
            lastLoginTimestamp = lastLogin
        )
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
