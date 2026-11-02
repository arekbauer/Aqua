package com.arekb.aqua.domain.usecase

import com.arekb.aqua.domain.model.DailyGoal
import com.arekb.aqua.domain.model.DailyTotal
import com.arekb.aqua.domain.model.DayPeriod
import com.arekb.aqua.domain.model.HistoryDay
import com.arekb.aqua.domain.model.HistoryInsights
import com.arekb.aqua.domain.model.HistoryPatternInsights
import com.arekb.aqua.domain.model.GoalStatus
import com.arekb.aqua.domain.model.WaterIntake
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Builds display-ready history metrics from locally stored intake and goal records. */
object HistoryInsightsCalculator {
    private const val WEEK_DAYS = 7L
    private const val PATTERN_DAYS = 30L
    private const val MIN_PATTERN_LOGGED_DAYS = 7

    fun goalStatus(day: HistoryDay, today: String): GoalStatus = when {
        day.localDate == today -> GoalStatus.IN_PROGRESS
        !day.hasLogs -> GoalStatus.NO_LOGS
        day.goalMl == null -> GoalStatus.GOAL_UNAVAILABLE
        day.totalMl >= day.goalMl -> GoalStatus.GOAL_MET
        else -> GoalStatus.BELOW_GOAL
    }

    fun calculate(
        today: LocalDate,
        totals: List<DailyTotal>,
        entries: List<WaterIntake>,
        goals: List<DailyGoal>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): HistoryInsights {
        val totalByDate = totals.associate { it.localDate to it.totalMl }
        val sortedGoals = goals.sortedBy(DailyGoal::effectiveLocalDate)
        fun goalFor(date: LocalDate): Int? = sortedGoals
            .lastOrNull { it.effectiveLocalDate <= date.toString() }
            ?.goalMl

        val weekStart = today.minusDays(WEEK_DAYS - 1)
        val week = (0 until WEEK_DAYS).map { offset ->
            val date = weekStart.plusDays(offset)
            HistoryDay(
                localDate = date.toString(),
                totalMl = totalByDate[date.toString()] ?: 0,
                goalMl = goalFor(date),
                hasLogs = date.toString() in totalByDate
            )
        }

        val completedDays = (1 until WEEK_DAYS).map { today.minusDays(it) }
        val completedHistory = completedDays.map { date ->
            HistoryDay(
                localDate = date.toString(),
                totalMl = totalByDate[date.toString()] ?: 0,
                goalMl = goalFor(date),
                hasLogs = date.toString() in totalByDate
            )
        }
        val goalAvailableDays = completedHistory.count { it.goalMl != null }
        val goalMetDays = completedHistory.count { day -> day.goalMl != null && day.totalMl >= day.goalMl }
        val average = if (completedHistory.isNotEmpty()) {
            completedHistory.sumOf(HistoryDay::totalMl) / completedHistory.size
        } else {
            null
        }

        val patternStart = today.minusDays(PATTERN_DAYS - 1)
        val patternEntries = entries.filter { it.localDate in patternStart.toString()..today.toString() }
        val loggedDates = patternEntries.map(WaterIntake::localDate).distinct()
        val hasEnoughHistory = loggedDates.size >= MIN_PATTERN_LOGGED_DAYS
        val firstDrinkMinute = if (hasEnoughHistory) {
            val firstMinutes = patternEntries
                .groupBy(WaterIntake::localDate)
                .values
                .mapNotNull { dayEntries ->
                    dayEntries.minByOrNull(WaterIntake::timestampMillis)?.let { intake ->
                        Instant.ofEpochMilli(intake.timestampMillis).atZone(zoneId).toLocalTime().let {
                            it.hour * 60 + it.minute
                        }
                    }
                }
                .sorted()
            firstMinutes.getOrNull(firstMinutes.size / 2)
        } else {
            null
        }
        val dominantPeriod = if (hasEnoughHistory) {
            patternEntries.groupBy { intake ->
                Instant.ofEpochMilli(intake.timestampMillis).atZone(zoneId).hour.toDayPeriod()
            }.maxByOrNull { (_, periodEntries) -> periodEntries.sumOf(WaterIntake::amountMl) }
                ?.key
        } else {
            null
        }

        val gapStart = today.minusDays(WEEK_DAYS)
        val gapEnd = today.minusDays(1)
        val longestGap = if (hasEnoughHistory) entries
            .asSequence()
            .filter { it.localDate in gapStart.toString()..gapEnd.toString() }
            .groupBy(WaterIntake::localDate)
            .values
            .flatMap { dayEntries ->
                val chronological = dayEntries.sortedBy(WaterIntake::timestampMillis)
                chronological.zipWithNext { first, next ->
                    Duration.between(
                        Instant.ofEpochMilli(first.timestampMillis),
                        Instant.ofEpochMilli(next.timestampMillis)
                    ).toMillis()
                }
            }
            .maxOrNull()
            ?.takeIf { it > 0 }
        else null
        val longestGapDate = if (longestGap == null) null else entries
            .asSequence()
            .filter { it.localDate in gapStart.toString()..gapEnd.toString() }
            .groupBy(WaterIntake::localDate)
            .mapNotNull { (date, dayEntries) ->
                val gap = dayEntries.sortedBy(WaterIntake::timestampMillis)
                    .zipWithNext { first, next -> next.timestampMillis - first.timestampMillis }
                    .maxOrNull()
                if (gap == longestGap) date else null
            }
            .firstOrNull()

        val allDays = totals.sortedByDescending(DailyTotal::localDate).map { total ->
            val date = runCatching { LocalDate.parse(total.localDate) }.getOrNull()
            HistoryDay(
                localDate = total.localDate,
                totalMl = total.totalMl,
                goalMl = date?.let(::goalFor),
                hasLogs = true
            )
        }

        return HistoryInsights(
            today = today.toString(),
            week = week,
            days = allDays,
            averageCompletedDayMl = average,
            completedDays = completedHistory.size,
            goalMetDays = goalMetDays,
            goalAvailableDays = goalAvailableDays,
            patterns = HistoryPatternInsights(
                loggedDaysInWindow = loggedDates.size,
                usualFirstDrinkMinute = firstDrinkMinute,
                mostActivePeriod = dominantPeriod,
                longestGap = longestGap,
                longestGapLocalDate = longestGapDate
            )
        )
    }

    private fun Int.toDayPeriod(): DayPeriod = when (this) {
        in 5..11 -> DayPeriod.MORNING
        in 12..16 -> DayPeriod.AFTERNOON
        in 17..20 -> DayPeriod.EVENING
        else -> DayPeriod.NIGHT
    }
}
