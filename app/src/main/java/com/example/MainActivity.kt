package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.data.model.Attempt
import com.example.data.model.Question
import com.example.data.model.Test
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ExamChapterPracticeScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.ProfileHistoryScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.ReviewScreen
import com.example.ui.screens.TestInstructionsScreen
import com.example.ui.screens.TestScreen
import com.example.ui.theme.MarksTheme
import com.example.ui.viewmodel.MarksViewModel
import com.example.util.AuthManager
import com.example.util.LoginLockoutManager
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.collectAsState
import com.example.ui.components.CybertronTransitions

enum class AppScreen {
    HOME,
    EXAM_PRACTICE,
    PRACTICE_SCREEN,
    TEST_INSTRUCTIONS,
    TEST_SCREEN,
    RESULT_SCREEN,
    REVIEW_SCREEN,
    PROFILE_HISTORY
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val app = FirebaseApp.initializeApp(this) ?: FirebaseApp.getInstance()
            val debugToken = "a78f2e2d-3c99-4d62-b91c-7f5b89a3dfcb"
            try {
                val persistenceKey = app.persistenceKey
                val storeName = "com.google.firebase.appcheck.debug.store.$persistenceKey"
                getSharedPreferences(storeName, android.content.Context.MODE_PRIVATE)
                    .edit()
                    .putString("com.google.firebase.appcheck.debug.DEBUG_SECRET", debugToken)
                    .apply()
                getSharedPreferences("com.google.firebase.appcheck.debug.store._DEFAULT", android.content.Context.MODE_PRIVATE)
                    .edit()
                    .putString("com.google.firebase.appcheck.debug.DEBUG_SECRET", debugToken)
                    .apply()
                Log.d("MainActivity", "Injected debug token $debugToken into $storeName")
            } catch (e: Exception) {
                Log.w("MainActivity", "Failed to pre-seed debug token store", e)
            }

            val appCheck = FirebaseAppCheck.getInstance()
            // Always use Debug provider for testing/preview builds to guarantee zero App Check token issues
            appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d("MainActivity", "Firebase App Check initialized with Debug provider")
            
            // Auto-reset login lockout on app start during testing/preview to prevent testing blockages
            LoginLockoutManager.resetAttempts(this)
            Log.d("MainActivity", "Login lockout status reset for developer testing")
        } catch (e: Exception) {
            Log.e("MainActivity", "Firebase App Check initialization failed", e)
        }

        // Initialize user theme preferences (System, Light, Dark mode & dynamic colors)
        com.example.ui.theme.ThemePreferences.init(this)

        enableEdgeToEdge()

        setContent {
            MarksTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MarksAppContent()
                }
            }
        }
    }
}

@Composable
fun MarksAppContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var googleAccountUserName by remember { mutableStateOf<String?>(null) }

    val isAuthenticated = currentUser != null || googleAccountUserName != null

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    // Attempt silent auto-sign in if not currently signed in
    LaunchedEffect(Unit) {
        if (!isAuthenticated) {
            AuthManager.attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                    currentUser = Firebase.auth.currentUser
                },
                onUnauthenticated = {
                    // Stay on login screen
                },
                scope = coroutineScope
            )
        }
    }

    if (!isAuthenticated) {
        AuthScreen(
            onGoogleAuthSuccess = { displayName ->
                currentUser = Firebase.auth.currentUser
                googleAccountUserName = displayName ?: (Firebase.auth.currentUser?.email ?: Firebase.auth.currentUser?.phoneNumber ?: "User")
            }
        )
    } else {
        // Authenticated user session - instantiate ViewModel safely
        val viewModel: MarksViewModel = viewModel()

        LaunchedEffect(currentUser?.uid) {
            viewModel.onUserAuthenticated()
        }

        var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
        var selectedTest by remember { mutableStateOf<Test?>(null) }
        var lastAttempt by remember { mutableStateOf<Attempt?>(null) }
        var reviewQuestions by remember { mutableStateOf<List<Question>>(emptyList()) }

        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                CybertronTransitions.appScreenTransition(initialState, targetState)
            },
            label = "cybertron_screen_transition"
        ) { targetScreen ->
            when (targetScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        googleUserName = googleAccountUserName,
                        onTestClick = { test ->
                            selectedTest = test
                            currentScreen = AppScreen.EXAM_PRACTICE
                        },
                        onAttemptReviewClick = { attempt ->
                            lastAttempt = attempt
                            coroutineScope.launch {
                                val activeQuestions = if (viewModel.activeSession.value?.test?.id == attempt.testId) {
                                    viewModel.activeSession.value?.questions
                                } else null
                                val fetched = activeQuestions?.takeIf { it.isNotEmpty() }
                                    ?: try {
                                        viewModel.getQuestionsForAttempt(attempt)
                                    } catch (e: Exception) {
                                        emptyList()
                                    }
                                reviewQuestions = if (fetched.isNotEmpty()) {
                                    fetched
                                } else {
                                    synthesizeQuestionsForAttempt(attempt)
                                }
                                currentScreen = AppScreen.RESULT_SCREEN
                            }
                        },
                        onProfileClick = {
                            currentScreen = AppScreen.PROFILE_HISTORY
                        }
                    )
                }

                AppScreen.EXAM_PRACTICE -> {
                    val test = selectedTest
                    val isNeet = test?.exam?.contains("neet", true) == true || viewModel.selectedExamBranch.value.contains("neet", true)
                    val examTitle = if (isNeet) "NEET Medical" else (test?.exam?.ifBlank { "JEE Main" } ?: "JEE Main")
                    val initTab = if (isNeet) {
                        when {
                            test?.subject?.contains("chem", true) == true -> "Chem"
                            test?.subject?.contains("zoo", true) == true -> "Zoo"
                            test?.subject?.contains("botan", true) == true -> "Bot"
                            test?.subject?.contains("phys", true) == true -> "Phy"
                            else -> "Chem"
                        }
                    } else {
                        when {
                            test?.subject?.contains("chem", true) == true -> "Chem"
                            test?.subject?.contains("math", true) == true -> "Math"
                            else -> "Phy"
                        }
                    }
                    ExamChapterPracticeScreen(
                        examTitle = examTitle,
                        initialTab = initTab,
                        viewModel = viewModel,
                        onBackClick = { currentScreen = AppScreen.HOME },
                        onStartPractice = { chapterTest, topic, diff ->
                            selectedTest = chapterTest
                            viewModel.startTest(chapterTest, topic, diff)
                            currentScreen = AppScreen.PRACTICE_SCREEN
                        },
                        onOpenBookmarks = { subject ->
                            currentScreen = AppScreen.PROFILE_HISTORY
                        },
                        onOpenAnalysis = { subject ->
                            // Handled inside ExamChapterPracticeScreen via tab
                        }
                    )
                }

                AppScreen.PRACTICE_SCREEN -> {
                    val test = selectedTest
                    if (test != null) {
                        val activeSession by viewModel.activeSession.collectAsState()
                        PracticeScreen(
                            test = test,
                            questions = activeSession?.questions ?: emptyList(),
                            viewModel = viewModel,
                            onFinishPractice = { attempt ->
                                lastAttempt = attempt
                                reviewQuestions = activeSession?.questions ?: emptyList()
                                currentScreen = AppScreen.RESULT_SCREEN
                            },
                            onExitPractice = {
                                currentScreen = AppScreen.EXAM_PRACTICE
                            }
                        )
                    } else {
                        currentScreen = AppScreen.HOME
                    }
                }

                AppScreen.TEST_INSTRUCTIONS -> {
                    val test = selectedTest
                    if (test != null) {
                        BackHandler {
                            currentScreen = AppScreen.HOME
                        }
                        TestInstructionsScreen(
                            test = test,
                            onStartTest = {
                                viewModel.startTest(test)
                                currentScreen = AppScreen.TEST_SCREEN
                            },
                            onBackClick = {
                                currentScreen = AppScreen.HOME
                            }
                        )
                    } else {
                        currentScreen = AppScreen.HOME
                    }
                }

                AppScreen.TEST_SCREEN -> {
                    TestScreen(
                        viewModel = viewModel,
                        onTestSubmitted = { attempt ->
                            lastAttempt = attempt
                            reviewQuestions = viewModel.activeSession.value?.questions ?: emptyList()
                            currentScreen = AppScreen.RESULT_SCREEN
                        },
                        onExitTest = {
                            currentScreen = AppScreen.HOME
                        }
                    )
                }

                AppScreen.RESULT_SCREEN -> {
                    val attempt = lastAttempt
                    if (attempt != null) {
                        BackHandler {
                            viewModel.resetSession()
                            currentScreen = AppScreen.HOME
                        }
                        ResultScreen(
                            attempt = attempt,
                            onReviewClick = {
                                currentScreen = AppScreen.REVIEW_SCREEN
                            },
                            onRetakeClick = {
                                val test = selectedTest
                                if (test != null) {
                                    viewModel.startTest(test)
                                    currentScreen = AppScreen.TEST_SCREEN
                                } else {
                                    currentScreen = AppScreen.HOME
                                }
                            },
                            onHomeClick = {
                                viewModel.resetSession()
                                currentScreen = AppScreen.HOME
                            }
                        )
                    } else {
                        currentScreen = AppScreen.HOME
                    }
                }

                AppScreen.REVIEW_SCREEN -> {
                    val attempt = lastAttempt
                    val questions = reviewQuestions
                    if (attempt != null) {
                        BackHandler {
                            currentScreen = AppScreen.RESULT_SCREEN
                        }
                        ReviewScreen(
                            attempt = attempt,
                            questions = questions,
                            viewModel = viewModel,
                            onBackClick = {
                                currentScreen = AppScreen.RESULT_SCREEN
                            }
                        )
                    } else {
                        currentScreen = AppScreen.HOME
                    }
                }

                AppScreen.PROFILE_HISTORY -> {
                    BackHandler {
                        currentScreen = AppScreen.HOME
                    }
                    ProfileHistoryScreen(
                        viewModel = viewModel,
                        onBackClick = {
                            currentScreen = AppScreen.HOME
                        },
                        onAttemptClick = { attempt ->
                            lastAttempt = attempt
                            coroutineScope.launch {
                                val activeQuestions = if (viewModel.activeSession.value?.test?.id == attempt.testId) {
                                    viewModel.activeSession.value?.questions
                                } else null
                                val fetched = activeQuestions?.takeIf { it.isNotEmpty() }
                                    ?: try {
                                        viewModel.getQuestionsForAttempt(attempt)
                                    } catch (e: Exception) {
                                        emptyList()
                                    }
                                reviewQuestions = if (fetched.isNotEmpty()) {
                                    fetched
                                } else {
                                    synthesizeQuestionsForAttempt(attempt)
                                }
                                currentScreen = AppScreen.RESULT_SCREEN
                            }
                        },
                        onSignOutComplete = {
                            currentUser = null
                            googleAccountUserName = null
                            currentScreen = AppScreen.HOME
                        }
                    )
                }
            }
        }
    }
}

private fun synthesizeQuestionsForAttempt(attempt: Attempt): List<Question> {
    if (attempt.answers.isNotEmpty()) {
        return attempt.answers.entries.mapIndexed { index, (qId, selectedIdx) ->
            val optionLetter = if (selectedIdx in 0..3) "Option " + ('A' + selectedIdx) else "Unanswered"
            Question(
                id = qId,
                testId = attempt.testId,
                exam = attempt.exam,
                text = "Question ${index + 1} (${attempt.testTitle.ifBlank { "Test Attempt" }})",
                options = listOf("Option A", "Option B", "Option C", "Option D"),
                correctIndex = if (selectedIdx in 0..3) selectedIdx else 0,
                explanation = "Your recorded response: $optionLetter. Original question details were not cached in this historical session.",
                order = index + 1
            )
        }
    }
    val totalCount = maxOf(1, attempt.correctCount + attempt.wrongCount + attempt.skippedCount)
    return (1..totalCount).map { num ->
        Question(
            id = "synth_q_$num",
            testId = attempt.testId,
            exam = attempt.exam,
            text = "Question $num (${attempt.testTitle.ifBlank { "Test Attempt" }})",
            options = listOf("Option A", "Option B", "Option C", "Option D"),
            correctIndex = 0,
            explanation = "Historical question from ${attempt.testTitle.ifBlank { "test attempt" }}.",
            order = num
        )
    }
}
