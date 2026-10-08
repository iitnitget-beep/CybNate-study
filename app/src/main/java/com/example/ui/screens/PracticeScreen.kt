package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Attempt
import com.example.data.model.Question
import com.example.data.model.Test
import com.example.ui.components.MarksTopAppBar
import com.example.ui.theme.MarksError
import com.example.ui.theme.MarksPrimary
import com.example.ui.theme.MarksSuccess
import com.example.ui.theme.MarksWarning
import com.example.ui.viewmodel.MarksViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    test: Test,
    questions: List<Question>,
    viewModel: MarksViewModel,
    onFinishPractice: (Attempt) -> Unit,
    onExitPractice: () -> Unit
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val isLoading = activeSession?.isLoading ?: false

    if (isLoading && questions.isEmpty()) {
        Scaffold(
            topBar = {
                MarksTopAppBar(title = test.chapter.ifBlank { test.title }, onBackClick = onExitPractice)
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator(color = MarksPrimary)
                    Text("Loading chapter practice questions...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        return
    }

    if (questions.isEmpty()) {
        Scaffold(
            topBar = {
                MarksTopAppBar(title = test.chapter.ifBlank { test.title }, onBackClick = onExitPractice)
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "No questions found matching this filter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Try clearing the topic or difficulty filter to load all available questions for this chapter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(
                        onClick = { viewModel.reloadQuestionsWithoutFilter() },
                        colors = ButtonDefaults.buttonColors(containerColor = MarksPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Show All Chapter Questions")
                    }
                    OutlinedButton(
                        onClick = onExitPractice,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Choose Another Chapter")
                    }
                }
            }
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    // User answers map: questionId -> selectedOptionIndex
    val userAnswers = remember { mutableStateMapOf<String, Int>() }
    // Time spent per question in seconds: questionId -> seconds elapsed
    val timeSpentPerQuestion = remember { mutableStateMapOf<String, Int>() }
    
    // Timer ticker for current question
    var currentQuestionSeconds by remember { mutableIntStateOf(0) }
    val currentQuestion = questions.getOrNull(currentIndex)
    val coroutineScope = rememberCoroutineScope()

    // Reset ticker when index changes
    LaunchedEffect(currentIndex) {
        currentQuestionSeconds = timeSpentPerQuestion[currentQuestion?.id] ?: 0
    }

    // Per-question timer increment loop
    LaunchedEffect(currentIndex) {
        while (true) {
            delay(1000L)
            currentQuestionSeconds++
            currentQuestion?.id?.let { qId ->
                timeSpentPerQuestion[qId] = currentQuestionSeconds
            }
        }
    }

    var showSolution by remember { mutableStateOf(false) }
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }
    var showSubmitConfirm by remember { mutableStateOf(false) }

    val answeredCount = userAnswers.size
    val currentSelectedOption = currentQuestion?.let { userAnswers[it.id] }
    val isAnswered = currentSelectedOption != null

    val topicSubtitle = activeSession?.selectedTopic?.let { " • $it" } ?: ""
    val diffSubtitle = activeSession?.selectedDifficulty?.let { " • $it" } ?: ""

    Scaffold(
        topBar = {
            MarksTopAppBar(
                title = test.chapter.ifBlank { test.title },
                subtitle = "Practice Mode • Q ${currentIndex + 1} of ${questions.size}$diffSubtitle$topicSubtitle",
                onBackClick = { showSubmitConfirm = true },
                actions = {
                    // Time spent on current question badge
                    val mins = currentQuestionSeconds / 60
                    val secs = currentQuestionSeconds % 60
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MarksPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = MarksPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = String.format("%dm %ds", mins, secs), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MarksPrimary)
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (currentIndex > 0) currentIndex-- },
                        enabled = currentIndex > 0,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Previous")
                    }

                    if (currentIndex < questions.size - 1) {
                        Button(
                            onClick = { currentIndex++ },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MarksPrimary),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = { showSubmitConfirm = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MarksSuccess),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Finish & Analyze")
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (currentQuestion != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Question Statement Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Question ${currentIndex + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MarksPrimary
                                )

                                val (diffBg, diffColor) = when (currentQuestion.difficulty.lowercase()) {
                                    "easy" -> Pair(MarksSuccess.copy(alpha = 0.15f), MarksSuccess)
                                    "hard" -> Pair(MarksError.copy(alpha = 0.15f), MarksError)
                                    else -> Pair(MarksWarning.copy(alpha = 0.15f), MarksWarning)
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = diffBg
                                ) {
                                    Text(
                                        text = currentQuestion.difficulty.uppercase(),
                                        color = diffColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (currentQuestion.topic.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = currentQuestion.topic,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentQuestion.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )

                        if (currentQuestion.imageUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            AsyncImage(
                                model = currentQuestion.imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .clickable { zoomedImageUrl = currentQuestion.imageUrl }
                            )
                        }
                    }
                }

                // Options
                Text(text = "Select Option:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                val optionLabels = listOf("A", "B", "C", "D")
                currentQuestion.options.forEachIndexed { optIndex, optText ->
                    val isSelected = currentSelectedOption == optIndex
                    val isCorrect = currentQuestion.correctIndex == optIndex

                    val (cardBorder, cardBg) = when {
                        isAnswered && isCorrect -> Pair(MarksSuccess, MarksSuccess.copy(alpha = 0.1f))
                        isAnswered && isSelected && !isCorrect -> Pair(MarksError, MarksError.copy(alpha = 0.1f))
                        isSelected -> Pair(MarksPrimary, MarksPrimary.copy(alpha = 0.1f))
                        else -> Pair(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), MaterialTheme.colorScheme.surface)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = if (isSelected || (isAnswered && isCorrect)) 2.dp else 1.dp, color = cardBorder, shape = RoundedCornerShape(14.dp))
                            .clickable {
                                userAnswers[currentQuestion.id] = optIndex
                                showSolution = true
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isAnswered && isCorrect -> MarksSuccess
                                            isAnswered && isSelected && !isCorrect -> MarksError
                                            isSelected -> MarksPrimary
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = optionLabels.getOrElse(optIndex) { "${optIndex + 1}" },
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected || (isAnswered && isCorrect)) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = optText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Explanation & Solution Toggle
                if (isAnswered || showSolution) {
                    val isUserCorrect = currentSelectedOption == currentQuestion.correctIndex
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = (if (isUserCorrect) MarksSuccess else MarksError).copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isUserCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (isUserCorrect) MarksSuccess else MarksError
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isUserCorrect) "Correct! (+${currentQuestion.marks.toInt()} Marks)" else "Incorrect. (-${test.negativeMarking.toString().removeSuffix(".0")} Mark)",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUserCorrect) MarksSuccess else MarksError
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "Step-by-step Solution:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentQuestion.explanation.ifBlank { "Textbook analytical solution with formulas applied." },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSubmitConfirm) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirm = false },
            title = { Text("Finish Practice Session?") },
            text = { Text("You have answered ${userAnswers.size} of ${questions.size} questions. Total time spent and accuracy will be saved to your analysis record.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirm = false
                        
                        var score = 0.0
                        var totalMarks = 0.0
                        var correctCount = 0
                        var wrongCount = 0
                        var skippedCount = 0

                        for (q in questions) {
                            totalMarks += q.marks
                            val userAnswer = userAnswers[q.id]
                            if (userAnswer == null) {
                                skippedCount++
                            } else {
                                if (userAnswer == q.correctIndex) {
                                    correctCount++
                                    score += q.marks
                                } else {
                                    wrongCount++
                                    score -= test.negativeMarking
                                }
                            }
                        }

                        val totalSecs = timeSpentPerQuestion.values.sum()
                        val attempt = Attempt(
                            testId = test.id,
                            testTitle = test.chapter.ifBlank { test.title },
                            exam = test.exam,
                            score = score,
                            totalMarks = totalMarks,
                            correctCount = correctCount,
                            wrongCount = wrongCount,
                            skippedCount = skippedCount,
                            timeTakenSeconds = totalSecs,
                            answers = userAnswers.toMap(),
                            completed = true
                        )
                        viewModel.saveAttempt(attempt)
                        onFinishPractice(attempt)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MarksSuccess)
                ) {
                    Text("Finish & View Analysis")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirm = false }) {
                    Text("Continue Practicing")
                }
            }
        )
    }

    if (zoomedImageUrl != null) {
        com.example.ui.components.ZoomableImageDialog(
            imageUrl = zoomedImageUrl!!,
            title = "Question Diagram",
            onDismiss = { zoomedImageUrl = null }
        )
    }
}
