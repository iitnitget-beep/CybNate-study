package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Attempt
import com.example.ui.components.MarksTopAppBar
import com.example.ui.theme.MarksError
import com.example.ui.theme.MarksPrimary
import com.example.ui.theme.MarksPrimaryDark
import com.example.ui.theme.MarksPrimaryLight
import com.example.ui.theme.MarksSuccess
import com.example.ui.theme.MarksWarning
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700

@Composable
fun ResultScreen(
    attempt: Attempt,
    onReviewClick: () -> Unit,
    onRetakeClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    val totalQuestions = attempt.correctCount + attempt.wrongCount + attempt.skippedCount
    val attemptedQuestions = attempt.correctCount + attempt.wrongCount
    val accuracy = if (attemptedQuestions > 0) {
        ((attempt.correctCount.toDouble() / attemptedQuestions) * 100).toInt()
    } else 0

    val percentage = if (attempt.totalMarks > 0) {
        ((attempt.score / attempt.totalMarks) * 100).toInt().coerceAtLeast(0)
    } else 0

    val performanceBadge = when {
        percentage >= 80 -> "Outstanding! 🏆"
        percentage >= 60 -> "Great Work! 🚀"
        percentage >= 40 -> "Good Effort! 📚"
        else -> "Keep Practicing! 💪"
    }

    val timeMinutes = attempt.timeTakenSeconds / 60
    val timeSeconds = attempt.timeTakenSeconds % 60
    val timeFormatted = "${timeMinutes}m ${timeSeconds}s"

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    com.example.ui.components.LiquidShaderBackground(
        variant = com.example.ui.components.LiquidThemeVariant.AURORA_CALM,
        isDarkTheme = isDark
    ) {
        Scaffold(
            topBar = {
                MarksTopAppBar(
                    title = "Test Analysis",
                    subtitle = attempt.testTitle.ifBlank { "Performance Summary" },
                    onBackClick = onHomeClick
                )
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onReviewClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MarksPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("review_solutions_button")
                        ) {
                            Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Solutions & Detailed Explanations", fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRetakeClick,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retake", fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = onHomeClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Dashboard", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Score Banner with Liquid Glass
                com.example.ui.components.LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    tintColor = MaterialTheme.colorScheme.surface,
                    frostAlpha = 0.65f,
                    borderGlowColor = com.example.ui.theme.BumblebeeYellow.copy(alpha = 0.5f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = com.example.ui.theme.BumblebeeYellow.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BumblebeeYellow.copy(alpha = 0.5f)),
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = performanceBadge,
                                    color = com.example.ui.theme.BumblebeeYellowBright,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }

                            Text(
                                text = "${attempt.score.toInt()}",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = com.example.ui.theme.BumblebeeYellowBright
                            )

                            Text(
                                text = "out of ${attempt.totalMarks.toInt()} Marks ($percentage%)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Score progress bar
                            LinearProgressIndicator(
                                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = com.example.ui.theme.EnergonBlueBright,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

            // Key Metrics Breakdown Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Accuracy",
                    value = "$accuracy%",
                    icon = Icons.Default.Speed,
                    color = MarksPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Time Taken",
                    value = timeFormatted,
                    icon = Icons.Default.Timer,
                    color = MarksWarning,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Attempted",
                    value = "$attemptedQuestions/$totalQuestions",
                    icon = Icons.Default.QueryStats,
                    color = MarksPrimaryLight,
                    modifier = Modifier.weight(1f)
                )
            }

            // Question Breakdown Details Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Question Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    BreakdownRow(
                        label = "Correct Answers",
                        count = attempt.correctCount,
                        marksText = "+${(attempt.correctCount * 4)} Marks",
                        color = MarksSuccess,
                        icon = Icons.Default.CheckCircle
                    )

                    BreakdownRow(
                        label = "Incorrect Answers",
                        count = attempt.wrongCount,
                        marksText = "-${attempt.wrongCount} Marks",
                        color = MarksError,
                        icon = Icons.Default.Close
                    )

                    BreakdownRow(
                        label = "Unattempted / Skipped",
                        count = attempt.skippedCount,
                        marksText = "0 Marks",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        icon = Icons.Default.RemoveCircleOutline
                    )
                }
            }

            // Performance Tip
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MarksPrimary.copy(alpha = 0.08f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MarksPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MarksPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (attempt.wrongCount > 0)
                            "Review your ${attempt.wrongCount} incorrect answers to understand concept gaps and avoid negative marks next time!"
                        else
                            "Flawless attempt with zero negative marks! Keep up this precision in upcoming mock tests.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    com.example.ui.components.LiquidGlassCard(
        cornerRadius = 16.dp,
        tintColor = MaterialTheme.colorScheme.surface,
        frostAlpha = 0.65f,
        borderGlowColor = color.copy(alpha = 0.25f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    count: Int,
    marksText: String,
    color: Color,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(text = marksText, style = MaterialTheme.typography.labelSmall, color = color)
            }
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.12f)
        ) {
            Text(
                text = "$count",
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}
