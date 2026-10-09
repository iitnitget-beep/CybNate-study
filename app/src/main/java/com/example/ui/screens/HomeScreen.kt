package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Attempt
import com.example.data.model.Category
import com.example.data.model.ExamBranch
import com.example.data.model.Test
import com.example.ui.components.EmptyView
import com.example.ui.components.LoadingSkeleton
import com.example.ui.theme.MarksError
import com.example.ui.theme.MarksPrimary
import com.example.ui.theme.MarksPrimaryDark
import com.example.ui.theme.MarksPrimaryLight
import com.example.ui.theme.MarksSecondary
import com.example.ui.theme.MarksSuccess
import com.example.ui.theme.MarksWarning
import com.example.ui.viewmodel.MarksViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MarksViewModel,
    googleUserName: String? = null,
    onTestClick: (Test) -> Unit,
    onAttemptReviewClick: (Attempt) -> Unit,
    onProfileClick: () -> Unit,
    onScienceSkillsClick: () -> Unit = {}
) {
    val examBranches by viewModel.examBranchesState.collectAsState()
    val selectedBranch by viewModel.selectedExamBranch.collectAsState()
    val categoriesState by viewModel.categoriesState.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val testsState by viewModel.testsState.collectAsState()
    val attemptsState by viewModel.userAttemptsState.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val currentUser = Firebase.auth.currentUser
    val isGoogleAccountUser = (currentUser != null && !currentUser.isAnonymous) || googleUserName != null
    val userName = googleUserName ?: (if (currentUser != null && !currentUser.isAnonymous) currentUser.displayName ?: "Aspirant" else "Guest Aspirant")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(com.example.ui.theme.BumblebeeYellow.copy(alpha = 0.2f))
                                .border(1.5.dp, com.example.ui.theme.BumblebeeYellow, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            com.example.ui.components.BumblebeeFaceLogo(
                                size = 26.dp,
                                tint = com.example.ui.theme.BumblebeeYellowBright,
                                animatedOptics = true
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CybNate",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Target: $selectedBranch Examination",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    com.example.ui.components.ThemeToggleIconButton()

                    IconButton(
                        onClick = { viewModel.refreshData() },
                        modifier = Modifier.testTag("sync_firestore_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh from Firestore",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier.testTag("home_profile_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MarksPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = MarksPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Welcome & Prep Stats Banner
            item {
                HomeHeaderBanner(
                    userName = userName,
                    selectedBranch = selectedBranch,
                    attempts = (attemptsState as? UiState.Success)?.data ?: emptyList()
                )
            }

            // Science Skills Hub Banner
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { onScienceSkillsClick() }
                        .testTag("science_skills_hub_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MarksPrimary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MarksPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🧪", fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Science Skills Hub",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MarksSuccess.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "NEW",
                                            color = MarksSuccess,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Virtual Vernier, Screw Gauge, Salt Analysis & Lab PYQs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Science Skills",
                            tint = MarksPrimary
                        )
                    }
                }
            }

            // 2. EXAM BRANCHES SECTION (Engineering and Medical Chapter-wise PYQs)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chapter-wise Previous Year Questions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "2001 - 2026",
                            style = MaterialTheme.typography.labelSmall,
                            color = MarksPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Engineering & Medical 15+ Years Solved Papers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Engineering Exams Subsection
                val engineeringExams = examBranches.filter { it.category == "Engineering" || (!it.category.equals("Medical", true) && !it.id.contains("neet") && !it.id.contains("aiims") && !it.id.contains("jipmer")) }
                val medicalExams = examBranches.filter { it.category == "Medical" || it.id.contains("neet") || it.id.contains("aiims") || it.id.contains("jipmer") }

                if (engineeringExams.isNotEmpty()) {
                    Text(
                        text = "Engineering",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(engineeringExams) { branch ->
                            val isSelected = selectedBranch.equals(branch.name, ignoreCase = true) || selectedBranch.equals(branch.displayName, ignoreCase = true)
                            ExamGridCardItem(
                                branch = branch,
                                isSelected = isSelected,
                                onClick = { viewModel.selectExamBranch(branch.name) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (medicalExams.isNotEmpty()) {
                    Text(
                        text = "Medical",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MarksSuccess,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(medicalExams) { branch ->
                            val isSelected = selectedBranch.equals(branch.name, ignoreCase = true) || selectedBranch.equals(branch.displayName, ignoreCase = true)
                            ExamGridCardItem(
                                branch = branch,
                                isSelected = isSelected,
                                onClick = { viewModel.selectExamBranch(branch.name) }
                            )
                        }
                    }
                }
            }

            // 3. Categories / Subjects under Selected Exam
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "$selectedBranch Subjects & Chapters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))

                when (val catState = categoriesState) {
                    is UiState.Loading -> {
                        LoadingSkeleton("Loading $selectedBranch subjects...")
                    }
                    is UiState.Error -> {
                        Text(
                            text = catState.message,
                            color = MarksError,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    is UiState.Success -> {
                        val categories = catState.data
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedCategoryId == null,
                                    onClick = { viewModel.selectCategory(null) },
                                    label = { Text("All $selectedBranch Chapter Sets") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MarksPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            items(categories) { cat ->
                                FilterChip(
                                    selected = selectedCategoryId == cat.id,
                                    onClick = { viewModel.selectCategory(cat.id) },
                                    label = { Text(cat.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MarksPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 4. Test Series Section Header
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$selectedBranch Chapter Practice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 5. Test Series List (Real-Time from Firestore)
            when (val tState = testsState) {
                is UiState.Loading -> {
                    item {
                        LoadingSkeleton("Fetching $selectedBranch practice sets from Firestore...")
                    }
                }
                is UiState.Error -> {
                    item {
                        EmptyView(
                            title = "Unable to load chapter practice",
                            description = tState.message
                        )
                    }
                }
                is UiState.Success -> {
                    val tests = tState.data
                    if (tests.isEmpty()) {
                        item {
                            EmptyView(
                                title = "No practice sets found for $selectedBranch",
                                description = "Upload new practice sets with exam: \"$selectedBranch\" in Firestore and they will automatically appear here!",
                                actionButton = {
                                    Button(
                                        onClick = { viewModel.refreshData() },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MarksPrimary)
                                    ) {
                                        Text("Refresh Practice Sets")
                                    }
                                }
                            )
                        }
                    } else {
                        items(tests) { test ->
                            TestCard(
                                test = test,
                                onTestClick = { onTestClick(test) }
                            )
                        }
                    }
                }
            }

            // 6. Recent Attempts Section
            val attempts = (attemptsState as? UiState.Success)?.data
            if (!attempts.isNullOrEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Recent Practice Attempts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(attempts.take(3)) { attempt ->
                    RecentAttemptCard(
                        attempt = attempt,
                        onClick = { onAttemptReviewClick(attempt) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeaderBanner(
    userName: String,
    selectedBranch: String,
    attempts: List<Attempt>
) {
    val branchAttempts = attempts.filter { it.exam.isBlank() || it.exam.equals(selectedBranch, ignoreCase = true) }
    val totalAttempts = branchAttempts.size
    val totalCorrect = branchAttempts.sumOf { it.correctCount }
    val accuracy = if (totalAttempts > 0 && (totalCorrect + branchAttempts.sumOf { it.wrongCount }) > 0) {
        val attemptedQ = totalCorrect + branchAttempts.sumOf { it.wrongCount }
        (totalCorrect.toDouble() / attemptedQ * 100).toInt()
    } else {
        0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0F1117), // Bumblebee Stinger Chassis Dark
                            Color(0xFF1E1700), // Cybertron Warm Core
                            Color(0xFF2B1D00)  // Gold Energon Ambient
                        )
                    )
                )
                .padding(18.dp)
        ) {
            // Cybertron Scanning Grid background
            com.example.ui.components.CybertronScanningGrid(
                gridColor = com.example.ui.theme.BumblebeeYellow.copy(alpha = 0.08f),
                scanLineColor = com.example.ui.theme.EnergonBlueBright.copy(alpha = 0.35f)
            )

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Hello, $userName! ⚡",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = com.example.ui.theme.BumblebeeYellowBright
                            )
                        }
                        Text(
                            text = "Target $selectedBranch Examination • Practice Series",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.BumblebeeYellow.copy(alpha = 0.18f))
                            .border(1.dp, com.example.ui.theme.BumblebeeYellow.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        com.example.ui.components.BumblebeeFaceLogo(
                            size = 28.dp,
                            tint = com.example.ui.theme.BumblebeeYellowBright,
                            animatedOptics = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatMetric(label = "Practice Sets", value = "$totalAttempts")
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                    StatMetric(label = "Accuracy", value = if (totalAttempts > 0) "$accuracy%" else "--")
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                    StatMetric(label = "Solved Qs", value = "$totalCorrect")
                }
            }
        }
    }
}

@Composable
private fun StatMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun TestCard(
    test: Test,
    onTestClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onTestClick() }
            .testTag("test_card_${test.id}"),
        shape = RoundedCornerShape(16.dp),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val diffColor = when (test.difficulty.lowercase()) {
                    "easy" -> MarksSuccess
                    "hard" -> MarksError
                    else -> MarksWarning
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = diffColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = test.difficulty.uppercase(),
                        color = diffColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "+4 / -${test.negativeMarking.toInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = test.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (test.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = test.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${test.duration} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${test.totalQuestions} Qs • ${test.totalMarks.toInt()} M",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onTestClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MarksPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Take Test",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentAttemptCard(
    attempt: Attempt,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attempt.testTitle.ifBlank { "Chapter Practice" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Score: ${attempt.score.toInt()} / ${attempt.totalMarks.toInt()} • ${attempt.correctCount} Correct • ${attempt.wrongCount} Wrong",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MarksSuccess.copy(alpha = 0.15f),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "Review",
                    color = MarksSuccess,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ExamGridCardItem(
    branch: ExamBranch,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(120.dp)
            .height(115.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) com.example.ui.theme.BumblebeeYellow else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("exam_card_${branch.id}"),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shadowElevation = if (isSelected) 3.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            branch.category.equals("Medical", true) -> Color(0xFF10B981).copy(alpha = 0.18f)
                            branch.id.contains("adv") -> Color(0xFFEF4444).copy(alpha = 0.18f)
                            branch.id.contains("iiser") || branch.id.contains("niser") -> Color(0xFF8B5CF6).copy(alpha = 0.18f)
                            else -> Color(0xFFF59E0B).copy(alpha = 0.18f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        branch.category.equals("Medical", true) -> "🩺"
                        branch.id.contains("adv") -> "⚙️"
                        branch.id.contains("iiser") || branch.id.contains("niser") -> "🔬"
                        branch.id.contains("bitsat") -> "🏛️"
                        branch.id.contains("kvpy") -> "🌟"
                        branch.id.contains("nda") -> "🛡️"
                        else -> "🎯"
                    },
                    fontSize = 16.sp
                )
            }
            Column {
                Text(
                    text = branch.displayName.ifBlank { branch.name },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = branch.yearsRange.ifBlank { "2026 - 2012" },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}



