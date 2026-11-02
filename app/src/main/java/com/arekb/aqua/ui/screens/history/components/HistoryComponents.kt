package com.arekb.aqua.ui.screens.history.components

import android.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arekb.aqua.domain.model.DayPeriod
import com.arekb.aqua.domain.model.GoalStatus
import com.arekb.aqua.domain.model.HistoryDay
import com.arekb.aqua.domain.model.HistoryInsights
import com.arekb.aqua.domain.model.HistoryPatternInsights
import com.arekb.aqua.domain.usecase.HistoryInsightsCalculator
import com.arekb.aqua.domain.model.LiquidUnit
import com.arekb.aqua.domain.model.label
import com.arekb.aqua.domain.model.toUserUnit
import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.forEach

@Composable
fun SummarySection(insights: HistoryInsights, unit: LiquidUnit) {
    val unitLabel = unit.label()
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        SummaryCard(
            title = "Daily average",
            value = insights.averageCompletedDayMl?.let { "${formatMl(it.toUserUnit(unit))} $unitLabel" } ?: "—",
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
fun HistoryWeekChart(days: List<HistoryDay>, today: String, unit: LiquidUnit) {
    val colors = MaterialTheme.colorScheme
    val unitLabel = unit.label()
    val chartDescription = days.joinToString(separator = ", ") { day ->
        val date = formatShortDate(day.localDate)
        val dayTotal = day.totalMl.toUserUnit(unit)
        val dayGoal = day.goalMl?.toUserUnit(unit)
        when {
            day.localDate == today -> "$date, today, $dayTotal $unitLabel, in progress, goal ${dayGoal ?: "unavailable"}"
            !day.hasLogs -> "$date, no logs, goal ${dayGoal ?: "unavailable"}"
            else -> "$date, $dayTotal $unitLabel, goal ${dayGoal ?: "unavailable"}"
        }
    }
    
    val animationPlayed = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationPlayed.value = true
    }
    val animationProgress = animateFloatAsState(
        targetValue = if (animationPlayed.value) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ChartAnimation"
    )

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelMedium.copy(
        color = colors.onSurface.copy(alpha = 0.75f)
    )

    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surfaceContainerLow)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Canvas(
            Modifier.fillMaxWidth().height(180.dp)
                .semantics { contentDescription = "Weekly water intake chart. $chartDescription" }
        ) {
            if (days.isEmpty()) return@Canvas
            
            // Increase maximum by 25% to leave plenty of room for text labels at the top
            val maximum = (days.flatMap { listOf(it.totalMl, it.goalMl ?: 0) }.maxOrNull() ?: 0).coerceAtLeast(1) * 1.25f
            val topInset = 24.dp.toPx()
            val bottomInset = 12.dp.toPx()
            val chartHeight = size.height - topInset - bottomInset
            val slotWidth = size.width / days.size
            val barWidth = (slotWidth * 0.66f).coerceAtMost(44.dp.toPx())
            val cornerRadius = CornerRadius(12.dp.toPx())
            
            val goalPath = Path()
            var hasGoal = false

            // Draw goal line behind the bars
            days.forEachIndexed { index, day ->
                val centerX = slotWidth * (index + 0.5f)
                val baseline = topInset + chartHeight
                
                day.goalMl?.let { goal ->
                    val goalY = baseline - chartHeight * (goal / maximum)
                    if (!hasGoal) {
                        goalPath.moveTo(centerX, goalY)
                    } else {
                        goalPath.lineTo(centerX, goalY)
                    }
                    hasGoal = true
                }
            }
            
            if (hasGoal) {
                drawPath(
                    path = goalPath,
                    color = colors.outlineVariant,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 8.dp.toPx()), 0f)
                    )
                )
            }

            // Draw bars and labels
            days.forEachIndexed { index, day ->
                val centerX = slotWidth * (index + 0.5f)
                val baseline = topInset + chartHeight
                
                val targetHeight = chartHeight * (day.totalMl / maximum)
                val animatedHeight = targetHeight * animationProgress.value
                val finalHeight = animatedHeight.coerceAtLeast(12.dp.toPx()) // Minimum height so it's visible
                val barTop = baseline - finalHeight
                
                if (day.hasLogs) {
                    val barColor = if (day.localDate == today) colors.tertiary else colors.primary
                    drawRoundRect(
                        color = barColor,
                        topLeft = androidx.compose.ui.geometry.Offset(centerX - barWidth / 2f, barTop),
                        size = androidx.compose.ui.geometry.Size(barWidth, finalHeight),
                        cornerRadius = cornerRadius
                    )
                    
                    // Fade in the text labels slightly after the bars start rising
                    val textAlpha = (animationProgress.value * 1.5f - 0.5f).coerceIn(0f, 1f)
                    if (textAlpha > 0f) {
                        val valueText = day.totalMl.toUserUnit(unit).toString()
                        val textLayout = textMeasurer.measure(valueText, style = labelStyle)
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = androidx.compose.ui.geometry.Offset(
                                x = centerX - textLayout.size.width / 2f,
                                y = barTop - textLayout.size.height - 4.dp.toPx()
                            ),
                            alpha = textAlpha
                        )
                    }
                } else {
                    drawCircle(
                        color = colors.outlineVariant,
                        radius = 3.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(centerX, baseline - 3.dp.toPx())
                    )
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            days.forEach { day ->
                val isToday = day.localDate == today
                Text(
                    text = formatWeekdayShort(day.localDate),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isToday) colors.tertiary else colors.onSurfaceVariant
                )
            }
        }
        
        if (days.none { it.goalMl != null }) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Goal line unavailable for these dates",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HistoryChartLegend(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
        val goalLineColor = MaterialTheme.colorScheme.outlineVariant
        Canvas(Modifier.width(18.dp).height(2.dp)) {
            drawLine(
                color = goalLineColor,
                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                strokeWidth = size.height,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            "Goal",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
fun HistoryDayRow(day: HistoryDay, today: String, unit: LiquidUnit) {
    val colors = MaterialTheme.colorScheme
    val unitLabel = unit.label()
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
                val dayTotal = day.totalMl.toUserUnit(unit)
                Text("${formatMl(dayTotal)} $unitLabel", style = MaterialTheme.typography.titleMediumEmphasized, color = colors.primary)
                if (day.goalMl != null && day.goalMl > 0) {
                    Spacer(Modifier.height(6.dp))
                    val progress = (day.totalMl.toFloat() / day.goalMl.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
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
            Text("Your history starts here", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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