package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CybNate", appName)
  }

  @Test
  fun `user session persists and clears properly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.util.UserSessionManager.clearSession(context)
    val initialSession = com.example.util.UserSessionManager.getSession(context)
    org.junit.Assert.assertFalse(initialSession.isLoggedIn)

    // Save logged-in Google session
    com.example.util.UserSessionManager.saveSession(
      context = context,
      userName = "Atul Aspirant",
      isGuest = false,
      userId = "user_12345",
      userEmail = "aspirant@example.com",
      authMethod = "google"
    )

    val savedSession = com.example.util.UserSessionManager.getSession(context)
    org.junit.Assert.assertTrue(savedSession.isLoggedIn)
    assertEquals("Atul Aspirant", savedSession.userName)
    assertEquals("user_12345", savedSession.userId)
    org.junit.Assert.assertFalse(savedSession.isGuest)
    assertEquals("aspirant@example.com", savedSession.userEmail)

    // Clear session on sign out
    com.example.util.UserSessionManager.clearSession(context)
    val clearedSession = com.example.util.UserSessionManager.getSession(context)
    org.junit.Assert.assertFalse(clearedSession.isLoggedIn)
  }

  @Test
  fun `guest mode session persists across restarts`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.util.UserSessionManager.clearSession(context)

    com.example.util.UserSessionManager.saveSession(
      context = context,
      userName = "Guest Aspirant",
      isGuest = true,
      userId = "guest_9999",
      authMethod = "guest"
    )

    val guestSession = com.example.util.UserSessionManager.getSession(context)
    org.junit.Assert.assertTrue(guestSession.isLoggedIn)
    org.junit.Assert.assertTrue(guestSession.isGuest)
    assertEquals("Guest Aspirant", guestSession.userName)
  }
}
