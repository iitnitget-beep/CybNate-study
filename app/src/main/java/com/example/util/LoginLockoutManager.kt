package com.example.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages login attempts and enforces a 24-hour lockout if 5 consecutive failed attempts occur.
 */
object LoginLockoutManager {
    private const val PREFS_NAME = "marks_auth_lockout_prefs"
    private const val KEY_FAILED_ATTEMPTS = "failed_attempts_count"
    private const val KEY_LOCKOUT_TIMESTAMP = "lockout_timestamp_ms"
    private const val MAX_ATTEMPTS = 5
    private const val LOCKOUT_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours in milliseconds

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Checks if the user is currently locked out.
     * Returns a pair of (isLockedOut, remainingHoursOrMinutesString).
     */
    fun checkLockoutStatus(context: Context): Pair<Boolean, String> {
        val prefs = getPrefs(context)
        val lockoutTime = prefs.getLong(KEY_LOCKOUT_TIMESTAMP, 0L)
        if (lockoutTime <= 0L) {
            return Pair(false, "")
        }

        val elapsed = System.currentTimeMillis() - lockoutTime
        if (elapsed < LOCKOUT_DURATION_MS) {
            val remainingMs = LOCKOUT_DURATION_MS - elapsed
            val remainingHours = remainingMs / (1000 * 60 * 60)
            val remainingMinutes = (remainingMs / (1000 * 60)) % 60
            val timeStr = if (remainingHours > 0) {
                "${remainingHours}h ${remainingMinutes}m"
            } else {
                "${remainingMinutes}m"
            }
            return Pair(true, "Account temporarily locked due to 5 failed attempts. Please try again in $timeStr.")
        } else {
            // Lockout expired - reset
            resetAttempts(context)
            return Pair(false, "")
        }
    }

    /**
     * Records a failed login attempt. If attempts reach 5, activates 24-hour lockout.
     * Returns remaining attempts or message.
     */
    fun recordFailedAttempt(context: Context): String {
        val prefs = getPrefs(context)
        val currentFailed = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
        if (currentFailed >= MAX_ATTEMPTS) {
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, currentFailed)
                .putLong(KEY_LOCKOUT_TIMESTAMP, System.currentTimeMillis())
                .apply()
            return "Too many failed attempts (5/5). Your access is locked for 24 hours."
        } else {
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, currentFailed)
                .apply()
            val remaining = MAX_ATTEMPTS - currentFailed
            return "Incorrect credentials. $remaining attempt${if (remaining == 1) "" else "s"} remaining before a 24-hour lockout."
        }
    }

    /**
     * Resets failed attempts after a successful login.
     */
    fun resetAttempts(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_FAILED_ATTEMPTS)
            .remove(KEY_LOCKOUT_TIMESTAMP)
            .apply()
    }

    fun getFailedAttemptsCount(context: Context): Int {
        return getPrefs(context).getInt(KEY_FAILED_ATTEMPTS, 0)
    }
}
