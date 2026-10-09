package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MarksTopAppBar
import com.example.ui.theme.MarksPrimary
import com.example.ui.theme.MarksSuccess
import com.example.ui.viewmodel.MarksViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    examTitle: String,
    subject: String = "All",
    viewModel: MarksViewModel,
    onBackClick: () -> Unit,
    onTabSelected: (String) -> Unit
) {
    var selectedTimeframe by remember { mutableStateOf("Weekly") } // Weekly / Monthly
    var selectedDifficultyFilter by remember { mutableStateOf("All Qs") } // All Qs, Easy Qs, Moderate Qs, Tough Qs

    val attemptsState by viewModel.userAttemptsState.collectAsState()

    val attempts = (attemptsState as? UiState.Success)?.data ?: emptyList()
    val totalSolved = attempts.sumOf { it.correctCount + it.wrongCount }
    val totalCorrect = attempts.sumOf { it.correctCount }
    val accuracy = if (totalSolved > 0) (totalCorrect.toFloat() / totalSolved.toFloat() * 100f).toInt() else 0
    val totalTimeSecs = attempts.sumOf { it.timeTakenSeconds }
    val avgTimePerQ = if (totalSolved > 0) totalTimeSecs / totalSolved else 8
    val avgTimeFormatted = run {
        val m = avgTimePerQ / 60
        val s = avgTimePerQ % 60
        String.format("%dm %ds", m, s)
    }

    val weekRanges = remember { getPast4WeekRanges() }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    com.example.ui.components.LiquidShaderBackground(
        variant = com.example.ui.components.LiquidThemeVariant.AURORA_CALM,
        isDarkTheme = isDark
    ) {
        Scaffold(
            topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                MarksTopAppBar(
                    title = examTitle,
                    subtitle = "$subject Detailed Attempt Analysis",
                    onBackClick = onBackClick
                )

                val isNeet = examTitle.contains("neet", ignoreCase = true) || viewModel.selectedExamBranch.collectAsState().value.contains("neet", ignoreCase = true)
                val tabs = if (isNeet) {
                    listOf("Home", "Bot", "Zoo", "Phy", "Chem", "Analysis")
                } else {
                    listOf("Home", "Phy", "Chem", "Math", "Analysis")
                }

                ScrollableTabRow(
                    selectedTabIndex = tabs.indexOf("Analysis").coerceAtLeast(0),
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MarksPrimary
                ) {
                    tabs.forEach { tab ->
                        Tab(
                            selected = tab == "Analysis",
                            onClick = { if (tab != "Analysis") onTabSelected(tab) },
                            text = { Text(text = tab, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
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
            // Weekly / Monthly Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { selectedTimeframe = "Weekly" },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedTimeframe == "Weekly") MarksPrimary.copy(alpha = 0.15f) else Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Weekly", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { selectedTimeframe = "Monthly" },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedTimeframe == "Monthly") MarksPrimary.copy(alpha = 0.15f) else Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Monthly", fontWeight = FontWeight.Bold)
                }
            }

            // Difficulty filter chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All Qs ✓", "Easy Qs", "Moderate Qs", "Tough Qs").forEach { diff ->
                    item {
                        FilterChip(
                            selected = selectedDifficultyFilter == diff,
                            onClick = { selectedDifficultyFilter = diff },
                            label = { Text(diff, fontSize = 12.sp) }
                        )
                    }
                }
            }

            Text(
                text = "Last 4 weeks comparison",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Card 1: Qs Attempted Bar Chart
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Qs Attempted", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("$totalSolved Qs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MarksPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Qs solved this week", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dynamic 4-week bar chart
                    Row(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        BarChartColumn(weekRanges.getOrElse(0) { "Week 1" }, 0, "0 Qs")
                        BarChartColumn(weekRanges.getOrElse(1) { "Week 2" }, 0, "0 Qs")
                        BarChartColumn(weekRanges.getOrElse(2) { "Week 3" }, 0, "0 Qs")
                        BarChartColumn(weekRanges.getOrElse(3) { "This Week" }, totalSolved, "$totalSolved Qs", isHighlighted = true)
                    }
                }
            }

            // Card 2: Accuracy Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Accuracy", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("$accuracy%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MarksSuccess)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("This week's average", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        BarChartColumn(weekRanges.getOrElse(0) { "Week 1" }, 0, "0%", color = MarksSuccess)
                        BarChartColumn(weekRanges.getOrElse(1) { "Week 2" }, 0, "0%", color = MarksSuccess)
                        BarChartColumn(weekRanges.getOrElse(2) { "Week 3" }, 0, "0%", color = MarksSuccess)
                        BarChartColumn(weekRanges.getOrElse(3) { "This Week" }, accuracy, "$accuracy%", color = MarksSuccess, isHighlighted = true)
                    }
                }
            }

            // Card 3: Time Per Qs
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Time Per Qs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(avgTimeFormatted, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("This week's average", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        BarChartColumn(weekRanges.getOrElse(0) { "Week 1" }, 0, "0m 0s", color = Color(0xFFEC4899))
                        BarChartColumn(weekRanges.getOrElse(1) { "Week 2" }, 0, "0m 0s", color = Color(0xFFEC4899))
                        BarChartColumn(weekRanges.getOrElse(2) { "Week 3" }, 0, "0m 0s", color = Color(0xFFEC4899))
                        BarChartColumn(weekRanges.getOrElse(3) { "This Week" }, avgTimePerQ.coerceAtLeast(10), avgTimeFormatted, color = Color(0xFFEC4899), isHighlighted = true)
                    }
                }
            }
        }
    }
}
}

@Composable
private fun RowScope.BarChartColumn(
    dateRange: String,
    value: Int,
    label: String,
    color: Color = MarksPrimary,
    isHighlighted: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.weight(1f).fillMaxHeight()
    ) {
        if (value > 0 || isHighlighted) {
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isHighlighted) color else Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height((value.coerceIn(10, 80)).dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        } else {
            Text(text = label, fontSize = 10.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = dateRange, fontSize = 9.sp, color = Color.Gray, maxLines = 1)
    }
}

private fun getPast4WeekRanges(): List<String> {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val today = java.time.LocalDate.now()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("d MMM", java.util.Locale.ENGLISH)
        (3 downTo 0).map { i ->
            val end = today.minusDays((i * 7).toLong())
            val start = end.minusDays(6)
            "${start.format(formatter)} - ${end.format(formatter)}"
        }
    } else {
        val sdf = java.text.SimpleDateFormat("d MMM", java.util.Locale.ENGLISH)
        (3 downTo 0).map { i ->
            val endCal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -i * 7) }
            val startCal = java.util.Calendar.getInstance().apply {
                time = endCal.time
                add(java.util.Calendar.DAY_OF_YEAR, -6)
            }
            "${sdf.format(startCal.time)} - ${sdf.format(endCal.time)}"
        }
    }
}
