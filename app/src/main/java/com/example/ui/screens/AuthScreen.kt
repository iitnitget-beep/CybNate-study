package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.ui.components.BumblebeeFaceLogo
import com.example.ui.components.GoogleColoredLogo
import com.example.ui.components.TermsAndConditionsDialog
import com.example.ui.theme.MarksError
import com.example.ui.theme.MarksPrimary
import com.example.ui.theme.MarksPrimaryDark
import com.example.ui.theme.MarksSecondary
import com.example.ui.theme.MarksSuccess
import com.example.util.AuthManager
import com.example.util.LoginLockoutManager
import com.example.util.findActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(
    onGoogleAuthSuccess: (displayName: String?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(false) }

    // 0 = Sign In, 1 = Sign Up
    var authModeTab by remember { mutableIntStateOf(0) }

    // Terms & Conditions acceptance state
    var acceptedTerms by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    // Unified input block for Gmail/Number
    var identifierInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }

    // Phone OTP state
    var phoneVerificationId by remember { mutableStateOf<String?>(null) }
    var otpInput by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    // Lockout state
    var isLockedOut by remember { mutableStateOf(false) }
    var lockoutMessage by remember { mutableStateOf("") }

    fun refreshLockout() {
        val (locked, msg) = LoginLockoutManager.checkLockoutStatus(context)
        isLockedOut = locked
        lockoutMessage = msg
    }

    LaunchedEffect(Unit) {
        refreshLockout()
    }

    // Input mode detection:
    // Starts typing numbers -> Send OTP option appears ONLY when 10 digits are entered
    // Starts typing alphabets / letters -> Enter Password appears
    val trimmedInput = identifierInput.trim()
    val digitsOnly = trimmedInput.filter { it.isDigit() }
    val isPhoneNumber = trimmedInput.isNotEmpty() && (trimmedInput.first().isDigit() || trimmedInput.startsWith("+"))
    val isAlphabetOrEmail = trimmedInput.isNotEmpty() && !isPhoneNumber

    // Strict Name validation (letters and spaces only, no numbers, no special characters)
    val isNameValid = nameInput.trim().length >= 2 && nameInput.trim().all { it.isLetter() || it.isWhitespace() }

    // Strict Password validation (min 8 chars, at least one number, at least one special character)
    val hasMin8Chars = passwordInput.length >= 8
    val hasNumber = passwordInput.any { it.isDigit() }
    val hasSpecialChar = passwordInput.any { !it.isLetterOrDigit() }
    val isPasswordValid = hasMin8Chars && hasNumber && hasSpecialChar

    // Gate: All input fields are disabled unless the user agrees to the Terms & Conditions
    val areInputsEnabled = acceptedTerms && !isLoading && !isLockedOut

    // Terms Dialog
    if (showTermsDialog) {
        TermsAndConditionsDialog(
            onDismiss = { showTermsDialog = false },
            onAccept = {
                acceptedTerms = true
                showTermsDialog = false
            }
        )
    }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    com.example.ui.components.LiquidShaderBackground(
        variant = com.example.ui.components.LiquidThemeVariant.CYBER_LIQUID,
        isDarkTheme = isDark
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hero Top Section (Liquid Glass Command Center)
                com.example.ui.components.LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    cornerRadius = 28.dp,
                    tintColor = MaterialTheme.colorScheme.surface,
                    frostAlpha = 0.65f,
                    borderGlowColor = com.example.ui.theme.BumblebeeYellow.copy(alpha = 0.45f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                com.example.ui.components.LiquidMorphingLogo(
                                    size = 64.dp,
                                    primaryColor = com.example.ui.theme.BumblebeeYellowBright,
                                    secondaryColor = com.example.ui.theme.EnergonBlueBright,
                                    accentColor = com.example.ui.theme.LiquidCoralNeon
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "CybNate",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Real Exam Test Series & Chapter-wise Analytics",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

            // Lockout Banner if user exceeded 5 attempts
            if (isLockedOut) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MarksError.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MarksError,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = lockoutMessage,
                            color = MarksError,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Main Auth Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Sign In / Sign Up Tabs
                    TabRow(
                        selectedTabIndex = authModeTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MarksPrimary
                    ) {
                        Tab(
                            selected = authModeTab == 0,
                            onClick = {
                                authModeTab = 0
                                isOtpSent = false
                                phoneVerificationId = null
                            },
                            text = { Text("Sign In", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = authModeTab == 1,
                            onClick = {
                                authModeTab = 1
                                isOtpSent = false
                                phoneVerificationId = null
                            },
                            text = { Text("Sign Up", fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mandatory Terms and Conditions Checkbox placed first at top
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (acceptedTerms) MarksPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { acceptedTerms = !acceptedTerms },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = acceptedTerms,
                                onCheckedChange = { acceptedTerms = it },
                                colors = CheckboxDefaults.colors(checkedColor = MarksPrimary),
                                modifier = Modifier.testTag("terms_checkbox")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "I agree to ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Terms & Conditions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MarksPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { showTermsDialog = true }
                                )
                            }
                        }
                    }

                    // Notice if user hasn't checked terms yet
                    if (!acceptedTerms) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MarksPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Please agree to Terms & Conditions above to unlock typing.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sign Up Full Name Field: Letters and spaces only (Disabled until terms checked)
                    if (authModeTab == 1) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { input ->
                                if (input.all { it.isLetter() || it.isWhitespace() }) {
                                    nameInput = input
                                }
                            },
                            label = { Text("Full Name (letters only)") },
                            placeholder = { Text(if (acceptedTerms) "Full Name" else "Agree to Terms first") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = if (areInputsEnabled) MarksPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            isError = nameInput.isNotBlank() && !isNameValid,
                            supportingText = {
                                if (nameInput.isNotBlank() && !isNameValid) {
                                    Text("Name must contain letters only (min 2 characters)", color = MarksError)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            enabled = areInputsEnabled
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Single Unified Gmail/Number Block (Disabled until terms checked)
                    OutlinedTextField(
                        value = identifierInput,
                        onValueChange = {
                            identifierInput = it
                            if (isOtpSent) {
                                isOtpSent = false
                                phoneVerificationId = null
                                otpInput = ""
                            }
                        },
                        label = { Text("Gmail / Number") },
                        placeholder = { Text(if (acceptedTerms) "Gmail / Number" else "Agree to Terms first to type") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isPhoneNumber) Icons.Default.Phone else Icons.Default.Email,
                                contentDescription = null,
                                tint = if (areInputsEnabled) MarksPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (isPhoneNumber) KeyboardType.Phone else KeyboardType.Email
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("identifier_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        enabled = areInputsEnabled && !isOtpSent
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // When typing alphabets / text -> Show "Enter Password"
                    AnimatedVisibility(visible = isAlphabetOrEmail) {
                        Column {
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Enter Password") },
                                placeholder = { Text("Enter Password") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = MarksPrimary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                isError = authModeTab == 1 && passwordInput.isNotBlank() && !isPasswordValid,
                                supportingText = {
                                    if (authModeTab == 1) {
                                        Text(
                                            text = "Min 8 chars, at least 1 number & 1 special character",
                                            color = if (isPasswordValid) MarksSuccess else if (passwordInput.isNotBlank()) MarksError else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("password_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                enabled = areInputsEnabled
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // When typing numbers -> Show Send OTP option ONLY when 10 digits are completed
                    AnimatedVisibility(visible = isPhoneNumber) {
                        Column {
                            if (!isOtpSent) {
                                if (digitsOnly.length >= 10) {
                                    Button(
                                        onClick = {
                                            if (!acceptedTerms) {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Please agree to Terms & Conditions first!")
                                                }
                                                return@Button
                                            }
                                            val currentActivity = context.findActivity()
                                            if (currentActivity == null) {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Unable to find Activity for phone verification")
                                                }
                                                return@Button
                                            }
                                            isLoading = true
                                            AuthManager.sendPhoneOtp(
                                                context = currentActivity,
                                                phoneNumber = identifierInput.trim(),
                                                onCodeSent = { vId ->
                                                    isLoading = false
                                                    phoneVerificationId = vId
                                                    isOtpSent = true
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar("OTP sent successfully to your number!")
                                                    }
                                                },
                                                onError = { err ->
                                                    isLoading = false
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(err)
                                                    }
                                                    refreshLockout()
                                                },
                                                onSuccess = { displayName ->
                                                    isLoading = false
                                                    onGoogleAuthSuccess(displayName)
                                                },
                                                scope = coroutineScope
                                            )
                                        },
                                        enabled = areInputsEnabled,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MarksSecondary),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("send_otp_button")
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                        } else {
                                            Text("Send OTP", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Enter 10-digit mobile number to send OTP (${digitsOnly.length}/10)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                OutlinedTextField(
                                    value = otpInput,
                                    onValueChange = { if (it.length <= 6) otpInput = it },
                                    label = { Text("Enter 6-Digit OTP") },
                                    placeholder = { Text("6-Digit OTP") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = MarksPrimary)
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("otp_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    enabled = areInputsEnabled
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "Change Number",
                                        color = MarksPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable {
                                            isOtpSent = false
                                            phoneVerificationId = null
                                            otpInput = ""
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Main Submit Button (Sign In / Sign Up / Verify OTP)
                    val canSubmit = when {
                        isLockedOut -> false
                        !acceptedTerms -> false
                        isPhoneNumber && isOtpSent -> otpInput.length == 6
                        isPhoneNumber && !isOtpSent -> false
                        isAlphabetOrEmail && authModeTab == 0 -> identifierInput.isNotBlank() && passwordInput.isNotBlank()
                        isAlphabetOrEmail && authModeTab == 1 -> identifierInput.isNotBlank() && isPasswordValid && isNameValid
                        else -> false
                    }

                    Button(
                        onClick = {
                            if (!acceptedTerms) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("You must agree to the Terms & Conditions before proceeding.")
                                }
                                return@Button
                            }

                            refreshLockout()
                            if (isLockedOut) return@Button

                            if (isPhoneNumber && isOtpSent) {
                                val vId = phoneVerificationId ?: return@Button
                                isLoading = true
                                AuthManager.verifyPhoneOtp(
                                    context = context,
                                    verificationId = vId,
                                    code = otpInput.trim(),
                                    onSuccess = { displayName ->
                                        isLoading = false
                                        onGoogleAuthSuccess(displayName)
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        refreshLockout()
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(err)
                                        }
                                    },
                                    scope = coroutineScope
                                )
                            } else if (isAlphabetOrEmail) {
                                isLoading = true
                                if (authModeTab == 0) {
                                    // Sign In
                                    AuthManager.signInWithEmail(
                                        context = context,
                                        identifier = identifierInput.trim(),
                                        password = passwordInput,
                                        onSuccess = { displayName ->
                                            isLoading = false
                                            LoginLockoutManager.resetAttempts(context)
                                            onGoogleAuthSuccess(displayName)
                                        },
                                        onError = { err ->
                                            isLoading = false
                                            refreshLockout()
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(err)
                                            }
                                        },
                                        scope = coroutineScope
                                    )
                                } else {
                                    // Create Account
                                    val finalDisplayName = nameInput.trim().ifBlank {
                                        identifierInput.trim().substringBefore("@")
                                    }
                                    AuthManager.signUpWithEmail(
                                        context = context,
                                        identifier = identifierInput.trim(),
                                        password = passwordInput,
                                        displayName = finalDisplayName,
                                        onSuccess = { displayName ->
                                            isLoading = false
                                            LoginLockoutManager.resetAttempts(context)
                                            onGoogleAuthSuccess(displayName)
                                        },
                                        onError = { err ->
                                            isLoading = false
                                            refreshLockout()
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(err)
                                            }
                                        },
                                        scope = coroutineScope
                                    )
                                }
                            }
                        },
                        enabled = canSubmit && !isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MarksPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                        } else {
                            val btnText = when {
                                isPhoneNumber && isOtpSent -> "Verify OTP & Continue"
                                authModeTab == 1 -> "Create Account"
                                else -> "Sign In"
                            }
                            Text(btnText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Divider with "OR"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = " OR ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Authentic Full-Color Google Sign-In Button
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = acceptedTerms && !isLoading && !isLockedOut) {
                                if (!acceptedTerms) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Please agree to the Terms & Conditions before signing in with Google.")
                                    }
                                    return@clickable
                                }

                                refreshLockout()
                                if (isLockedOut) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(lockoutMessage)
                                    }
                                    return@clickable
                                }
                                isLoading = true
                                AuthManager.signInWithGoogle(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = { displayName ->
                                        isLoading = false
                                        LoginLockoutManager.resetAttempts(context)
                                        onGoogleAuthSuccess(displayName)
                                    },
                                    onAuthError = { errorMsg ->
                                        isLoading = false
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(errorMsg)
                                        }
                                    },
                                    scope = coroutineScope,
                                    onAuthCancelled = {
                                        isLoading = false
                                    }
                                )
                            }
                            .testTag("google_sign_in_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (acceptedTerms) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (acceptedTerms) 2.dp else 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleColoredLogo(size = 22.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (acceptedTerms) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Guest / Offline Mode Button
                    Button(
                        onClick = {
                            if (!acceptedTerms) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please agree to the Terms & Conditions before continuing.")
                                }
                                return@Button
                            }
                            onGoogleAuthSuccess("Guest Aspirant")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("guest_bypass_button")
                    ) {
                        Text(
                            text = "Continue as Guest (Try Offline)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Value props footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureRow(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    title = "Realistic Test Engine",
                    desc = "Real test environment with countdown timer and question palette"
                )
                FeatureRow(
                    icon = Icons.Default.Analytics,
                    title = "In-depth Score Analytics",
                    desc = "Instant accuracy breakdown and comprehensive metrics"
                )
                FeatureRow(
                    icon = Icons.Default.CheckCircle,
                    title = "Step-by-Step Solutions",
                    desc = "Clear explanations with formula insights and bookmarks"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MarksPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MarksPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
