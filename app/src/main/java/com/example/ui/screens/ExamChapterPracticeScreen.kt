package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Test
import com.example.ui.components.MarksTopAppBar
import com.example.ui.theme.MarksError
import com.example.ui.theme.MarksPrimary
import com.example.ui.theme.MarksSuccess
import com.example.ui.viewmodel.MarksViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamChapterPracticeScreen(
    examTitle: String,
    initialTab: String = "Phy",
    viewModel: MarksViewModel,
    onBackClick: () -> Unit,
    onStartPractice: (Test, String?, String?) -> Unit, // test, topic, difficulty
    onOpenBookmarks: (String) -> Unit,
    onOpenAnalysis: (String) -> Unit
) {
    val isNeet = examTitle.contains("neet", ignoreCase = true) || viewModel.selectedExamBranch.collectAsState().value.contains("neet", ignoreCase = true)
    val availableTabs = if (isNeet) {
        listOf("Home", "Chem", "Zoo", "Bot", "Phy", "Analysis")
    } else {
        listOf("Home", "Phy", "Chem", "Math", "Analysis")
    }

    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }
    var selectedClassFilter by remember { mutableStateOf("All") }
    var selectedUnitFilter by remember { mutableStateOf("All Units") }
    var searchQuery by remember { mutableStateOf("") }
    
    // Chapter click bottom sheet / dialog state for topic & difficulty
    var selectedChapterForPractice by remember { mutableStateOf<Test?>(null) }
    var selectedTopic by remember { mutableStateOf<String?>(null) }
    var selectedDifficulty by remember { mutableStateOf<String>("All") }

    val categoriesState by viewModel.categoriesState.collectAsState()
    val testsState by viewModel.testsState.collectAsState()

    // If Analysis tab is selected, render AnalysisScreen
    if (selectedTab.equals("Analysis", ignoreCase = true)) {
        AnalysisScreen(
            examTitle = examTitle,
            subject = "All",
            viewModel = viewModel,
            onBackClick = onBackClick,
            onTabSelected = { selectedTab = it }
        )
        return
    }

    // Filter tests by current subject tab
    val subjectMapping = mapOf(
        "Bot" to "Botany",
        "Zoo" to "Zoology",
        "Phy" to "Physics",
        "Chem" to "Chemistry",
        "Math" to "Mathematics"
    )
    val currentSubjectName = subjectMapping[selectedTab] ?: selectedTab

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                MarksTopAppBar(
                    title = examTitle,
                    onBackClick = onBackClick,
                    actions = {
                        com.example.ui.components.ThemeToggleIconButton()
                        IconButton(onClick = { /* Search toggle */ }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(onClick = { /* Menu */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                )

                // Top Navigation Tabs (Home | Bio/Phy | Chem | Math/Analysis)
                ScrollableTabRow(
                    selectedTabIndex = availableTabs.indexOf(selectedTab).coerceAtLeast(0),
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MarksPrimary
                ) {
                    availableTabs.forEach { tab ->
                        Tab(
                            selected = selectedTab.equals(tab, true),
                            onClick = {
                                if (tab == "Home") {
                                    onBackClick()
                                } else {
                                    selectedTab = tab
                                }
                            },
                            text = {
                                Text(
                                    text = tab,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Bottom Action Bar: Go Home | Subject Bookmarks | Subject Analysis
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBackClick) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = MarksPrimary, modifier = Modifier.size(22.dp))
                            Text("Go Home", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    TextButton(onClick = { onOpenBookmarks(currentSubjectName) }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = MarksPrimary, modifier = Modifier.size(22.dp))
                            Text("$selectedTab Bookmarks", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    TextButton(onClick = { selectedTab = "Analysis" }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = MarksPrimary, modifier = Modifier.size(22.dp))
                            Text("$selectedTab Analysis", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Chips Row (Class, Units, Not Started, Weak Chapter)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    OutlinedButton(
                        onClick = { /* Filter modal */ },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
                item {
                    FilterChip(
                        selected = selectedClassFilter != "All",
                        onClick = {
                            selectedClassFilter = if (selectedClassFilter == "All") "12th" else "All"
                        },
                        label = { Text(if (selectedClassFilter == "All") "Class ⌵" else selectedClassFilter) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedUnitFilter != "All Units",
                        onClick = { selectedUnitFilter = "All Units" },
                        label = { Text("$selectedTab Units ⌵") }
                    )
                }
                item {
                    FilterChip(
                        selected = false,
                        onClick = { },
                        label = { Text("Not Started") }
                    )
                }
                item {
                    FilterChip(
                        selected = false,
                        onClick = { },
                        label = { Text("Weak Chapter ➔") }
                    )
                }
            }

            // Chapters Count Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (val tState = testsState) {
                    is UiState.Success -> {
                        val filteredTests = tState.data.filter { test ->
                            when {
                                selectedTab.equals("Bot", true) -> test.subject.contains("botan", true) || test.categoryId.contains("botan", true)
                                selectedTab.equals("Zoo", true) -> test.subject.contains("zoo", true) || test.categoryId.contains("zoo", true)
                                selectedTab.equals("Phy", true) -> test.subject.contains("phys", true) || test.categoryId.contains("phys", true)
                                selectedTab.equals("Chem", true) -> test.subject.contains("chem", true) || test.categoryId.contains("chem", true)
                                selectedTab.equals("Math", true) -> test.subject.contains("math", true) || test.categoryId.contains("math", true)
                                else -> test.subject.isBlank() || test.subject.contains(currentSubjectName, ignoreCase = true) || test.categoryId.contains(currentSubjectName, ignoreCase = true)
                            }
                        }
                        Text(
                            text = "Showing chapters (${filteredTests.size})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    else -> {
                        Text(
                            text = "Showing chapters (30/32)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "⇅ Sort",
                    style = MaterialTheme.typography.bodySmall,
                    color = MarksPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Chapter List
            when (val tState = testsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MarksPrimary)
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(tState.message, color = MarksError)
                    }
                }
                is UiState.Success -> {
                    val chapters = tState.data.filter { test ->
                        when {
                            selectedTab.equals("Bot", true) -> test.subject.contains("botan", true) || test.categoryId.contains("botan", true)
                            selectedTab.equals("Zoo", true) -> test.subject.contains("zoo", true) || test.categoryId.contains("zoo", true)
                            selectedTab.equals("Phy", true) -> test.subject.contains("phys", true) || test.categoryId.contains("phys", true)
                            selectedTab.equals("Chem", true) -> test.subject.contains("chem", true) || test.categoryId.contains("chem", true)
                            selectedTab.equals("Math", true) -> test.subject.contains("math", true) || test.categoryId.contains("math", true)
                            else -> test.subject.isBlank() || test.subject.contains(currentSubjectName, ignoreCase = true) || test.categoryId.contains(currentSubjectName, ignoreCase = true)
                        }
                    }

                    if (chapters.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(54.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No chapters found for $selectedTab in database",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Listening to Firestore collections: 'tests', 'chapters', 'questions', and 'Exams'. Upload chapters to your database to practice them here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(chapters) { chapter ->
                                ChapterPracticeCard(
                                    test = chapter,
                                    onClick = {
                                        selectedChapterForPractice = chapter
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Topic and Difficulty Selection Bottom Sheet / Dialog
    if (selectedChapterForPractice != null) {
        val chapter = selectedChapterForPractice!!
        val chapterTopics by viewModel.getTopicsForChapter(chapter).collectAsState(
            initial = listOf("All Topics")
        )

        AlertDialog(
            onDismissRequest = { selectedChapterForPractice = null },
            title = {
                Column {
                    Text(text = chapter.chapter.ifBlank { chapter.title }, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Filter Topic & Difficulty Sub-branches", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Select Difficulty Level:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Easy", "Medium", "Hard").forEach { diff ->
                            FilterChip(
                                selected = selectedDifficulty == diff,
                                onClick = { selectedDifficulty = diff },
                                label = { Text(diff, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (diff) {
                                        "Easy" -> MarksSuccess
                                        "Hard" -> MarksError
                                        else -> MarksPrimary
                                    },
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Topic Sub-branch:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(chapterTopics) { topic ->
                            val isSel = (selectedTopic == topic || (selectedTopic == null && topic == "All Topics"))
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedTopic = if (topic == "All Topics") null else topic },
                                label = { Text(topic, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MarksPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val t = selectedChapterForPractice
                        val top = selectedTopic
                        val diff = if (selectedDifficulty == "All") null else selectedDifficulty
                        selectedChapterForPractice = null
                        if (t != null) {
                            onStartPractice(t, top, diff)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MarksPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Filtered Practice", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedChapterForPractice = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun ChapterPracticeCard(
    test: Test,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MarksPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MarksPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = test.chapter.ifBlank { test.title },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (test.isRemoved) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MarksError.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "REMOVED",
                                color = MarksError,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2026: ${test.questions2026} Qs ↓ | 2025: ${test.questions2025} Qs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "0/${test.totalQuestions} Qs",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MarksPrimary
                    )
                }
            }
        }
    }
}
