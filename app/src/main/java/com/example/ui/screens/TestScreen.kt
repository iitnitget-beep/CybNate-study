package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Attempt
import com.example.data.model.Question
import com.example.data.model.QuestionPaletteStatus
import com.example.ui.components.LoadingSkeleton
import com.example.ui.components.PaletteColorHelper
import com.example.ui.components.PaletteStatusLegendItem
import com.example.ui.theme.MarksError
import com.example.ui.theme.MarksNotVisited
import com.example.ui.theme.MarksPrimary
import com.example.ui.theme.MarksPurple
import com.example.ui.theme.MarksSuccess
import com.example.ui.theme.MarksWarning
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.viewmodel.MarksViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    viewModel: MarksViewModel,
    onTestSubmitted: (Attempt) -> Unit,
    onExitTest: () -> Unit
) {
    val session by viewModel.activeSession.collectAsState()
    var showPaletteSheet by remember { mutableStateOf(false) }
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle Android system back button safely
    BackHandler {
        showExitConfirmDialog = true
    }

    if (session == null || session!!.questions.isEmpty()) {
        var waitExceeded by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(4000)
            waitExceeded = true
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Practice Session") },
                    navigationIcon = {
                        IconButton(onClick = onExitTest) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!waitExceeded) {
                    LoadingSkeleton("Fetching practice questions from Firestore...")
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No questions found in database for this chapter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ensure questions for this chapter exist in Firestore under collection 'Questions' or 'questions'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onExitTest) {
                            Text("Back to Chapters")
                        }
                    }
                }
            }
        }
        return
    }

    val currentSession = session!!

    LaunchedEffect(currentSession.isSubmitted) {
        if (currentSession.isSubmitted && currentSession.submittedAttempt != null) {
            showSubmitConfirmDialog = false
            showPaletteSheet = false
            onTestSubmitted(currentSession.submittedAttempt!!)
        }
    }

    val questions = currentSession.questions
    val currentIndex = currentSession.currentQuestionIndex.coerceIn(0, questions.size - 1)
    val currentQuestion = questions[currentIndex]
    val selectedOption = currentSession.answers[currentQuestion.id]
    val currentStatus = currentSession.statusMap[currentQuestion.id] ?: QuestionPaletteStatus.NOT_ANSWERED
    val isBookmarked = viewModel.isBookmarked(currentQuestion.id)

    // Format remaining time (MM:SS)
    val minutes = currentSession.timeRemainingSeconds / 60
    val seconds = currentSession.timeRemainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val timerColor = when {
        currentSession.timeRemainingSeconds < 60 -> MarksError
        currentSession.timeRemainingSeconds < 300 -> MarksWarning
        else -> MarksPrimary
    }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    com.example.ui.components.LiquidShaderBackground(
        variant = com.example.ui.components.LiquidThemeVariant.SUNSET_GLASS,
        isDarkTheme = isDark
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Q ${currentIndex + 1} of ${questions.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "+${currentQuestion.marks.toInt()} / -${currentSession.test.negativeMarking}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MarksSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Countdown Timer badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = timerColor.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, timerColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = timerColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = timeFormatted,
                                    color = timerColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Question Palette Toggle button
                    IconButton(
                        onClick = { showPaletteSheet = true },
                        modifier = Modifier.testTag("open_palette_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Question Palette",
                            tint = MarksPrimary
                        )
                    }

                    // Bookmark question
                    IconButton(
                        onClick = { viewModel.toggleBookmark(currentQuestion) },
                        modifier = Modifier.testTag("bookmark_question_button")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark Question",
                            tint = if (isBookmarked) MarksWarning else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Exam Action Control Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Top row: Secondary actions (Clear, Mark for review, Submit)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.clearResponse() },
                            enabled = selectedOption != null,
                            modifier = Modifier.testTag("clear_response_button")
                        ) {
                            Text(
                                text = "Clear Response",
                                color = if (selectedOption != null) MarksError else Slate600,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        TextButton(
                            onClick = { viewModel.toggleMarkForReview() },
                            modifier = Modifier.testTag("mark_review_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = MarksPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentStatus == QuestionPaletteStatus.MARKED_FOR_REVIEW || currentStatus == QuestionPaletteStatus.ANSWERED_AND_MARKED)
                                    "Unmark" else "Mark for Review",
                                color = MarksPurple,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { showSubmitConfirmDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MarksSuccess
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("submit_test_button")
                        ) {
                            Text(
                                text = "Submit",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Bottom row: Previous & Save and Next primary navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.previousQuestion() },
                            enabled = currentIndex > 0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("previous_question_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Previous", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                if (currentIndex == questions.size - 1) {
                                    showSubmitConfirmDialog = true
                                } else {
                                    viewModel.nextQuestion()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MarksPrimary
                            ),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .testTag("next_question_button")
                        ) {
                            Text(
                                text = if (currentIndex == questions.size - 1) "Review & Submit" else "Save & Next",
                                fontWeight = FontWeight.Bold
                            )
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Question Statement Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Question ${currentIndex + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MarksPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentQuestion.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )

                    // Diagrams / Figures in Question
                    val allQuestionDiagrams = if (currentQuestion.imageUrls.isNotEmpty()) {
                        currentQuestion.imageUrls
                    } else if (currentQuestion.imageUrl.isNotBlank()) {
                        listOf(currentQuestion.imageUrl)
                    } else emptyList()

                    if (allQuestionDiagrams.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        allQuestionDiagrams.forEachIndexed { dIdx, dUrl ->
                            val title = if (allQuestionDiagrams.size > 1) "Figure / Diagram ${dIdx + 1}" else "Figure / Question Diagram"
                            com.example.ui.components.DiagramView(
                                imageUrl = dUrl,
                                title = title,
                                onClick = { zoomedImageUrl = dUrl }
                            )
                            if (dIdx < allQuestionDiagrams.size - 1) {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4 Multiple Choice Options
            Text(
                text = "Choose the correct option:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            val optionLabels = listOf("A", "B", "C", "D")
            currentQuestion.options.forEachIndexed { index, optionText ->
                val optionImg = currentQuestion.optionImages.getOrElse(index) { "" }
                val isSelected = selectedOption == index
                OptionItem(
                    label = optionLabels.getOrElse(index) { "${index + 1}" },
                    text = optionText,
                    imageUrl = optionImg,
                    isSelected = isSelected,
                    onSelect = { viewModel.selectAnswer(index) },
                    onImageClick = { if (optionImg.isNotBlank()) zoomedImageUrl = optionImg }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Question Palette Bottom Sheet
    if (showPaletteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaletteSheet = false },
            sheetState = sheetState
        ) {
            QuestionPaletteContent(
                questions = questions,
                currentQuestionIndex = currentIndex,
                statusMap = currentSession.statusMap,
                answers = currentSession.answers,
                onQuestionClick = { index ->
                    viewModel.navigateToQuestion(index)
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        showPaletteSheet = false
                    }
                },
                onSubmitClick = {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        showPaletteSheet = false
                        showSubmitConfirmDialog = true
                    }
                }
            )
        }
    }

    // Submit Confirmation Dialog
    if (showSubmitConfirmDialog) {
        val totalQ = questions.size
        val answeredCount = currentSession.answers.size
        val markedCount = currentSession.statusMap.values.count {
            it == QuestionPaletteStatus.MARKED_FOR_REVIEW || it == QuestionPaletteStatus.ANSWERED_AND_MARKED
        }
        val unansweredCount = totalQ - answeredCount

        AlertDialog(
            onDismissRequest = {
                if (!currentSession.isSubmitting) showSubmitConfirmDialog = false
            },
            title = {
                Text(
                    text = "Submit Test?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Are you sure you want to finish this test? Once submitted, your score will be calculated and saved.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Questions:", style = MaterialTheme.typography.bodySmall)
                                Text("$totalQ", fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Answered:", style = MaterialTheme.typography.bodySmall, color = MarksSuccess)
                                Text("$answeredCount", fontWeight = FontWeight.Bold, color = MarksSuccess)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Not Answered:", style = MaterialTheme.typography.bodySmall, color = MarksError)
                                Text("$unansweredCount", fontWeight = FontWeight.Bold, color = MarksError)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Marked for Review:", style = MaterialTheme.typography.bodySmall, color = MarksPurple)
                                Text("$markedCount", fontWeight = FontWeight.Bold, color = MarksPurple)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitActiveTest(onComplete = { attempt ->
                            showSubmitConfirmDialog = false
                            onTestSubmitted(attempt)
                        })
                    },
                    enabled = !currentSession.isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = MarksSuccess),
                    modifier = Modifier.testTag("confirm_submit_button")
                ) {
                    if (currentSession.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Confirm & Submit")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSubmitConfirmDialog = false },
                    enabled = !currentSession.isSubmitting
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Exit Confirmation Dialog
    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("Quit Practice?") },
            text = { Text("Your practice progress will be lost if you leave without submitting. Are you sure you want to exit?") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        viewModel.resetSession()
                        onExitTest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MarksError)
                ) {
                    Text("Exit Practice")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text("Resume Practice")
                }
            }
        )
    }

    if (zoomedImageUrl != null) {
        com.example.ui.components.ZoomableImageDialog(
            imageUrl = zoomedImageUrl!!,
            title = "Figure / Diagram",
            onDismiss = { zoomedImageUrl = null }
        )
    }
}
}

@Composable
private fun OptionItem(
    label: String,
    text: String,
    imageUrl: String = "",
    isSelected: Boolean,
    onSelect: () -> Unit,
    onImageClick: (() -> Unit)? = null
) {
    val borderColor = if (isSelected) MarksPrimary else Color.White.copy(alpha = 0.2f)
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface

    com.example.ui.components.LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .then(
                if (isSelected) Modifier.border(2.dp, MarksPrimary, RoundedCornerShape(16.dp)) else Modifier
            ),
        cornerRadius = 16.dp,
        tintColor = bgColor,
        frostAlpha = if (isSelected) 0.75f else 0.55f,
        borderGlowColor = borderColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MarksPrimary else Slate200),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (text.isNotBlank()) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (imageUrl.isNotBlank()) {
                    if (text.isNotBlank()) Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, if (isSelected) MarksPrimary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .clickable { onImageClick?.invoke() ?: onSelect() }
                            .padding(6.dp)
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Option $label Diagram",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 150.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionPaletteContent(
    questions: List<Question>,
    currentQuestionIndex: Int,
    statusMap: Map<String, QuestionPaletteStatus>,
    answers: Map<String, Int>,
    onQuestionClick: (Int) -> Unit,
    onSubmitClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Question Palette",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Status Counts Summary
        val answeredCount = answers.size
        val markedCount = statusMap.values.count { it == QuestionPaletteStatus.MARKED_FOR_REVIEW || it == QuestionPaletteStatus.ANSWERED_AND_MARKED }
        val unansweredCount = questions.size - answeredCount

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            PaletteStatusLegendItem("Answered", answeredCount, MarksSuccess, Color.White)
            PaletteStatusLegendItem("Not Answered", unansweredCount, MarksError.copy(alpha = 0.2f), MarksError)
            PaletteStatusLegendItem("Marked", markedCount, MarksPurple, Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Questions Grid
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            questions.forEachIndexed { index, q ->
                val status = statusMap[q.id] ?: QuestionPaletteStatus.NOT_VISITED
                val (bgColor, textColor) = PaletteColorHelper(status)
                val isCurrent = index == currentQuestionIndex

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(bgColor)
                        .then(
                            if (isCurrent) Modifier.border(2.5.dp, MarksPrimary, CircleShape) else Modifier
                        )
                        .clickable { onQuestionClick(index) }
                        .testTag("palette_question_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    if (status == QuestionPaletteStatus.ANSWERED_AND_MARKED) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MarksSuccess)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onSubmitClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MarksSuccess),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Review Summary & Submit", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
