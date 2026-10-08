package com.example.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Attempt
import com.example.data.model.Question
import com.example.ui.components.MarksTopAppBar
import com.example.ui.theme.MarksError
import com.example.ui.theme.MarksPrimary
import com.example.ui.theme.MarksSuccess
import com.example.ui.theme.MarksWarning
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.viewmodel.MarksViewModel

enum class ReviewFilter {
    ALL,
    INCORRECT,
    CORRECT,
    SKIPPED
}

@Composable
fun ReviewScreen(
    attempt: Attempt,
    questions: List<Question>,
    viewModel: MarksViewModel,
    onBackClick: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(ReviewFilter.ALL) }
    var currentFilteredIndex by remember { mutableIntStateOf(0) }
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val filteredQuestions = remember(selectedFilter, questions, attempt) {
        when (selectedFilter) {
            ReviewFilter.ALL -> questions
            ReviewFilter.CORRECT -> questions.filter { q ->
                attempt.answers[q.id] == q.correctIndex
            }
            ReviewFilter.INCORRECT -> questions.filter { q ->
                val ans = attempt.answers[q.id]
                ans != null && ans != q.correctIndex
            }
            ReviewFilter.SKIPPED -> questions.filter { q ->
                !attempt.answers.containsKey(q.id)
            }
        }
    }

    val safeIndex = currentFilteredIndex.coerceIn(0, (filteredQuestions.size - 1).coerceAtLeast(0))
    val currentQuestion = filteredQuestions.getOrNull(safeIndex)

    Scaffold(
        topBar = {
            MarksTopAppBar(
                title = "Solutions & Explanations",
                subtitle = attempt.testTitle.ifBlank { "Solution Review" },
                onBackClick = onBackClick,
                actions = {
                    com.example.ui.components.ThemeToggleIconButton()
                    if (currentQuestion != null) {
                        val isBookmarked = viewModel.isBookmarked(currentQuestion.id)
                        IconButton(
                            onClick = { viewModel.toggleBookmark(currentQuestion) },
                            modifier = Modifier.testTag("review_bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) MarksWarning else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (filteredQuestions.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (safeIndex > 0) currentFilteredIndex = safeIndex - 1
                            },
                            enabled = safeIndex > 0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Previous")
                        }

                        Button(
                            onClick = {
                                if (safeIndex < filteredQuestions.size - 1) currentFilteredIndex = safeIndex + 1
                            },
                            enabled = safeIndex < filteredQuestions.size - 1,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MarksPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
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
            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == ReviewFilter.ALL,
                    onClick = {
                        selectedFilter = ReviewFilter.ALL
                        currentFilteredIndex = 0
                    },
                    label = { Text("All (${questions.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MarksPrimary,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedFilter == ReviewFilter.INCORRECT,
                    onClick = {
                        selectedFilter = ReviewFilter.INCORRECT
                        currentFilteredIndex = 0
                    },
                    label = { Text("Wrong (${attempt.wrongCount})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MarksError,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedFilter == ReviewFilter.CORRECT,
                    onClick = {
                        selectedFilter = ReviewFilter.CORRECT
                        currentFilteredIndex = 0
                    },
                    label = { Text("Correct (${attempt.correctCount})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MarksSuccess,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedFilter == ReviewFilter.SKIPPED,
                    onClick = {
                        selectedFilter = ReviewFilter.SKIPPED
                        currentFilteredIndex = 0
                    },
                    label = { Text("Skipped (${attempt.skippedCount})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedLabelColor = Color.White
                    )
                )
            }

            if (questions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Questions not available for this old attempt",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Score: ${attempt.score.toInt()}/${attempt.totalMarks.toInt()} (${attempt.correctCount} Correct, ${attempt.wrongCount} Wrong)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (currentQuestion == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No questions match this filter.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val userAnswer = attempt.answers[currentQuestion.id]
                val isCorrect = userAnswer == currentQuestion.correctIndex
                val isSkipped = userAnswer == null

                val originalIndex = questions.indexOfFirst { it.id == currentQuestion.id } + 1

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Question Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question $originalIndex",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val (statusBadgeText, badgeColor) = when {
                            isCorrect -> Pair("Correct (+4)", MarksSuccess)
                            isSkipped -> Pair("Unattempted (0)", MaterialTheme.colorScheme.onSurfaceVariant)
                            else -> Pair("Incorrect (-1)", MarksError)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = statusBadgeText,
                                color = badgeColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Question Statement
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = currentQuestion.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 24.sp
                            )

                            val allQuestionDiagrams = if (currentQuestion.imageUrls.isNotEmpty()) {
                                currentQuestion.imageUrls
                            } else if (currentQuestion.imageUrl.isNotBlank()) {
                                listOf(currentQuestion.imageUrl)
                            } else emptyList()

                            if (allQuestionDiagrams.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                allQuestionDiagrams.forEachIndexed { dIdx, dUrl ->
                                    val title = if (allQuestionDiagrams.size > 1) "Figure / Diagram ${dIdx + 1}" else "Question Figure / Diagram"
                                    com.example.ui.components.DiagramView(
                                        imageUrl = dUrl,
                                        title = title,
                                        onClick = { zoomedImageUrl = dUrl }
                                    )
                                    if (dIdx < allQuestionDiagrams.size - 1) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Options with Review Highlighting
                    Text(
                        text = "Options & Responses:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val optionLabels = listOf("A", "B", "C", "D")
                    currentQuestion.options.forEachIndexed { optIndex, optText ->
                        val isUserPick = userAnswer == optIndex
                        val isCorrectOption = currentQuestion.correctIndex == optIndex

                        val (cardBorder, cardBg) = when {
                            isCorrectOption -> Pair(MarksSuccess, MarksSuccess.copy(alpha = 0.1f))
                            isUserPick && !isCorrectOption -> Pair(MarksError, MarksError.copy(alpha = 0.1f))
                            else -> Pair(Slate200, MaterialTheme.colorScheme.surface)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isCorrectOption || isUserPick) 2.dp else 1.dp,
                                    color = cardBorder,
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isCorrectOption -> MarksSuccess
                                                isUserPick -> MarksError
                                                else -> Slate200
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCorrectOption) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    } else if (isUserPick) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    } else {
                                        Text(
                                            text = optionLabels.getOrElse(optIndex) { "${optIndex + 1}" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    if (optText.isNotBlank()) {
                                        Text(
                                            text = optText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    val optImg = currentQuestion.optionImages.getOrElse(optIndex) { "" }
                                    if (optImg.isNotBlank()) {
                                        if (optText.isNotBlank()) Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White)
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                                .clickable { zoomedImageUrl = optImg }
                                                .padding(6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = optImg,
                                                contentDescription = "Option ${optionLabels.getOrElse(optIndex) { "" }} Diagram",
                                                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 160.dp)
                                            )
                                        }
                                    }
                                }

                                if (isCorrectOption) {
                                    Text(
                                        text = "Correct Answer",
                                        color = MarksSuccess,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 6.dp)
                                    )
                                } else if (isUserPick) {
                                    Text(
                                        text = "Your Answer",
                                        color = MarksError,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Detailed Explanation Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MarksPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = MarksPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Detailed Explanation",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MarksPrimary
                                )
                            }

                            val allSolutionDiagrams = if (currentQuestion.solutionImageUrls.isNotEmpty()) {
                                currentQuestion.solutionImageUrls
                            } else if (currentQuestion.solutionImageUrl.isNotBlank()) {
                                listOf(currentQuestion.solutionImageUrl)
                            } else emptyList()

                            if (allSolutionDiagrams.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                allSolutionDiagrams.forEachIndexed { sIdx, sUrl ->
                                    val title = if (allSolutionDiagrams.size > 1) "Worked Solution Figure ${sIdx + 1}" else "Worked Solution Diagram"
                                    com.example.ui.components.DiagramView(
                                        imageUrl = sUrl,
                                        title = title,
                                        onClick = { zoomedImageUrl = sUrl }
                                    )
                                    if (sIdx < allSolutionDiagrams.size - 1) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }

                            val hasStaticExplanation = currentQuestion?.explanation?.isNotBlank() == true && currentQuestion.explanation != "No Solution Available"

                            if (hasStaticExplanation) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = currentQuestion.explanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 22.sp
                                )
                            } else {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Detailed explanation is not available for this question yet.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (zoomedImageUrl != null) {
        com.example.ui.components.ZoomableImageDialog(
            imageUrl = zoomedImageUrl!!,
            title = "Diagram / Figure Inspection",
            onDismiss = { zoomedImageUrl = null }
        )
    }
}
