package com.arekb.aqua.ui.screens.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.util.TimeUtils.formatDuration
import com.arekb.aqua.domain.model.DayPeriod
import com.arekb.aqua.domain.model.GoalStatus
import com.arekb.aqua.domain.model.HistoryDay
import com.arekb.aqua.domain.model.HistoryInsights
import com.arekb.aqua.domain.model.HistoryPatternInsights
import com.arekb.aqua.domain.usecase.HistoryInsightsCalculator
import com.arekb.aqua.ui.components.AquaTopAppBar
import com.arekb.aqua.ui.screens.history.components.EmptyHistoryCard
import com.arekb.aqua.ui.screens.history.components.HistoryDayRow
import com.arekb.aqua.ui.screens.history.components.HistoryWeekChart
import com.arekb.aqua.ui.screens.history.components.PatternSection
import com.arekb.aqua.ui.screens.history.components.SectionHeading
import com.arekb.aqua.ui.screens.history.components.SummarySection
import com.arekb.aqua.ui.theme.AquaTheme
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.text.NumberFormat

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    modifier: Modifier = Modifier
) {
    var visibleDaysCount by remember { mutableIntStateOf(5) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val insights = state.insights

    if (state.isLoading || state.insights == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AquaTopAppBar(
                title = ("History"),
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
        ) {

            item {
                SummarySection(insights)
                Spacer(Modifier.height(24.dp))
            }

            item {
                Text("This week", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                HistoryWeekChart(insights.week, insights.today)
                Spacer(Modifier.height(8.dp))
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(10.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Water logged",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(
                        Modifier.size(8.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "No logs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(
                        Modifier.width(18.dp).height(2.dp)
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Daily goal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                SectionHeading("Your rhythm", "30-day patterns and this week’s longest gap")
                Spacer(Modifier.height(12.dp))
                PatternSection(insights.patterns)
            }

            item {
                Spacer(Modifier.height(24.dp))
                SectionHeading("Daily log", "Your recorded totals")
                Spacer(Modifier.height(12.dp))
            }
            if (insights.days.isEmpty()) {
                item {
                    EmptyHistoryCard()
                }
            } else {
                val visibleDays = insights.days.take(visibleDaysCount)
                items(
                    count = visibleDays.size,
                    key = { index -> visibleDays[index].localDate }
                ) { index ->
                    val day = visibleDays[index]
                    HistoryDayRow(day, today = insights.today)
                    Spacer(Modifier.height(10.dp))
                }
                
                if (visibleDaysCount < insights.days.size) {
                    item {
                        OutlinedButton(
                            onClick = { visibleDaysCount += 5 },
                            modifier = Modifier.fillMaxWidth().padding(8.dp).height(56.dp),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Text("Show older entries")
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "History · insights", showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    AquaTheme(darkTheme = false) { HistoryScreen(HistoryUiState(isLoading = false, insights = MockHistoryData.insights)) }
}

@Preview(name = "History · empty", showBackground = true)
@Composable
private fun HistoryScreenEmptyPreview() {
    val today = LocalDate.now()
    val insights = HistoryInsights(
        today = today.toString(),
        week = (6 downTo 0).map { offset ->
            HistoryDay(today.minusDays(offset.toLong()).toString(), 0, 2_000, hasLogs = false)
        },
        days = emptyList(),
        averageCompletedDayMl = 0,
        completedDays = 6,
        goalMetDays = 0,
        goalAvailableDays = 6,
        patterns = HistoryPatternInsights()
    )
    AquaTheme(darkTheme = false) { HistoryScreen(HistoryUiState(isLoading = false, insights = insights)) }
}
