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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionPaletteStatus
import com.example.data.model.Test
import com.example.ui.components.MarksTopAppBar
import com.example.ui.components.PaletteStatusLegendItem
import com.example.ui.theme.MarksError
import com.example.ui.theme.MarksNotVisited
import com.example.ui.theme.MarksPrimary
import com.example.ui.theme.MarksPurple
import com.example.ui.theme.MarksSuccess

@Composable
fun TestInstructionsScreen(
    test: Test,
    onStartTest: () -> Unit,
    onBackClick: () -> Unit
) {
    var agreedToTerms by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            MarksTopAppBar(
                title = "Practice Set Instructions",
                subtitle = test.title,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Checkbox(
                            checked = agreedToTerms,
                            onCheckedChange = { agreedToTerms = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MarksPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "I have read and understood all the practice instructions and marking scheme.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = onStartTest,
                        enabled = agreedToTerms,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MarksPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_test_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Practice Now",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Test Overview Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = test.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InstructionDetail(
                            icon = Icons.Default.Timer,
                            title = "Duration",
                            value = "${test.duration} Minutes"
                        )
                        InstructionDetail(
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            title = "Questions",
                            value = "${test.totalQuestions} Questions"
                        )
                        InstructionDetail(
                            icon = Icons.Default.CheckCircle,
                            title = "Max Marks",
                            value = "${test.totalMarks.toInt()} Marks"
                        )
                    }
                }
            }

            // Marking Scheme Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Marking Scheme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MarkPill(label = "Correct Answer", score = "+4.0", color = MarksSuccess)
                        MarkPill(label = "Incorrect Answer", score = "-${test.negativeMarking}", color = MarksError)
                        MarkPill(label = "Unattempted", score = "0.0", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Question Palette Legend Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Question Palette Guide",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PaletteStatusLegendItem(
                        label = "You have not visited the question yet",
                        count = 1,
                        backgroundColor = MarksNotVisited.copy(alpha = 0.2f),
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PaletteStatusLegendItem(
                        label = "You have not answered the question",
                        count = 2,
                        backgroundColor = MarksError.copy(alpha = 0.2f),
                        textColor = MarksError
                    )
                    PaletteStatusLegendItem(
                        label = "You have answered the question",
                        count = 3,
                        backgroundColor = MarksSuccess,
                        textColor = Color.White
                    )
                    PaletteStatusLegendItem(
                        label = "You have marked the question for review",
                        count = 4,
                        backgroundColor = MarksPurple,
                        textColor = Color.White
                    )
                    PaletteStatusLegendItem(
                        label = "Answered and marked for review (will be evaluated)",
                        count = 5,
                        backgroundColor = MarksPurple,
                        textColor = Color.White,
                        showDot = true
                    )
                }
            }

            // General Exam Rules Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "General Instructions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    BulletPoint("The timer starts as soon as you tap 'Start Practice Now'.")
                    BulletPoint("The practice set will automatically submit when the timer expires.")
                    BulletPoint("You can tap the Question Palette icon in the top bar anytime to jump to any question.")
                    BulletPoint("Use 'Clear Response' to deselect an answered option.")
                    BulletPoint("Detailed step-by-step solutions will be available immediately upon submission.")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InstructionDetail(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MarkPill(label: String, score: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = score,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = color
            )
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• ",
            fontWeight = FontWeight.Bold,
            color = MarksPrimary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
