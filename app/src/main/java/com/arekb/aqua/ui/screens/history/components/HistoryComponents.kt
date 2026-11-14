package com.arekb.aqua.ui.screens.history.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.arekb.aqua.domain.model.DayPeriod
import com.arekb.aqua.domain.model.GoalStatus
import com.arekb.aqua.domain.model.HistoryDay
import com.arekb.aqua.domain.model.HistoryInsights
import com.arekb.aqua.domain.model.HistoryPatternInsights
import com.arekb.aqua.domain.model.LiquidUnit
import com.arekb.aqua.domain.model.label
import com.arekb.aqua.domain.model.toUserUnit
import com.arekb.aqua.domain.usecase.HistoryInsightsCalculator
import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    val chartDescription = remember(days, today, unit) {
        days.joinToString(separator = ", ") { day ->
            val date = formatShortDate(day.localDate)
            val dayTotal = day.totalMl.toUserUnit(unit)
            val dayGoal = day.goalMl?.toUserUnit(unit)
            when {
                day.localDate == today -> "$date, today, $dayTotal $unitLabel, in progress, goal ${dayGoal ?: "unavailable"}"
                !day.hasLogs -> "$date, no logs, goal ${dayGoal ?: "unavailable"}"
                else -> "$date, $dayTotal $unitLabel, goal ${dayGoal ?: "unavailable"}"
            }
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
    val baseLabelStyle = MaterialTheme.typography.labelMedium
    val labelColor = colors.onSurface
    val labelStyle = remember(baseLabelStyle, labelColor) {
        baseLabelStyle.copy(color = labelColor.copy(alpha = 0.75f))
    }
    val primaryColor = colors.primary
    val todayColor = colors.tertiary
    val outlineColor = colors.outlineVariant

    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surfaceContainerLow)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // A single node that draws the whole chart. Everything that only depends on the data or the
        // size (scale, goal path, stroke, measured labels) is computed once inside drawWithCache;
        // only the animation progress is read per frame, in the draw block.
        Spacer(
            Modifier.fillMaxWidth().height(180.dp)
                .semantics { contentDescription = "Weekly water intake chart. $chartDescription" }
                .drawWithCache {
                    if (days.isEmpty()) return@drawWithCache onDrawBehind { }

                    // Increase maximum by 25% to leave plenty of room for text labels at the top
                    val maximum = (days.maxOfOrNull { maxOf(it.totalMl, it.goalMl ?: 0) } ?: 0)
                        .coerceAtLeast(1) * 1.25f
                    val topInset = 24.dp.toPx()
                    val bottomInset = 12.dp.toPx()
                    val chartHeight = size.height - topInset - bottomInset
                    val baseline = topInset + chartHeight
                    val slotWidth = size.width / days.size
                    val barWidth = (slotWidth * 0.66f).coerceAtMost(44.dp.toPx())
                    val cornerRadius = CornerRadius(12.dp.toPx())
                    // Minimum height so a logged day is always visible
                    val minBarHeight = 12.dp.toPx()
                    val labelGap = 4.dp.toPx()
                    val dotRadius = 3.dp.toPx()

                    val goalPath = Path()
                    var goalPathStarted = false
                    days.forEachIndexed { index, day ->
                        val goal = day.goalMl ?: return@forEachIndexed
                        val centerX = slotWidth * (index + 0.5f)
                        val goalY = baseline - chartHeight * (goal / maximum)
                        if (goalPathStarted) goalPath.lineTo(centerX, goalY) else goalPath.moveTo(centerX, goalY)
                        goalPathStarted = true
                    }
                    val hasGoal = goalPathStarted
                    val goalStroke = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 8.dp.toPx()), 0f)
                    )
                    val valueLayouts = days.map { day ->
                        if (day.hasLogs) {
                            textMeasurer.measure(day.totalMl.toUserUnit(unit).toString(), style = labelStyle)
                        } else {
                            null
                        }
                    }

                    onDrawBehind {
                        val progress = animationProgress.value

                        // Goal line is drawn behind the bars
                        if (hasGoal) {
                            drawPath(path = goalPath, color = outlineColor, style = goalStroke)
                        }

                        // Fade in the text labels slightly after the bars start rising
                        val textAlpha = (progress * 1.5f - 0.5f).coerceIn(0f, 1f)

                        days.forEachIndexed { index, day ->
                            val centerX = slotWidth * (index + 0.5f)
                            if (day.hasLogs) {
                                val targetHeight = chartHeight * (day.totalMl / maximum)
                                val barHeight = (targetHeight * progress).coerceAtLeast(minBarHeight)
                                val barTop = baseline - barHeight
                                drawRoundRect(
                                    color = if (day.localDate == today) todayColor else primaryColor,
                                    topLeft = Offset(centerX - barWidth / 2f, barTop),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = cornerRadius
                                )

                                val valueLayout = valueLayouts[index]
                                if (valueLayout != null && textAlpha > 0f) {
                                    drawText(
                                        textLayoutResult = valueLayout,
                                        topLeft = Offset(
                                            x = centerX - valueLayout.size.width / 2f,
                                            y = barTop - valueLayout.size.height - labelGap
                                        ),
                                        alpha = textAlpha
                                    )
                                }
                            } else {
                                drawCircle(
                                    color = outlineColor,
                                    radius = dotRadius,
                                    center = Offset(centerX, baseline - dotRadius)
                                )
                            }
                        }
                    }
                }
        )

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
        Spacer(
            Modifier.width(18.dp).height(2.dp).drawWithCache {
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f)
                onDrawBehind {
                    drawLine(
                        color = goalLineColor,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = size.height,
                        cap = StrokeCap.Round,
                        pathEffect = dashEffect
                    )
                }
            }
        )
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
    val goalMl = day.goalMl
    val isGoalMet = goalMl != null && day.totalMl >= goalMl
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
                if (goalMl != null && goalMl > 0) {
                    Spacer(Modifier.height(6.dp))
                    val progress = (day.totalMl.toFloat() / goalMl.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
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
