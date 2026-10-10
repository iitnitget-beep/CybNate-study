package com.example.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

object AuthManager {

    fun sendPhoneOtp(
        context: Context,
        phoneNumber: String,
        onCodeSent: (verificationId: String) -> Unit,
        onError: (String) -> Unit,
        onSuccess: (displayName: String?) -> Unit = {},
        scope: CoroutineScope = CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
    ) {
        val activity = context.findActivity() ?: return
        val (isLocked, lockMsg) = LoginLockoutManager.checkLockoutStatus(activity)
        if (isLocked) {
            onError(lockMsg)
            return
        }

        val formattedPhone = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieval
                scope.launch {
                    try {
                        val authResult = Firebase.auth.signInWithCredential(credential).await()
                        onSuccess(authResult.user?.phoneNumber ?: "User")
                    } catch (e: Exception) {
                        Log.e("Auth", "Phone auto-verification failed", e)
                        onError(e.localizedMessage ?: "Verification failed")
                    }
                }
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                Log.e("Auth", "Phone verification failed", e)
                val msg = e.localizedMessage ?: ""
                val friendly = when {
                    msg.contains("quota", true) || msg.contains("billing", true) ->
                        "SMS quota for today is used up. Try again later or use Google/Email sign-in."
                    msg.contains("too many", true) || msg.contains("throttl", true) ->
                        "Too many OTP requests. Please wait a few minutes and try again."
                    msg.contains("invalid phone", true) || msg.contains("phone number", true) ->
                        "That phone number is not accepted. Please check it and try again."
                    else -> "Failed to send OTP. Please try again in a moment."
                }
                onError(friendly)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(formattedPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun sendPhoneOtp(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (verificationId: String) -> Unit,
        onError: (String) -> Unit,
        onSuccess: (displayName: String?) -> Unit = {},
        scope: CoroutineScope = CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
    ) = sendPhoneOtp(
        context = activity as Context,
        phoneNumber = phoneNumber,
        onCodeSent = onCodeSent,
        onError = onError,
        onSuccess = onSuccess,
        scope = scope
    )

    fun verifyPhoneOtp(
        context: Context,
        verificationId: String,
        code: String,
        onSuccess: (displayName: String?) -> Unit,
        onError: (String) -> Unit,
        scope: CoroutineScope
    ) {
        val (isLocked, lockMsg) = LoginLockoutManager.checkLockoutStatus(context)
        if (isLocked) {
            onError(lockMsg)
            return
        }

        scope.launch {
            try {
                val credential = PhoneAuthProvider.getCredential(verificationId, code.trim())
                val result = Firebase.auth.signInWithCredential(credential).await()
                LoginLockoutManager.resetAttempts(context)
                onSuccess(result.user?.phoneNumber ?: "User")
            } catch (e: Exception) {
                Log.e("Auth", "Phone OTP sign in failed", e)
                val raw = e.localizedMessage ?: ""
                if (raw.contains("invalid code", true) || raw.contains("expired", true)) {
                    val lockWarning = LoginLockoutManager.recordFailedAttempt(context)
                    onError("Invalid or expired OTP. $lockWarning")
                } else if (raw.contains("too many", true)) {
                    onError("Too many wrong attempts. Resend a fresh OTP and try again.")
                } else {
                    val lockWarning = LoginLockoutManager.recordFailedAttempt(context)
                    onError("OTP verification failed. $lockWarning")
                }
            }
        }
    }

    fun signInWithEmail(
        context: Context,
        identifier: String,
        password: String,
        onSuccess: (displayName: String?) -> Unit,
        onError: (String) -> Unit,
        scope: CoroutineScope
    ) {
        val (isLocked, lockMsg) = LoginLockoutManager.checkLockoutStatus(context)
        if (isLocked) {
            onError(lockMsg)
            return
        }

        val formattedEmail = if (identifier.contains("@")) {
            identifier
        } else {
            "${identifier.lowercase().replace(" ", "")}@cybnate.internal"
        }

        scope.launch {
            try {
                val result = Firebase.auth.signInWithEmailAndPassword(formattedEmail, password).await()
                LoginLockoutManager.resetAttempts(context)
                val resolvedName = result.user?.displayName?.ifBlank { null }
                    ?: identifier.substringBefore("@").replaceFirstChar { it.uppercase() }
                UserSessionManager.saveSession(
                    context = context,
                    userName = resolvedName,
                    isGuest = false,
                    userId = result.user?.uid ?: "user_${System.currentTimeMillis()}",
                    userEmail = result.user?.email ?: formattedEmail,
                    authMethod = "email"
                )
                onSuccess(resolvedName)
            } catch (e: FirebaseAuthInvalidUserException) {
                // Enumeration-safe: identical message for missing accounts & wrong passwords
                Log.w("Auth", "Email sign-in failed: account-not-found")
                val warning = LoginLockoutManager.recordFailedAttempt(context)
                onError("Invalid email/username or password. $warning")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Log.w("Auth", "Email sign-in failed: bad-credentials")
                val warning = LoginLockoutManager.recordFailedAttempt(context)
                onError("Invalid email/username or password. $warning")
            } catch (e: Exception) {
                Log.w("Auth", "Email sign-in failed: ${e.javaClass.simpleName}")
                val warning = LoginLockoutManager.recordFailedAttempt(context)
                onError("Sign-in failed. Please check your connection and try again. $warning")
            }
        }
    }

    fun signUpWithEmail(
        context: Context,
        identifier: String,
        password: String,
        displayName: String,
        onSuccess: (displayName: String?) -> Unit,
        onError: (String) -> Unit,
        scope: CoroutineScope
    ) {
        val (isLocked, lockMsg) = LoginLockoutManager.checkLockoutStatus(context)
        if (isLocked) {
            onError(lockMsg)
            return
        }

        if (password.length < 6) {
            onError("Password must be at least 6 characters long.")
            return
        }
        val formattedEmail = if (identifier.contains("@")) {
            identifier
        } else {
            "${identifier.lowercase().replace(" ", "")}@cybnate.internal"
        }

        val cleanName = displayName.ifBlank { identifier.substringBefore("@").replaceFirstChar { it.uppercase() } }

        scope.launch {
            try {
                val result = Firebase.auth.createUserWithEmailAndPassword(formattedEmail, password).await()
                try {
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(cleanName)
                        .build()
                    result.user?.updateProfile(profileUpdates)?.await()
                } catch (e: Exception) {
                    Log.w("Auth", "Failed to set display name: ${e.message}")
                }
                LoginLockoutManager.resetAttempts(context)
                UserSessionManager.saveSession(
                    context = context,
                    userName = cleanName,
                    isGuest = false,
                    userId = result.user?.uid ?: "user_${System.currentTimeMillis()}",
                    userEmail = formattedEmail,
                    authMethod = "email"
                )
                onSuccess(cleanName)
            } catch (e: FirebaseAuthUserCollisionException) {
                onError("An account with this email/username already exists. Please Sign In.")
            } catch (e: Exception) {
                Log.w("Auth", "Email sign-up failed: ${e.javaClass.simpleName}")
                onError("Could not create account. Please check your connection and try again.")
            }
        }
    }

    fun attemptAutoSignIn(
        context: Context,
        credentialManager: CredentialManager,
        onAuthSuccess: () -> Unit,
        onUnauthenticated: () -> Unit,
        scope: CoroutineScope
    ) {
        if (Firebase.auth.currentUser != null) {
            onAuthSuccess()
            return
        }
        val activity = context.findActivity() ?: return
        val clientId = try {
            activity.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onUnauthenticated()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    Firebase.auth.signInWithCredential(authCredential).await()
                    onAuthSuccess()
                } else {
                    onUnauthenticated()
                }
            } catch (e: Exception) {
                onUnauthenticated()
            }
        }
    }

    fun signInWithGoogle(
        context: Context,
        credentialManager: CredentialManager,
        onAuthSuccess: (displayName: String?) -> Unit,
        onAuthError: (String) -> Unit,
        scope: CoroutineScope,
        onAuthCancelled: () -> Unit = {}
    ) {
        val activity = context.findActivity() ?: return
        val clientId = try {
            activity.getString(R.string.default_web_client_id).trim()
        } catch (e: Exception) {
            ""
        }

        if (clientId.isBlank()) {
            onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                    val googleIdToken = googleCred.idToken
                    val googleDisplayName = googleCred.displayName ?: googleCred.id
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    
                    try {
                        Firebase.auth.signInWithCredential(authCredential).await()
                        onAuthSuccess(googleDisplayName)
                        return@launch
                    } catch (e: Exception) {
                        Log.e("Auth", "Firebase auth credential sign-in failed", e)
                        onAuthError("Google Sign-In failed: ${e.localizedMessage ?: "Authentication error"}")
                        return@launch
                    }
                } else {
                    onAuthError("Invalid credentials received")
                }
            } catch (e: GetCredentialCancellationException) {
                val msg = e.message ?: ""
                Log.w("Auth", "CredentialManager cancellation: $msg")
                onAuthCancelled()
            } catch (e: Exception) {
                Log.e("Auth", "CredentialManager exception", e)
                onAuthError("Authentication failed: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun signOut(
        context: Context,
        credentialManager: CredentialManager,
        onSignOutComplete: () -> Unit,
        scope: CoroutineScope
    ) {
        UserSessionManager.clearSession(context)
        Firebase.auth.signOut()
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("Auth", "Failed to clear credential state", e)
            } finally {
                onSignOutComplete()
            }
        }
    }
}
