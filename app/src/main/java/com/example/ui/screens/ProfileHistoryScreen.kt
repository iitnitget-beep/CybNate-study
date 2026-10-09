package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import coil.compose.AsyncImage
import com.example.data.model.Attempt
import com.example.data.model.Bookmark
import com.example.ui.components.EmptyView
import com.example.ui.components.LoadingSkeleton
import com.example.ui.components.MarksTopAppBar
import com.example.ui.theme.MarksError
import com.example.ui.theme.MarksPrimary
import com.example.ui.theme.MarksSuccess
import com.example.ui.theme.MarksWarning
import com.example.ui.viewmodel.MarksViewModel
import com.example.ui.viewmodel.UiState
import com.example.util.AuthManager
import com.example.util.UserSessionManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun ProfileHistoryScreen(
    viewModel: MarksViewModel,
    onBackClick: () -> Unit,
    onAttemptClick: (Attempt) -> Unit,
    onSignOutComplete: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val attemptsState by viewModel.userAttemptsState.collectAsState()
    val bookmarksState by viewModel.userBookmarksState.collectAsState()

    val session = remember { UserSessionManager.getSession(context) }
    val currentUser = Firebase.auth.currentUser
    val userName = currentUser?.displayName?.ifBlank { null }
        ?: session.userName.ifBlank { null }
        ?: if (session.isGuest) "Guest Aspirant" else "Aspirant"
    val userEmail = currentUser?.email?.ifBlank { null }
        ?: if (session.isGuest) "Guest Mode (Local Progress Saved)"
           else if (session.userEmail.isNotBlank()) session.userEmail
           else currentUser?.phoneNumber ?: "Registered Student"
    val photoUrl = currentUser?.photoUrl?.toString()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MarksTopAppBar(
                title = "My Profile & History",
                onBackClick = onBackClick,
                actions = {
                    com.example.ui.components.ThemeToggleIconButton()

                    IconButton(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier.testTag("sign_out_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = MarksError
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Profile Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MarksPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MarksPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "JEE / NEET Aspirant",
                                color = MarksPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Theme & Display Mode Preference Card
            com.example.ui.components.ThemeSettingsCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Tabs: History vs Bookmarks
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MarksPrimary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        val count = (attemptsState as? UiState.Success)?.data?.size ?: 0
                        Text("Attempts ($count)", fontWeight = FontWeight.Bold)
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        val count = (bookmarksState as? UiState.Success)?.data?.size ?: 0
                        Text("Bookmarks ($count)", fontWeight = FontWeight.Bold)
                    }
                )
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // Attempts History
                    when (val state = attemptsState) {
                        is UiState.Loading -> LoadingSkeleton("Loading past attempts...")
                        is UiState.Error -> EmptyView("Error", state.message)
                        is UiState.Success -> {
                            val attempts = state.data
                            if (attempts.isEmpty()) {
                                EmptyView(
                                    title = "No test attempts yet",
                                    description = "Start taking chapter-wise mock tests to track your scores and rank progression.",
                                    icon = Icons.Default.History
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(attempts) { attempt ->
                                        AttemptHistoryItem(
                                            attempt = attempt,
                                            onClick = { onAttemptClick(attempt) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Bookmarks List
                    when (val state = bookmarksState) {
                        is UiState.Loading -> LoadingSkeleton("Loading saved questions...")
                        is UiState.Error -> EmptyView("Error", state.message)
                        is UiState.Success -> {
                            val bookmarks = state.data
                            if (bookmarks.isEmpty()) {
                                EmptyView(
                                    title = "No bookmarked questions",
                                    description = "Bookmark tricky questions during tests or review to revise them here anytime.",
                                    icon = Icons.Default.Bookmark
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(bookmarks) { bookmark ->
                                        BookmarkItemCard(bookmark = bookmark)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out of your Marks account?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        UserSessionManager.clearSession(context)
                        AuthManager.signOut(
                            context = context,
                            credentialManager = credentialManager,
                            onSignOutComplete = onSignOutComplete,
                            scope = coroutineScope
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MarksError)
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AttemptHistoryItem(
    attempt: Attempt,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = attempt.testTitle.ifBlank { "Test Series Paper" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MarksPrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Score: ${attempt.score.toInt()}/${attempt.totalMarks.toInt()}",
                        color = MarksPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "✓ ${attempt.correctCount} Correct",
                    style = MaterialTheme.typography.bodySmall,
                    color = MarksSuccess,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "✗ ${attempt.wrongCount} Wrong",
                    style = MaterialTheme.typography.bodySmall,
                    color = MarksError,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "– ${attempt.skippedCount} Skipped",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val timeM = attempt.timeTakenSeconds / 60
                val timeS = attempt.timeTakenSeconds % 60
                Text(
                    text = "Time spent: ${timeM}m ${timeS}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "View Solutions →",
                    color = MarksPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BookmarkItemCard(bookmark: Bookmark) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MarksWarning.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = MarksWarning,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bookmark.questionText.ifBlank { "Bookmarked Question" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
