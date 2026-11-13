package com.arekb.aqua.ui.screens.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arekb.aqua.domain.model.HistoryDay
import com.arekb.aqua.domain.model.HistoryInsights
import com.arekb.aqua.domain.model.HistoryPatternInsights
import com.arekb.aqua.ui.components.AquaTopAppBar
import com.arekb.aqua.ui.screens.history.components.EmptyHistoryCard
import com.arekb.aqua.ui.screens.history.components.HistoryChartLegend
import com.arekb.aqua.ui.screens.history.components.HistoryDayRow
import com.arekb.aqua.ui.screens.history.components.HistoryWeekChart
import com.arekb.aqua.ui.screens.history.components.PatternSection
import com.arekb.aqua.ui.screens.history.components.SectionHeading
import com.arekb.aqua.ui.screens.history.components.SummarySection
import com.arekb.aqua.ui.theme.AquaTheme
import java.time.LocalDate

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    modifier: Modifier = Modifier
) {
    var visibleDaysCount by rememberSaveable { mutableIntStateOf(5) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val insights = state.insights

    if (state.isLoading || state.insights == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularWavyProgressIndicator()
        }
        return
    }

    // Cached so the sublist isn't reallocated on every recomposition of the lazy list content.
    val visibleDays = remember(insights.days, visibleDaysCount) {
        insights.days.take(visibleDaysCount)
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

            item(key = "summary", contentType = "summary") {
                SummarySection(insights, state.settings.liquidUnit)
                Spacer(Modifier.height(24.dp))
            }

            item(key = "week_chart", contentType = "week_chart") {
                Text("This week", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                HistoryWeekChart(insights.week, insights.today, state.settings.liquidUnit)
                Spacer(Modifier.height(8.dp))
            }

            item(key = "chart_legend", contentType = "chart_legend") {
                HistoryChartLegend(modifier = Modifier.fillMaxWidth().padding(end = 12.dp))
            }

            item(key = "rhythm", contentType = "rhythm") {
                Spacer(Modifier.height(24.dp))
                SectionHeading("Your rhythm", "30-day patterns and this week’s longest gap")
                Spacer(Modifier.height(12.dp))
                PatternSection(insights.patterns)
            }

            item(key = "daily_log_header", contentType = "daily_log_header") {
                Spacer(Modifier.height(24.dp))
                SectionHeading("Daily log", "Your recorded totals")
                Spacer(Modifier.height(12.dp))
            }
            if (insights.days.isEmpty()) {
                item(key = "empty", contentType = "empty") {
                    EmptyHistoryCard()
                }
            } else {
                items(
                    count = visibleDays.size,
                    key = { index -> visibleDays[index].localDate },
                    contentType = { "day" }
                ) { index ->
                    val day = visibleDays[index]
                    HistoryDayRow(day, today = insights.today, unit = state.settings.liquidUnit)
                    Spacer(Modifier.height(10.dp))
                }
                
                if (visibleDaysCount < insights.days.size) {
                    item(key = "show_older", contentType = "show_older") {
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
