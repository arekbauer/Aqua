package com.arekb.aqua.ui.screens.history.components

import android.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arekb.aqua.domain.model.DayPeriod
import com.arekb.aqua.domain.model.GoalStatus
import com.arekb.aqua.domain.model.HistoryDay
import com.arekb.aqua.domain.model.HistoryInsights
import com.arekb.aqua.domain.model.HistoryPatternInsights
import com.arekb.aqua.domain.usecase.HistoryInsightsCalculator
import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.forEach

@Composable
fun SummarySection(insights: HistoryInsights) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        SummaryCard(
            title = "Daily average",
            value = insights.averageCompletedDayMl?.let { "${formatMl(it)} ml" } ?: "—",
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Goal hit",
            value = if (insights.goalAvailableDays == 0) "—" else "${insights.goalMetDays}/${insights.goalAvailableDays} days",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier.height(96.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            Spacer(Modifier.height(8.dp))
            Text(value,
                style = MaterialTheme.typography.titleLargeEmphasized,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
fun SectionHeading(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun HistoryWeekChart(days: List<HistoryDay>, today: String) {
    val colors = MaterialTheme.colorScheme
    val chartDescription = days.joinToString(separator = ", ") { day ->
        val date = formatShortDate(day.localDate)
        when {
            day.localDate == today -> "$date, today, ${day.totalMl} millilitres, in progress, goal ${day.goalMl ?: "unavailable"}"
            !day.hasLogs -> "$date, no logs, goal ${day.goalMl ?: "unavailable"}"
            else -> "$date, ${day.totalMl} millilitres, goal ${day.goalMl ?: "unavailable"}"
        }
    }
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surfaceContainerLow)
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Canvas(
            Modifier.fillMaxWidth().height(160.dp)
                .semantics { contentDescription = "Weekly water intake chart. $chartDescription" }
        ) {
            if (days.isEmpty()) return@Canvas
            val topInset = 12.dp.toPx()
            val bottomInset = 10.dp.toPx()
            val chartHeight = size.height - topInset - bottomInset
            val maximum = (days.flatMap { listOf(it.totalMl, it.goalMl ?: 0) }.maxOrNull() ?: 0)
                .coerceAtLeast(1) * 1.12f
            val slotWidth = size.width / days.size
            val barWidth = (slotWidth * .48f).coerceAtMost(28.dp.toPx())
            val goalPath = Path()
            var hasGoal = false

            days.forEachIndexed { index, day ->
                val centerX = slotWidth * (index + .5f)
                val baseline = topInset + chartHeight
                val barTop = baseline - chartHeight * (day.totalMl / maximum)
                if (day.hasLogs) {
                    drawRoundRect(
                        color = if (day.localDate == today) colors.tertiary else colors.primary,
                        topLeft = androidx.compose.ui.geometry.Offset(centerX - barWidth / 2f, barTop),
                        size = androidx.compose.ui.geometry.Size(barWidth, (baseline - barTop).coerceAtLeast(2.dp.toPx())),
                        cornerRadius = CornerRadius(barWidth / 2f)
                    )
                } else {
                    drawCircle(colors.outlineVariant, radius = 3.dp.toPx(), center = androidx.compose.ui.geometry.Offset(centerX, baseline))
                }

                day.goalMl?.let { goal ->
                    val goalY = baseline - chartHeight * (goal / maximum)
                    if (!hasGoal) goalPath.moveTo(centerX, goalY) else goalPath.lineTo(centerX, goalY)
                    hasGoal = true
                }
            }
            if (hasGoal) {
                drawPath(
                    goalPath,
                    colors.tertiary,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 5.dp.toPx())))
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { day ->
                Text(formatWeekdayShort(day.localDate), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (days.none { it.goalMl != null }) {
            Spacer(Modifier.height(10.dp))
            Text("Goal line unavailable for these dates", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PatternSection(patterns: HistoryPatternInsights) {
    val colors = MaterialTheme.colorScheme
    if (patterns.loggedDaysInWindow < 7) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow)) {
            Text(
                "Insights appear after you have logs on 7 days in the last 30 days.",
                Modifier.fillMaxWidth().padding(20.dp),
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PatternCard(
            title = "First drink average",
            value = patterns.usualFirstDrinkMinute?.let(::formatTime) ?: "Not enough entries"
        )
        PatternCard(
            title = "Most logged period",
            value = patterns.mostActivePeriod?.displayName() ?: "Not enough entries"
        )
        PatternCard(
            title = "Longest gap",
            value = patterns.longestGap?.let(::formatDuration) ?: "Not enough entries"
        )
    }
}

@Composable
private fun PatternCard(title: String, value: String) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(value, style = MaterialTheme.typography.titleMediumEmphasized, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun HistoryDayRow(day: HistoryDay, today: String) {
    val colors = MaterialTheme.colorScheme
    val isToday = day.localDate == today
    val isGoalMet = day.goalMl != null && day.totalMl >= day.goalMl
    val status = when (HistoryInsightsCalculator.goalStatus(day, today)) {
        GoalStatus.IN_PROGRESS -> "In progress"
        GoalStatus.NO_LOGS -> "No logs"
        GoalStatus.GOAL_UNAVAILABLE -> "Goal unavailable"
        GoalStatus.GOAL_MET -> "Goal met"
        GoalStatus.BELOW_GOAL -> "Below goal"
    }
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(if (isToday) "Today" else formatDate(day.localDate), style = MaterialTheme.typography.titleSmallEmphasized)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isGoalMet) {
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                    }
                    if (!isGoalMet) {
                        Icon(Icons.Rounded.Close, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(status, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("${formatMl(day.totalMl)} ml", style = MaterialTheme.typography.titleMediumEmphasized, color = colors.primary)
                if (day.goalMl != null && day.goalMl > 0) {
                    Spacer(Modifier.height(6.dp))
                    val progress = (day.totalMl.toFloat() / day.goalMl.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.width(64.dp).height(6.dp).clip(CircleShape),
                        color = colors.primary.copy(0.4f),
                        trackColor = colors.surfaceContainerHighest,
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Your history starts here", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,)
        }
    }
}

private fun formatTime(minuteOfDay: Int): String = LocalTime.of(minuteOfDay / 60, minuteOfDay % 60)
    .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))

private fun formatMl(value: Int): String = NumberFormat.getIntegerInstance(Locale.getDefault()).format(value)

private fun formatDuration(millis: Long): String {
    val duration = Duration.ofMillis(millis)
    val hours = duration.toHours()
    val minutes = (duration.toMinutes() % 60).toInt()
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

private fun DayPeriod.displayName(): String = when (this) {
    DayPeriod.MORNING -> "Morning"
    DayPeriod.AFTERNOON -> "Afternoon"
    DayPeriod.EVENING -> "Evening"
    DayPeriod.NIGHT -> "Night"
}

private fun formatDate(value: String): String = runCatching {
    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()))
}.getOrDefault(value)

private fun formatShortDate(value: String): String = runCatching {
    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
}.getOrDefault(value)

private fun formatWeekday(value: String): String = runCatching {
    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("EEEE", Locale.getDefault()))
}.getOrDefault("")

private fun formatWeekdayShort(value: String): String = runCatching {
    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()))
}.getOrDefault("")

internal fun todayHeading(value: String): String = runCatching {
    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("EEEE, dd MMM", Locale.getDefault()))
}.getOrDefault("Today")